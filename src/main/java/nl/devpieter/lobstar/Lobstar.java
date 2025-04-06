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
import nl.devpieter.lobstar.listeners.sees.TestListener;
import nl.devpieter.lobstar.listeners.velocity.ConnectionListener;
import nl.devpieter.lobstar.listeners.velocity.WhitelistListener;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.StatusManager;
import nl.devpieter.lobstar.managers.VersionManager;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.models.version.Version;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.socket.listeners.player.KickAllPlayersListener;
import nl.devpieter.lobstar.socket.listeners.player.KickPlayerListener;
import nl.devpieter.lobstar.socket.listeners.player.MoveAllPlayersListener;
import nl.devpieter.lobstar.socket.listeners.player.MovePlayerListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerCreatedListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerDeletedListener;
import nl.devpieter.lobstar.socket.listeners.server.ServerUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.server.SyncServersListener;
import nl.devpieter.sees.Sees;
import org.slf4j.Logger;

@Plugin(id = "lobstar", name = "Lobstar v2", version = BuildConstants.VERSION, description = "TODO", authors = {"DevPieter"})
public class Lobstar {

    private static Lobstar instance;

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    private SocketManager socketManager;
    private StatusManager statusManager;
    private VersionManager versionManager;
    private ServerManager serverManager;
    private WhitelistManager whitelistManager;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        this.logger.info("Initializing version manager");
        this.versionManager = new VersionManager();

        this.logger.info("Loading version info, and checking compatibility. Please wait...");
        this.versionManager.loadVersionInfo(unused -> {
            if (!this.versionManager.isSuccessfullyLoaded()) {
                this.logger.error("Failed to load version info, cannot continue");
                return;
            }

            this.logger.info("Successfully loaded version info");

            Version apiVersion = this.versionManager.getApiVersion();
            Version pluginVersion = this.versionManager.getPluginVersion();
            boolean compatible = this.versionManager.isCompatible();

            if (apiVersion == null || pluginVersion == null) {
                this.logger.error("API or plugin version is null, cannot continue");
                return;
            }

            this.logger.info("> API Version: {} ({})", apiVersion.current(), apiVersion.latest());
            this.logger.info("> Plugin Version: {} ({})", pluginVersion.current(), pluginVersion.latest());

            if (apiVersion.updateAvailable()) {
                this.logger.warn("API update available ({} -> {}), please update to the latest version", apiVersion.current(), apiVersion.latest());
            }

            if (pluginVersion.updateAvailable()) {
                this.logger.warn("Plugin update available ({} -> {}), please update to the latest version", pluginVersion.current(), pluginVersion.latest());
            }

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
        return instance;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public SocketManager getSocketManager() {
        return this.socketManager;
    }

    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public WhitelistManager getWhitelistManager() {
        return this.whitelistManager;
    }

    private void init() {
        Sees sees = Sees.getInstance();

        this.logger.info("Initializing socket manager");
        this.socketManager = new SocketManager(this);
        sees.subscribe(this.socketManager);

        this.socketManager.addListener(new SyncServersListener());
        this.socketManager.addListener(new ServerCreatedListener());
        this.socketManager.addListener(new ServerUpdatedListener());
        this.socketManager.addListener(new ServerDeletedListener());

        this.socketManager.addListener(new KickPlayerListener());
        this.socketManager.addListener(new KickAllPlayersListener());
        this.socketManager.addListener(new MovePlayerListener());
        this.socketManager.addListener(new MoveAllPlayersListener());

        this.logger.info("Initializing status manager");
        this.statusManager = new StatusManager(this);

        this.logger.info("Initializing server manager");
        this.serverManager = new ServerManager(this);
        sees.subscribe(this.serverManager);

        this.logger.info("Initializing whitelist manager");
        this.whitelistManager = new WhitelistManager();
        sees.subscribe(this.whitelistManager);

        this.logger.info("Registering sees listeners");
        sees.subscribe(new TestListener());

        this.logger.info("Registering velocity listeners");
        EventManager eventManager = this.proxy.getEventManager();
        eventManager.register(this, new ConnectionListener());
        eventManager.register(this, new WhitelistListener());

        this.logger.info("Registering commands");
        CommandManager commandManager = this.proxy.getCommandManager();
        commandManager.register(commandManager.metaBuilder("lobby").aliases("hub", "l").plugin(this).build(), new LobbyCommand().lobbyCommand);

        this.logger.info("Connecting to socket");
        this.socketManager.connect().subscribe(() -> this.logger.info("Connected to socket"), throwable -> this.logger.error("Failed to connect to socket"));
    }

    private void shutdown() {
        this.logger.info("Cancelling sync task");
        this.statusManager.cancelSyncTask();

        this.logger.info("Unsubscribing from Sees");
        Sees sees = Sees.getInstance();
        sees.unsubscribe(this.socketManager);
        sees.unsubscribe(this.serverManager);
        sees.unsubscribe(this.whitelistManager);

        this.logger.info("Disconnecting from socket");
        this.socketManager.disconnect().subscribe(() -> this.logger.info("Disconnected from socket"), throwable -> this.logger.error("Failed to disconnect from socket"));

        this.logger.info("Shutting down executor service");
        AsyncRequest.shutdown();
    }
}
