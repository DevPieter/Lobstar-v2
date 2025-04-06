package nl.devpieter.lobstar.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.player.PlayerStatus;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.server.ServerStatus;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatusManager {

    private final Lobstar lobstar;
    private final SocketManager socketManager;
    private final ProxyServer proxy;
    private final Logger logger;

    private final ScheduledTask syncTask;

    public StatusManager(@NotNull Lobstar lobstar) {
        this.lobstar = lobstar;
        this.socketManager = lobstar.getSocketManager();
        this.proxy = lobstar.getProxy();
        this.logger = lobstar.getLogger();

        this.syncTask = this.proxy.getScheduler().buildTask(this.lobstar, () -> {
            this.logger.info("[StatusManager] Refreshing player and server statuses");

            this.syncServerStatuses().thenCompose(unused -> this.syncPlayerStatuses()).whenComplete((unused, throwable) -> {
                if (throwable == null) this.logger.info("[StatusManager] Successfully refreshed player and server statuses");
                else this.logger.error("[StatusManager] Failed to refresh player and server statuses", throwable);
            });
        }).repeat(Duration.ofMinutes(10)).delay(Duration.ofMinutes(1)).schedule();
    }

    public void cancelSyncTask() {
        this.syncTask.cancel();
    }

    private ServerManager getServerManager() {
        return this.lobstar.getServerManager();
    }

    public CompletableFuture<Void> syncPlayerStatuses() {
        List<Player> players = this.proxy.getAllPlayers().stream().toList();
        return this.setPlayerStatuses(players);
    }

    public CompletableFuture<Void> syncServerStatuses() {
        return this.setServerStatuses(this.getServerManager().getServers());
    }

    public CompletableFuture<Void> setPlayerStatuses(@NotNull List<Player> players) {
        return CompletableFuture.allOf(players.stream().map(this::setPlayerStatus).toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> setServerStatuses(@NotNull List<Server> servers) {
        return CompletableFuture.allOf(servers.stream().map(this::setServerStatus).toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> setPlayerStatus(@NotNull Player player) {
        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return this.clearPlayerStatus(player);

        Server server = this.getServerManager().getServer(current);
        if (server == null) return this.clearPlayerStatus(player);

        PlayerStatus status = new PlayerStatus(
                player.getUsername(),
                true,
                server.id()
        );

        this.socketManager.send("SetPlayerStatus", player.getUniqueId(), status);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> setServerStatus(@NotNull Server server) {
        RegisteredServer registered = server.findRegisteredServer();
        if (registered == null) return this.clearServerStatus(server);

        ServerPing ping = ServerUtils.getServerPing(registered, Duration.ofSeconds(5));
        if (ping == null) return this.clearServerStatus(server);

        ServerPing.Players players = ping.getPlayers().orElse(null);

        int proxyPlayers = ServerUtils.getPlayerCount(registered);
        int serverPlayers = players != null ? players.getOnline() : 0;
        int maxPlayers = players != null ? players.getMax() : 0;

        ServerStatus status = new ServerStatus(
                true,
                proxyPlayers,
                serverPlayers,
                maxPlayers,
                ping.getVersion().getName()
        );

        this.socketManager.send("SetServerStatus", server.id(), status);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> clearPlayerStatus(@NotNull Player player) {
        this.socketManager.send("ClearPlayerStatus", player.getUniqueId());
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> clearServerStatus(@NotNull Server server) {
        this.socketManager.send("ClearServerStatus", server.id());
        return CompletableFuture.completedFuture(null);
    }
}
