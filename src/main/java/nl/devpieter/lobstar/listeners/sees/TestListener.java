package nl.devpieter.lobstar.listeners.sees;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.socket.events.player.KickAllPlayersEvent;
import nl.devpieter.lobstar.socket.events.player.KickPlayerEvent;
import nl.devpieter.lobstar.socket.events.player.MoveAllPlayersEvent;
import nl.devpieter.lobstar.socket.events.player.MovePlayerEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;

public class TestListener implements Listener {

    private final Lobstar lobstar = Lobstar.getInstance();
    private final ServerManager serverManager = lobstar.getServerManager();
    private final ProxyServer proxy = lobstar.getProxy();

    @EventListener
    public void onKickPlayer(KickPlayerEvent event) {
        System.out.println("Player " + event.playerId() + " was kicked");

        Component reason = Component.text("You were kicked from the server").color(NamedTextColor.RED);
        if (event.reason() != null) reason = Component.text(event.reason()).color(NamedTextColor.RED);

        Player player = proxy.getPlayer(event.playerId()).orElse(null);
        if (player != null) player.disconnect(reason);
    }

    @EventListener
    public void onKickAllPlayers(KickAllPlayersEvent event) {
        System.out.println("All players were kicked from server " + event.serverId());
    }

    @EventListener
    public void onMovePlayer(MovePlayerEvent event) {
        System.out.println("Player " + event.playerId() + " was moved to server " + event.serverId());

        Player player = proxy.getPlayer(event.playerId()).orElse(null);
        if (player == null) return;

        Server server = serverManager.getServer(event.serverId());
        if (server == null) return;

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return;

        player.createConnectionRequest(registeredServer).fireAndForget();
    }

    @EventListener
    public void onMoveAllPlayers(MoveAllPlayersEvent event) {
        System.out.println("All players were moved from server " + event.fromServerId() + " to server " + event.toServerId());
    }
}
