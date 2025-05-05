package nl.devpieter.lobstar.listeners.sees;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.events.player.MoveAllPlayersEvent;
import nl.devpieter.lobstar.socket.events.player.MovePlayerEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final Lobstar lobstar = Lobstar.getInstance();
    private final ServerManager serverManager = ServerManager.getInstance();
    private final ProxyServer proxy = lobstar.getProxy();

    @EventListener
    public void onMovePlayer(MovePlayerEvent event) {
        Player player = proxy.getPlayer(event.playerId()).orElse(null);
        if (player == null) return;

        RegisteredServer registeredServer = this.findAsRegisteredServer(event.serverId());
        if (registeredServer == null) return;

        this.tryMovePlayer(player, registeredServer);
    }

    @EventListener
    public void onMoveAllPlayers(MoveAllPlayersEvent event) {
        RegisteredServer toServer = this.findAsRegisteredServer(event.toServerId());
        if (toServer == null) return;

        if (event.fromServerId() != null) {
            this.moveFromServer(event.fromServerId(), toServer);
            return;
        }

        this.moveAllPlayers(toServer);
    }

    private void moveAllPlayers(@NotNull RegisteredServer toServer) {
        // TODO - Check if getAllPlayers is the right method to use (read doc)
        for (Player player : this.proxy.getAllPlayers()) {
            this.tryMovePlayer(player, toServer);
        }
    }

    private void moveFromServer(@NotNull UUID fromServerId, @NotNull RegisteredServer toServer) {
        Server server = this.serverManager.getServerById(fromServerId);
        if (server == null) return;

        RegisteredServer fromServer = server.findRegisteredServer();
        if (fromServer == null) return;

        for (Player player : fromServer.getPlayersConnected()) {
            this.tryMovePlayer(player, toServer);
        }
    }

    private void tryMovePlayer(@NotNull Player player, @NotNull RegisteredServer toServer) {
        if (!player.isActive()) return;

        ServerConnection currentServer = player.getCurrentServer().orElse(null);
        if (currentServer == null) return;

        if (currentServer.getServerInfo().equals(toServer.getServerInfo())) return;
        player.createConnectionRequest(toServer).fireAndForget();
    }

    private @Nullable RegisteredServer findAsRegisteredServer(@NotNull UUID serverId) {
        Server server = this.serverManager.getServerById(serverId);
        if (server == null) return null;

        return server.findRegisteredServer();
    }
}
