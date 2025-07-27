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

        VirtualHost virtualHost = this.virtualHostManager.findMatchingVirtualHost(requestedAddress.getHostString(), true);
        if (virtualHost == null || virtualHost.getMotdId() == null) return;

        Motd motd = this.motdManager.getMotdById(virtualHost.getMotdId());
        if (motd == null) return;

        ServerPing serverPing = this.serverPingRequired(motd) ? this.getServerPing(virtualHost) : event.getPing();
        if (serverPing == null) serverPing = event.getPing();

        ServerPing.Builder serverPingBuilder = serverPing.asBuilder();
        ServerPing.Builder originalBuilder = event.getPing().asBuilder();

        if (!motd.getOnlinePlayersEnabled()) {
            originalBuilder.nullPlayers();
        } else if (motd.getOnlinePlayersType() == MotdGetType.Custom) {
            originalBuilder.onlinePlayers(motd.getOnlinePlayers());
        } else if (motd.getOnlinePlayersType() == MotdGetType.Server) {
            originalBuilder.onlinePlayers(serverPingBuilder.getOnlinePlayers());
        }

        if (motd.getMaximumPlayersType() == MotdGetType.Custom) {
            originalBuilder.maximumPlayers(motd.getMaximumPlayers());
        } else if (motd.getMaximumPlayersType() == MotdGetType.Server) {
            originalBuilder.maximumPlayers(serverPingBuilder.getMaximumPlayers());
        }

        // TODO - Not working, check why
        if (!motd.getSamplePlayersEnabled()) {
            originalBuilder.clearSamplePlayers();
        } else if (motd.getSamplePlayersType() == MotdGetType.Custom) {
            originalBuilder.samplePlayers(MotdSamplePlayer.toServerPingSamplePlayer(motd.getSamplePlayers()));
        } else if (motd.getSamplePlayersType() == MotdGetType.Server) {
            originalBuilder.samplePlayers(serverPingBuilder.getSamplePlayers());
        }

        if (!motd.getDescriptionEnabled()) {
            originalBuilder.description(Component.empty());
        } else if (motd.getDescriptionType() == MotdGetType.Custom) {
            originalBuilder.description(MiniMessage.miniMessage().deserialize(motd.getDescription()));
        } else if (motd.getDescriptionType() == MotdGetType.Server) {
            serverPingBuilder.getDescriptionComponent().ifPresent(originalBuilder::description);
        }

        if (!motd.getFaviconEnabled()) {
            originalBuilder.favicon(new Favicon(""));
        } else if (motd.getFaviconType() == MotdGetType.Custom) {
            originalBuilder.favicon(new Favicon(motd.getFavicon()));
        } else if (motd.getFaviconType() == MotdGetType.Server) {
            originalBuilder.favicon(serverPingBuilder.getFavicon().orElse(new Favicon("")));
        }

        event.setPing(originalBuilder.build());
    }

    private boolean serverPingRequired(@NotNull Motd motd) {
        return motd.getOnlinePlayersType() == MotdGetType.Server ||
                motd.getMaximumPlayersType() == MotdGetType.Server ||
                motd.getSamplePlayersType() == MotdGetType.Server ||
                motd.getDescriptionType() == MotdGetType.Server ||
                motd.getFaviconType() == MotdGetType.Server;
    }

    private @Nullable ServerPing getServerPing(@NotNull VirtualHost virtualHost) {
        Server server = this.serverManager.getServerById(virtualHost.getServerId());
        if (server == null) return null;

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return null;

        return ServerUtils.getServerPing(registeredServer, Duration.ofMillis(500));
    }
}
