package nl.devpieter.lobstar;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.commands.LobbyCommand;
import nl.devpieter.lobstar.listeners.sees.PlayerKickListener;
import nl.devpieter.lobstar.listeners.sees.PlayerMoveListener;
import nl.devpieter.lobstar.listeners.velocity.ConnectionListener;
import nl.devpieter.lobstar.listeners.velocity.ProxyPingListener;
import nl.devpieter.lobstar.listeners.velocity.WhitelistListener;
import nl.devpieter.lobstar.managers.*;
import nl.devpieter.lobstar.models.version.Version;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.socket.listeners.motd.MotdCreatedListener;
import nl.devpieter.lobstar.socket.listeners.motd.MotdDeletedListener;
import nl.devpieter.lobstar.socket.listeners.motd.MotdUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.motd.SyncMotdsListener;
import nl.devpieter.lobstar.socket.listeners.player.KickAllPlayersListener;
import nl.devpieter.lobstar.socket.listeners.player.KickPlayerListener;
import nl.devpieter.lobstar.socket.listeners.player.MoveAllPlayersListener;
import nl.devpieter.lobstar.socket.listeners.player.MovePlayerListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerCreatedListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerDeletedListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.server.SyncServersListener;
import nl.devpieter.lobstar.socket.listeners.server.type.ServerTypeCreatedListener;
import nl.devpieter.lobstar.socket.listeners.server.type.ServerTypeDeletedListener;
import nl.devpieter.lobstar.socket.listeners.server.type.ServerTypeUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.server.type.SyncServerTypesListener;
import nl.devpieter.lobstar.socket.listeners.virtualHost.SyncVirtualHostsListener;
import nl.devpieter.lobstar.socket.listeners.virtualHost.VirtualHostCreatedListener;
import nl.devpieter.lobstar.socket.listeners.virtualHost.VirtualHostDeletedListener;
import nl.devpieter.lobstar.socket.listeners.virtualHost.VirtualHostUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.whitelist.SyncWhitelistEntriesListener;
import nl.devpieter.lobstar.socket.listeners.whitelist.WhitelistEntryCreatedListener;
import nl.devpieter.lobstar.socket.listeners.whitelist.WhitelistEntryDeletedListener;
import nl.devpieter.lobstar.socket.listeners.whitelist.WhitelistEntryUpdatedListener;
import nl.devpieter.sees.Sees;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Plugin(id = "lobstar", name = "Lobstar v2", version = BuildConstants.VERSION, description = "TODO", authors = {"DevPieter"})
public class Lobstar {

    private static Lobstar INSTANCE;

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        INSTANCE = this;

        this.logger.info("<Init> Initializing version manager");
        VersionManager versionManager = VersionManager.getInstance();

