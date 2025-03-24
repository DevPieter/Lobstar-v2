package nl.devpieter.lobstar.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.models.player.CreatePlayer;
import nl.devpieter.lobstar.models.player.PlayerStatus;
import nl.devpieter.lobstar.socket.SocketManager;

public class ConnectionListener {

    private final Lobstar lobstar = Lobstar.getInstance();

    private final SocketManager socketManager = lobstar.getSocketManager();
    private final ServerManager serverManager = lobstar.getServerManager();

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        CreatePlayer createPlayer = new CreatePlayer(player.getUniqueId(), player.getUsername());
        socketManager.send("TryCreatePlayer", createPlayer);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = event.getServer();

        Server currentServer = serverManager.getServer(server.getServerInfo().getName());
        if (currentServer == null) return;

        PlayerStatus playerStatus = new PlayerStatus(player.getUsername(), true, currentServer.id());
        socketManager.send("SetPlayerStatus", player.getUniqueId(), playerStatus);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        socketManager.send("SetPlayerStatus", event.getPlayer().getUniqueId(), null);
    }
}
