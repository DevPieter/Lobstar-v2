package nl.devpieter.lobstar.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.devpieter.lobstar.enums.MotdGetType;
import nl.devpieter.lobstar.managers.MotdManager;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.VirtualHostManager;
import nl.devpieter.lobstar.models.common.MotdSamplePlayer;
import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;

public class ProxyPingListener {

    private final MotdManager motdManager = MotdManager.getInstance();
    private final ServerManager serverManager = ServerManager.getInstance();
    private final VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        InetSocketAddress requestedAddress = event.getConnection().getVirtualHost().orElse(null);
        if (requestedAddress == null) return;

        VirtualHost virtualHost = this.virtualHostManager.findMatchingVirtualHost(requestedAddress.getHostString());
        if (virtualHost == null || virtualHost.motdId() == null) return;

        Motd motd = this.motdManager.getMotdById(virtualHost.motdId());
        if (motd == null) return;

        ServerPing serverPing = this.serverPingRequired(motd) ? this.getServerPing(virtualHost) : event.getPing();
        if (serverPing == null) serverPing = event.getPing();

        ServerPing.Builder serverPingBuilder = serverPing.asBuilder();
        ServerPing.Builder originalBuilder = event.getPing().asBuilder();

        if (!motd.onlinePlayersEnabled()) {
            originalBuilder.nullPlayers();
        } else if (motd.getOnlinePlayersGetType() == MotdGetType.Custom) {
            originalBuilder.onlinePlayers(motd.onlinePlayers());
        } else if (motd.getOnlinePlayersGetType() == MotdGetType.Server) {
            originalBuilder.onlinePlayers(serverPingBuilder.getOnlinePlayers());
        }

        if (motd.getMaximumPlayersGetType() == MotdGetType.Custom) {
            originalBuilder.maximumPlayers(motd.maximumPlayers());
        } else if (motd.getMaximumPlayersGetType() == MotdGetType.Server) {
            originalBuilder.maximumPlayers(serverPingBuilder.getMaximumPlayers());
        }

        if (!motd.samplePlayersEnabled()) {
            originalBuilder.clearSamplePlayers();
        } else if (motd.getSamplePlayersGetType() == MotdGetType.Custom) {
            MotdSamplePlayer[] samplePlayers = motd.samplePlayers();
            if (samplePlayers != null) originalBuilder.samplePlayers(MotdSamplePlayer.toServerPingSamplePlayer(samplePlayers));
        } else if (motd.getSamplePlayersGetType() == MotdGetType.Server) {
            // TODO - Not working
            originalBuilder.samplePlayers(serverPingBuilder.getSamplePlayers());
        }

        if (!motd.descriptionEnabled()) {
            originalBuilder.description(Component.empty());
        } else if (motd.getDescriptionGetType() == MotdGetType.Custom) {
            String description = motd.description();
            if (description == null) description = "";
            originalBuilder.description(MiniMessage.miniMessage().deserialize(description));
        } else if (motd.getDescriptionGetType() == MotdGetType.Server) {
            serverPingBuilder.getDescriptionComponent().ifPresent(originalBuilder::description);
        }

        if (!motd.faviconEnabled()) {
            originalBuilder.favicon(new Favicon(""));
        } else if (motd.getFaviconGetType() == MotdGetType.Custom) {
            String favicon = motd.favicon();
            if (favicon != null) originalBuilder.favicon(new Favicon(favicon));
        } else if (motd.getFaviconGetType() == MotdGetType.Server) {
            originalBuilder.favicon(serverPingBuilder.getFavicon().orElse(new Favicon("")));
        }

        event.setPing(originalBuilder.build());
    }

    private boolean serverPingRequired(@NotNull Motd motd) {
        return motd.getOnlinePlayersGetType() == MotdGetType.Server ||
                motd.getMaximumPlayersGetType() == MotdGetType.Server ||
                motd.getSamplePlayersGetType() == MotdGetType.Server ||
                motd.getDescriptionGetType() == MotdGetType.Server ||
                motd.getFaviconGetType() == MotdGetType.Server;
    }

    private @Nullable ServerPing getServerPing(@NotNull VirtualHost virtualHost) {
        Server server = this.serverManager.getServerById(virtualHost.serverId());
        if (server == null) return null;

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return null;

        return ServerUtils.getServerPing(registeredServer, Duration.ofMillis(500));
    }
}
