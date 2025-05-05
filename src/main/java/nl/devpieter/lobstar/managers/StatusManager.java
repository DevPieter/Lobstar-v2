package nl.devpieter.lobstar.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.events.server.RegisteredServerDeletedEvent;
import nl.devpieter.lobstar.events.server.RegisteredServerRegisteredEvent;
import nl.devpieter.lobstar.events.server.RegisteredServerUpdatedEvent;
import nl.devpieter.lobstar.events.server.RegisteredServersSyncedEvent;
import nl.devpieter.lobstar.models.common.MinecraftVersion;
import nl.devpieter.lobstar.models.player.PlayerStatus;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.server.ServerStatus;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.utils.ServerUtils;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatusManager implements Listener {

    private static StatusManager INSTANCE;

    private final Lobstar lobstar = Lobstar.getInstance();
    private final SocketManager socketManager = SocketManager.getInstance();
    private final ServerManager serverManager = ServerManager.getInstance();

    private final ProxyServer proxy = this.lobstar.getProxy();
    private final Logger logger = this.lobstar.getLogger();

    private @Nullable ScheduledTask syncTask;

    private StatusManager() {
    }

    public static StatusManager getInstance() {
        if (INSTANCE == null) INSTANCE = new StatusManager();
        return INSTANCE;
    }

    public void startSyncTask() {
        this.cancelSyncTask();
        if (this.syncTask != null) return;

        this.logger.info("[StatusManager] <Sync> Starting status sync task");

        this.syncTask = this.proxy.getScheduler().buildTask(this.lobstar, () -> {
            this.syncServerStatuses().thenCompose(unused -> this.syncPlayerStatuses()).whenComplete((unused, throwable) -> {
                if (throwable != null) this.logger.error("[StatusManager] <Sync> Failed to refresh statuses", throwable);
            });
        }).repeat(Duration.ofMinutes(5)).delay(Duration.ofMinutes(1)).schedule();
    }

    public void cancelSyncTask() {
        if (this.syncTask == null) return;

        this.syncTask.cancel();
        this.syncTask = null;
    }

    public CompletableFuture<Void> syncPlayerStatuses() {
        return this.setPlayerStatuses(this.proxy.getAllPlayers().stream().toList());
    }

    public CompletableFuture<Void> syncServerStatuses() {
        return this.setServerStatuses(this.serverManager.getServers());
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

        Server server = this.serverManager.getServer(current);
        if (server == null) return this.clearPlayerStatus(player);

        InetSocketAddress socketAddress = player.getRemoteAddress();
        InetSocketAddress virtualHost = player.getVirtualHost().orElse(null); // TODO - Send virtual host

        this.socketManager.send("SetPlayerStatus", player.getUniqueId(), new PlayerStatus(
                player.getUsername(),

                player.getPing(),
                true,

                socketAddress.getAddress().getHostAddress(),
                socketAddress.getPort(),

                server.id(),
                MinecraftVersion.of(player.getProtocolVersion())
        ));
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> setServerStatus(@NotNull Server server) {
        RegisteredServer registered = server.findRegisteredServer();
        if (registered == null) return this.clearServerStatus(server);

        ServerPing ping = ServerUtils.getServerPing(registered, Duration.ofSeconds(5));
        if (ping == null) return this.clearServerStatus(server);

        ServerPing.Players players = ping.getPlayers().orElse(null);
        MinecraftVersion version = MinecraftVersion.of(ping.getVersion());

        int onlinePlayers = ServerUtils.getPlayerCount(registered);
        int maxPlayers = players != null ? players.getMax() : 0;

        this.socketManager.send("SetServerStatus", server.id(), new ServerStatus(
                true,

                onlinePlayers,
                maxPlayers,

                version
        ));
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

    @EventListener
    public void onRegisteredServerRegistered(RegisteredServerRegisteredEvent event) {
        Server server = this.serverManager.getServerById(event.serverId());
        if (server != null) this.setServerStatus(server);
    }

    @EventListener
    public void onRegisteredServerUpdated(RegisteredServerUpdatedEvent event) {
        Server server = this.serverManager.getServerById(event.serverId());
        if (server != null) this.setServerStatus(server);
    }

    @EventListener
    public void onRegisteredServerDeleted(RegisteredServerDeletedEvent event) {
        if (!event.permanently()) return;

        Server server = this.serverManager.getServerById(event.serverId());
        if (server != null) this.clearServerStatus(server);
    }

    @EventListener
    public void onRegisteredServersSynced(RegisteredServersSyncedEvent event) {
        this.syncPlayerStatuses();
    }
}
