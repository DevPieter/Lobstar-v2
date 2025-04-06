package nl.devpieter.lobstar.managers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.events.server.ServerCreatedEvent;
import nl.devpieter.lobstar.socket.events.server.ServerDeletedEvent;
import nl.devpieter.lobstar.socket.events.server.ServerUpdatedEvent;
import nl.devpieter.lobstar.socket.events.server.SyncServersEvent;
import nl.devpieter.lobstar.utils.ServerUtils;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerManager implements Listener {

    private final List<Server> servers = new ArrayList<>();

    private final StatusManager statusManager;
    private final ProxyServer proxy;
    private final Logger logger;

    public ServerManager(@NotNull Lobstar lobstar) {
        this.statusManager = lobstar.getStatusManager();
        this.proxy = lobstar.getProxy();
        this.logger = lobstar.getLogger();
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public List<Server> getServers(ServerType type) {
        return this.servers.stream().filter(server -> server.getType() == type).toList();
    }

    public @Nullable Server getServer(@NotNull RegisteredServer registeredServer) {
        return this.getServer(registeredServer.getServerInfo().getName());
    }

    public @Nullable Server getServer(@NotNull ServerConnection serverConnection) {
        return this.getServer(serverConnection.getServerInfo().getName());
    }

    public @Nullable Server getServer(String name) {
        return this.servers.stream().filter(server -> server.name().equals(name)).findFirst().orElse(null);
    }

    public @Nullable Server getServer(UUID serverId) {
        return this.servers.stream().filter(server -> server.id().equals(serverId)).findFirst().orElse(null);
    }

    @EventListener
    public void onSyncServers(SyncServersEvent event) {
        this.logger.info("[SyncServersEvent] Syncing servers");

        // Unregister all servers
        this.logger.info("[SyncServersEvent] Unregistering all servers");
        this.servers.clear();
        this.proxy.getAllServers().forEach(server -> this.proxy.unregisterServer(server.getServerInfo()));

        // Register all servers
        this.logger.info("[SyncServersEvent] Registering all servers");
        event.servers().forEach(this::registerServer);

        this.logger.info("[SyncServersEvent] Synced servers");
    }

    @EventListener
    public void onServerCreated(ServerCreatedEvent event) {
        Server existingId = this.getServer(event.serverId());
        Server existingName = this.getServer(event.server().name());

        if (existingId != null || existingName != null) {
            this.logger.warn("[ServerCreatedEvent] Server already exists: {} ({})", event.server().name(), event.serverId());
            return;
        }

        this.logger.info("[ServerCreatedEvent] Registering server: {} ({})", event.server().name(), event.serverId());
        this.registerServer(event.server());
    }

    @EventListener
    public void onServerUpdated(ServerUpdatedEvent event) {
        Server existing = this.getServer(event.serverId());
        if (existing == null) {
            this.logger.warn("[ServerUpdatedEvent] Server not found: {} ({})", event.server().name(), event.serverId());
            return;
        }

        if (existing.isCriticallyDifferent(event.server())) {
            this.logger.info("[ServerUpdatedEvent] Critical change detected, re-registering server: {} ({})", event.server().name(), event.serverId());
            ServerUtils.kickAllPlayers(existing, Component.text("Server is restarting, please reconnect."));

            this.unregisterServer(existing);
            this.registerServer(event.server());
        } else {
            this.logger.info("[ServerUpdatedEvent] Non-critical change detected, updating server: {} ({})", event.server().name(), event.serverId());
            this.updateServer(event.serverId(), event.server());
        }
    }

    @EventListener
    public void onServerDeleted(ServerDeletedEvent event) {
        Server existing = this.getServer(event.serverId());
        if (existing == null) {
            this.logger.warn("[ServerDeletedEvent] Server not found: {}", event.serverId());
            return;
        }

        this.logger.info("[ServerDeletedEvent] Removing server: {} ({})", existing.name(), event.serverId());
        ServerUtils.kickAllPlayers(existing, Component.text("Server is shutting down."));

        this.unregisterServer(existing);
    }

    private void registerServer(@NotNull Server server) {
        InetSocketAddress address = new InetSocketAddress(server.address(), server.port());
        ServerInfo serverInfo = new ServerInfo(server.name(), address);

        this.proxy.registerServer(serverInfo);
        this.servers.add(server);

        this.statusManager.setServerStatus(server);
        this.logger.info("[ServerManager] Registered server: {} ({})", server.name(), server.address());
    }

    private void updateServer(@NotNull UUID serverId, @NotNull Server server) {
        Server existingServer = servers.stream().filter(s -> s.id().equals(serverId)).findFirst().orElse(null);
        if (existingServer == null) return;

        existingServer.setPrefix(server.prefix());
        existingServer.setDisplayName(server.displayName());

        existingServer.setType(server.type());
        existingServer.setWhitelistEnabled(server.isWhitelistEnabled());

        this.statusManager.setServerStatus(existingServer);
        this.logger.info("[ServerManager] Updated server: {} ({})", existingServer.name(), existingServer.address());
    }

    private void unregisterServer(@NotNull Server server) {
        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return;

        proxy.unregisterServer(registeredServer.getServerInfo());
        servers.remove(server);

        this.statusManager.clearServerStatus(server);
        this.logger.info("[ServerManager] Unregistered server: {} ({})", server.name(), server.address());
    }
}
