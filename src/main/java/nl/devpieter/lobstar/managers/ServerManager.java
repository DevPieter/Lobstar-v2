package nl.devpieter.lobstar.managers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.server.ServerStatus;
import nl.devpieter.lobstar.socket.SocketManager;
import nl.devpieter.lobstar.socket.events.server.ServerUpdatedEvent;
import nl.devpieter.lobstar.socket.events.server.SyncServersEvent;
import nl.devpieter.lobstar.utils.ServerUtils;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerManager implements Listener {

    private final List<Server> servers = new ArrayList<>();

    private final SocketManager socketManager;
    private final ProxyServer proxy;
    private final Logger logger;

    public ServerManager(@NotNull Lobstar lobstar) {
        this.socketManager = lobstar.getSocketManager();
        this.proxy = lobstar.getProxy();
        this.logger = lobstar.getLogger();
    }

    public List<Server> getServers(ServerType type) {
        return servers.stream().filter(server -> server.getType() == type).toList();
    }

    public @Nullable Server getServer(RegisteredServer registeredServer) {
        return getServer(registeredServer.getServerInfo().getName());
    }

    public @Nullable Server getServer(ServerConnection serverConnection) {
        return getServer(serverConnection.getServerInfo().getName());
    }

    public @Nullable Server getServer(String name) {
        return servers.stream().filter(server -> server.name().equals(name)).findFirst().orElse(null);
    }

    public @Nullable Server getServer(UUID serverId) {
        return servers.stream().filter(server -> server.id().equals(serverId)).findFirst().orElse(null);
    }

    public CompletableFuture<Void> syncRemoteServerStatus() {
        return setRemoteServerStatus(servers);
    }

    public CompletableFuture<Void> setRemoteServerStatus(List<Server> servers) {
        return CompletableFuture.allOf(
                servers.stream()
                        .map(this::setRemoteServerStatus)
                        .toArray(CompletableFuture[]::new)
        );
    }

    public CompletableFuture<Void> setRemoteServerStatus(Server server) {
        RegisteredServer registered = server.findRegisteredServer();
        if (registered == null) {
            ServerStatus status = new ServerStatus(false, 0);
            socketManager.send("SetServerStatus", server.id(), status);

            return CompletableFuture.completedFuture(null);
        }

        int playerCount = ServerUtils.getPlayerCount(registered);
        boolean online = (playerCount > 0) || ServerUtils.isOnline(registered, Duration.ofSeconds(5));

        ServerStatus status = new ServerStatus(online, playerCount);
        socketManager.send("SetServerStatus", server.id(), status);

        return CompletableFuture.completedFuture(null);
    }

    @EventListener
    public void onSyncServers(SyncServersEvent event) {
        logger.info("[SyncServersEvent] Syncing servers");

        // Unregister all servers
        logger.info("[SyncServersEvent] Unregistering all servers");
        servers.clear();
        proxy.getAllServers().forEach(server -> proxy.unregisterServer(server.getServerInfo()));

        // Register all servers
        logger.info("[SyncServersEvent] Registering all servers");
        event.servers().forEach(this::registerServer);

        logger.info("[SyncServersEvent] Synced servers");

        syncRemoteServerStatus().thenRun(() -> logger.info("Synced remote server status"));
    }

    @EventListener
    public void onServerUpdated(ServerUpdatedEvent event) {
        Server existingServer = servers.stream().filter(s -> s.id().equals(event.serverId())).findFirst().orElse(null);
        Server server = event.server();

        if (existingServer == null && server != null) {
            logger.info("[ServerUpdatedEvent] Adding server: {}", server.name());
            registerServer(server);
        } else if (existingServer != null && server != null) {
            logger.info("[ServerUpdatedEvent] Updating server: {}", server.name());

            boolean criticalChange = !existingServer.name().equals(server.name()) ||
                    !existingServer.address().equals(server.address()) ||
                    existingServer.port() != server.port();

            if (criticalChange) {
                logger.info("[ServerUpdatedEvent] Critical change detected, re-registering server");

                ServerUtils.kickAllPlayers(existingServer, Component.text("Server is restarting."));
                unregisterServer(existingServer);
                registerServer(server);
            } else {
                logger.info("[ServerUpdatedEvent] Non-critical change detected, updating server");
                updateServer(event.serverId(), server);
            }
        } else if (existingServer != null) {
            logger.info("[ServerUpdatedEvent] Removing server: {}", existingServer.name());

            ServerUtils.kickAllPlayers(existingServer, Component.text("Server is restarting."));
            unregisterServer(existingServer);
        }
    }

    private void registerServer(@NotNull Server server) {
        InetSocketAddress address = new InetSocketAddress(server.address(), server.port());
        ServerInfo serverInfo = new ServerInfo(server.name(), address);

        proxy.registerServer(serverInfo);
        servers.add(server);
    }

    private void updateServer(@NotNull UUID serverId, @NotNull Server server) {
        Server existingServer = servers.stream().filter(s -> s.id().equals(serverId)).findFirst().orElse(null);
        if (existingServer == null) return;

        existingServer.setPrefix(server.prefix());
        existingServer.setDisplayName(server.displayName());

        existingServer.setType(server.type());
        existingServer.setWhitelistEnabled(server.isWhitelistEnabled());
    }

    private void unregisterServer(@NotNull Server server) {
        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return;

        proxy.unregisterServer(registeredServer.getServerInfo());
        servers.remove(server);
    }
}
