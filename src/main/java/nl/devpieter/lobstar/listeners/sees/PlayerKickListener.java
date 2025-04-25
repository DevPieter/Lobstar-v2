package nl.devpieter.lobstar.listeners.sees;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.events.player.KickAllPlayersEvent;
import nl.devpieter.lobstar.socket.events.player.KickPlayerEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerKickListener implements Listener {

    private final Lobstar lobstar = Lobstar.getInstance();
    private final ServerManager serverManager = lobstar.getServerManager();
    private final ProxyServer proxy = lobstar.getProxy();

    @EventListener
    public void onKickPlayer(KickPlayerEvent event) {
        Player player = this.proxy.getPlayer(event.playerId()).orElse(null);
        if (player == null) return;

        this.kickPlayer(player, event.reason(), event.toLobby());
    }

    @EventListener
    public void onKickAllPlayers(KickAllPlayersEvent event) {
        String reason = event.reason();
        boolean toLobby = event.toLobby();

        if (event.serverId() != null) {
            this.kickFromServer(event.serverId(), reason, toLobby);
            return;
        }

        this.kickAllPlayers(reason, toLobby);
    }

    private void kickAllPlayers(@Nullable String reason, boolean toLobby) {
        // TODO - Check if getAllPlayers is the right method to use (read doc)
        for (Player player : this.proxy.getAllPlayers()) {
            this.kickPlayer(player, reason, toLobby);
        }
    }

    private void kickFromServer(@NotNull UUID serverId, @Nullable String reason, boolean toLobby) {
        Server server = this.serverManager.getServer(serverId);
        if (server == null) return;

        RegisteredServer fromServer = server.findRegisteredServer();
        if (fromServer == null) return;

        for (Player player : fromServer.getPlayersConnected()) {
            this.kickPlayer(player, reason, toLobby);
        }
    }

    private void kickPlayer(@NotNull Player player, @Nullable String reason, boolean toLobby) {
        if (!player.isActive()) return;

        Component component = Component.text("You were kicked from the server").color(NamedTextColor.RED);
        if (reason != null) component = Component.text(reason).color(NamedTextColor.RED);

        if (!toLobby) {
            player.disconnect(component);
            return;
        }

        Server server = this.serverManager.getLobbyServer(player);
        if (server == null) {
            player.disconnect(component);
            return;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            player.disconnect(component);
            return;
        }

        player.createConnectionRequest(registeredServer).fireAndForget();
    }
}