        this.logger.info("Loading version info, and checking compatibility. Please wait...");
        versionManager.loadVersionInfo(unused -> {
            if (!versionManager.isSuccessfullyLoaded()) {
                this.logger.error("Failed to load version info, cannot continue");
                return;
            }

            this.logger.info("Successfully loaded version info");

            Version apiVersion = versionManager.getApiVersion();
            Version pluginVersion = versionManager.getPluginVersion();
            boolean compatible = versionManager.isCompatible();

            if (apiVersion == null || pluginVersion == null) {
                this.logger.error("API or plugin version is null, cannot continue");
                return;
            }

            this.logger.info("> API Version: {} ({})", apiVersion.getCurrent(), apiVersion.getLatest());
            this.logger.info("> Plugin Version: {} ({})", pluginVersion.getCurrent(), pluginVersion.getLatest());

            if (apiVersion.getUpdateAvailable())
                this.logger.warn("API update available ({} -> {}), please update to the latest version", apiVersion.getCurrent(), apiVersion.getLatest());
            if (pluginVersion.getUpdateAvailable())
                this.logger.warn("Plugin update available ({} -> {}), please update to the latest version", pluginVersion.getCurrent(), pluginVersion.getLatest());

            if (compatible) {
                this.logger.info("Plugin and API are compatible, continuing...");
                this.init();

                return;
            }

            this.logger.error("Plugin and API are not compatible, please update to the latest version");
        });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.shutdown();
    }

    public static Lobstar getInstance() {
        return INSTANCE;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    private void init() {
        Sees sees = Sees.getInstance();

        this.logger.info("<Init> Initializing socket manager");
        SocketManager socketManager = SocketManager.getInstance();

        socketManager.addListener(new SyncMotdsListener());
        socketManager.addListener(new MotdCreatedListener());
        socketManager.addListener(new MotdUpdatedListener());
        socketManager.addListener(new MotdDeletedListener());

        socketManager.addListener(new KickPlayerListener());
        socketManager.addListener(new KickAllPlayersListener());
        socketManager.addListener(new MovePlayerListener());
        socketManager.addListener(new MoveAllPlayersListener());

        socketManager.addListener(new SyncServersListener());
        socketManager.addListener(new ServerCreatedListener());
        socketManager.addListener(new ServerUpdatedListener());
        socketManager.addListener(new ServerDeletedListener());

        socketManager.addListener(new SyncServerTypesListener());
        socketManager.addListener(new ServerTypeCreatedListener());
        socketManager.addListener(new ServerTypeUpdatedListener());
        socketManager.addListener(new ServerTypeDeletedListener());

        socketManager.addListener(new SyncVirtualHostsListener());
        socketManager.addListener(new VirtualHostCreatedListener());
        socketManager.addListener(new VirtualHostUpdatedListener());
        socketManager.addListener(new VirtualHostDeletedListener());

        socketManager.addListener(new SyncWhitelistEntriesListener());
        socketManager.addListener(new WhitelistEntryCreatedListener());
        socketManager.addListener(new WhitelistEntryUpdatedListener());
        socketManager.addListener(new WhitelistEntryDeletedListener());

        this.logger.info("<Init> Registering Sees listeners");
        sees.subscribe(socketManager);
        sees.subscribe(MotdManager.getInstance());
        sees.subscribe(ServerTypeManager.getInstance());
        sees.subscribe(ServerManager.getInstance());
        sees.subscribe(VirtualHostManager.getInstance());
        sees.subscribe(WhitelistManager.getInstance());
        sees.subscribe(StatusManager.getInstance());

        sees.subscribe(new PlayerKickListener());
        sees.subscribe(new PlayerMoveListener());

        this.logger.info("<Init> Registering Velocity listeners");
        EventManager eventManager = this.proxy.getEventManager();
        eventManager.register(this, new ConnectionListener());
        eventManager.register(this, new ProxyPingListener());
        eventManager.register(this, new WhitelistListener());

        this.logger.info("<Init> Registering commands");
        CommandManager commandManager = this.proxy.getCommandManager();
        commandManager.register(commandManager.metaBuilder("lobby").aliases("hub", "l").plugin(this).build(), new LobbyCommand().lobbyCommand);

        this.logger.info("<Init> Connecting to socket. This may take a second...");
        socketManager.connect().subscribe(() -> {
            this.logger.info("<Init> Connected to socket, starting sync task");
            StatusManager.getInstance().startSyncTask();
        }, throwable -> {
            this.logger.error("<Init> Failed to connect to socket");
        });
    }

    private void shutdown() {
        this.logger.info("<Shutdown> Cancelling sync task");
        StatusManager.getInstance().cancelSyncTask();

        this.logger.info("<Shutdown> Unsubscribing from Sees");
        Sees sees = Sees.getInstance();
        sees.unsubscribe(SocketManager.getInstance());
        sees.unsubscribe(ServerTypeManager.getInstance());
        sees.unsubscribe(ServerManager.getInstance());
        sees.unsubscribe(VirtualHostManager.getInstance());
        sees.unsubscribe(WhitelistManager.getInstance());
        sees.unsubscribe(StatusManager.getInstance());

        this.logger.info("<Shutdown> Shutting down executor service");
        AsyncRequest.shutdown();

        this.logger.info("<Shutdown> Disconnecting from socket");
        boolean success = SocketManager.getInstance().disconnect().blockingAwait(30, TimeUnit.SECONDS);

        if (success) this.logger.info("<Shutdown> Disconnected from socket");
        else this.logger.error("<Shutdown> Failed to disconnect from socket");
    }
}
