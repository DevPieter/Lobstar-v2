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
import nl.devpieter.lobstar.models.player.CreatePlayer;
import nl.devpieter.lobstar.models.player.PlayerStatus;
import nl.devpieter.lobstar.models.server.Server;
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
    public void onServerConnected(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return;

        Server currentServer = serverManager.getServer(current);
        if (currentServer == null) return;
        serverManager.setRemoteServerStatus(currentServer);

        PlayerStatus playerStatus = new PlayerStatus(player.getUsername(), true, currentServer.id());
        socketManager.send("SetPlayerStatus", player.getUniqueId(), playerStatus);

        RegisteredServer previous = event.getPreviousServer();
        if (previous == null) return;

        Server previousServer = serverManager.getServer(previous);
        if (previousServer != null) serverManager.setRemoteServerStatus(previousServer);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        socketManager.send("SetPlayerStatus", player.getUniqueId(), null);

        ServerConnection current = player.getCurrentServer().orElse(null);
        if (current == null) return;

        Server currentServer = serverManager.getServer(current);
        if (currentServer != null) serverManager.setRemoteServerStatus(currentServer);
    }
}
