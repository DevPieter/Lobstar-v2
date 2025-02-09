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
import nl.devpieter.lobstar.listeners.ConnectionListener;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.socket.listeners.ServerUpdatedListener;
import nl.devpieter.lobstar.socket.listeners.SyncServersListener;
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
    private ServerManager serverManager;
    private WhitelistManager whitelistManager;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        Sees sees = Sees.getInstance();

        logger.info("Initializing socket manager");
        socketManager = new SocketManager(this);
        sees.subscribe(socketManager);

        socketManager.addListener(new SyncServersListener());
        socketManager.addListener(new ServerUpdatedListener());

        logger.info("Initializing server manager");
        serverManager = new ServerManager(this);
        sees.subscribe(serverManager);

        logger.info("initializing whitelist manager");
        whitelistManager = new WhitelistManager();
        sees.subscribe(whitelistManager);

        logger.info("Registering listeners");
        EventManager eventManager = proxy.getEventManager();
        eventManager.register(this, new ConnectionListener());

        logger.info("Registering commands");
        CommandManager commandManager = proxy.getCommandManager();
        commandManager.register(commandManager.metaBuilder("lobby").aliases("hub", "l").plugin(this).build(), new LobbyCommand().lobbyCommand);

        logger.info("Connecting to socket");
        socketManager.connect().subscribe(() -> logger.info("Connected to socket"), throwable -> logger.error("Failed to connect to socket"));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Unsubscribing from Sees");
        Sees sees = Sees.getInstance();
        sees.unsubscribe(socketManager);
        sees.unsubscribe(serverManager);
        sees.unsubscribe(whitelistManager);

        logger.info("Disconnecting from socket");
        socketManager.disconnect().subscribe(() -> logger.info("Disconnected from socket"), throwable -> logger.error("Failed to disconnect from socket"));

        logger.info("Shutting down executor service");
        AsyncRequest.shutdown();
    }

    public static Lobstar getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public SocketManager getSocketManager() {
        return socketManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
}
