package nl.devpieter.lobstar.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.StatusManager;
import nl.devpieter.lobstar.models.player.CreatePlayer;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.SocketManager;

public class ConnectionListener {

    private final Lobstar lobstar = Lobstar.getInstance();
    private final SocketManager socketManager = this.lobstar.getSocketManager();
    private final ServerManager serverManager = this.lobstar.getServerManager();
    private final StatusManager statusManager = this.lobstar.getStatusManager();

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        CreatePlayer createPlayer = new CreatePlayer(player.getUniqueId(), player.getUsername());
        this.socketManager.send("TryCreatePlayer", createPlayer);
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        this.statusManager.setPlayerStatus(player);

        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current != null) {
            Server currentServer = serverManager.getServer(current);
            if (currentServer != null) this.statusManager.setServerStatus(currentServer);
        }

        RegisteredServer previous = event.getPreviousServer();
        if (previous != null) {
            Server previousServer = this.serverManager.getServer(previous);
            if (previousServer != null) this.statusManager.setServerStatus(previousServer);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        this.statusManager.clearPlayerStatus(player);

        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return;

        Server currentServer = serverManager.getServer(current);
        if (currentServer != null) this.statusManager.setServerStatus(currentServer);
    }
}
