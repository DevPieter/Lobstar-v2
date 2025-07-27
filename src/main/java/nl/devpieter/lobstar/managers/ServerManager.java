package nl.devpieter.lobstar.managers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.events.server.RegisteredServerDeletedEvent;
import nl.devpieter.lobstar.events.server.RegisteredServerRegisteredEvent;
import nl.devpieter.lobstar.events.server.RegisteredServerUpdatedEvent;
import nl.devpieter.lobstar.events.server.RegisteredServersSyncedEvent;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.events.server.ServerCreatedEvent;
import nl.devpieter.lobstar.socket.events.server.ServerDeletedEvent;
import nl.devpieter.lobstar.socket.events.server.ServerUpdatedEvent;
import nl.devpieter.lobstar.socket.events.server.SyncServersEvent;
import nl.devpieter.lobstar.utils.ServerUtils;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import nl.devpieter.sees.Sees;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerManager implements Listener {

    private static ServerManager INSTANCE;

    private final List<Server> servers = new ArrayList<>();

    private final Sees sees = Sees.getInstance();
    private final Lobstar lobstar = Lobstar.getInstance();

    private final ProxyServer proxy = this.lobstar.getProxy();
    private final Logger logger = this.lobstar.getLogger();

    private ServerManager() {
    }

    public static ServerManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ServerManager();
        return INSTANCE;
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public @Nullable Server getServer(@NotNull RegisteredServer registeredServer) {
        return this.getServerByName(registeredServer.getServerInfo().getName());
    }

    public @Nullable Server getServer(@NotNull ServerConnection serverConnection) {
        return this.getServerByName(serverConnection.getServerInfo().getName());
    }

    public @Nullable Server getServerByName(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        return this.servers.stream().filter(server -> server.getName().equals(name)).findFirst().orElse(null);
    }

    public @Nullable Server getServerById(@Nullable UUID serverId) {
        if (serverId == null) return null;
        return this.servers.stream().filter(server -> server.getId().equals(serverId)).findFirst().orElse(null);
    }

    public @NotNull List<Server> getServersByTypeId(@Nullable UUID typeId) {
        if (typeId == null) return new ArrayList<>();
        return this.servers.stream().filter(server -> server.getTypeId().equals(typeId)).toList();
    }

    @EventListener
    public void onSyncServers(SyncServersEvent event) {
        this.logger.info("[ServerManager] <Sync> Syncing servers, unregistering first");

        this.servers.clear();
        this.proxy.getAllServers().forEach(server -> this.proxy.unregisterServer(server.getServerInfo()));

        event.servers().forEach(this::registerServer);

        this.sees.call(new RegisteredServersSyncedEvent());
        this.logger.info("[ServerManager] <Sync> Successfully synced {} servers", event.servers().size());
    }

    @EventListener
    public void onServerCreated(ServerCreatedEvent event) {
        Server created = event.server();
        Server existingId = this.getServerById(event.serverId());
        Server existingName = this.getServerByName(created.getName());

        if (existingId != null || existingName != null) {
            this.logger.warn("[ServerManager] <Create> Tried to register server {}, but a server with the same ID or name already exists!", created.getName());
            return;
        }

        this.logger.info("[ServerManager] <Create> Registering server {} with ID {}", created.getName(), event.serverId());
        this.registerServer(created);
    }

    @EventListener
    public void onServerUpdated(ServerUpdatedEvent event) {
        Server existing = this.getServerById(event.serverId());
        if (existing == null) {
            this.logger.warn("[ServerManager] <Update> Tried to update server {}, but it was not found!", event.server().getName());
            return;
        }

        if (existing.isCriticallyDifferent(event.server())) {
            this.logger.info("[ServerManager] <Update> Critical change detected, re-registering server {} with ID {}", event.server().getName(), event.serverId());
            ServerUtils.kickAllPlayers(existing, Component.text("Server is restarting, please reconnect."));

            this.unregisterServer(existing, false);
            this.registerServer(event.server());
        } else {
            this.logger.info("[ServerManager] <Update> Non-critical change detected, updating server {} with ID {}", event.server().getName(), event.serverId());
            this.updateServer(event.serverId(), event.server());
        }
    }

    @EventListener
    public void onServerDeleted(ServerDeletedEvent event) {
        Server existing = this.getServerById(event.serverId());
        if (existing == null) {
            this.logger.warn("[ServerManager] <Delete> Tried to delete server {}, but it was not found!", event.serverId());
            return;
        }

        this.logger.info("[ServerManager] <Delete> Removing server {} with ID {}", existing.getName(), event.serverId());

        ServerUtils.kickAllPlayers(existing, Component.text("Server is shutting down."));
        this.unregisterServer(existing, true);
    }

    private void registerServer(@NotNull Server server) {
        InetSocketAddress address = new InetSocketAddress(server.getAddress(), server.getPort());
        ServerInfo serverInfo = new ServerInfo(server.getName(), address);

        RegisteredServer registeredServer = this.proxy.registerServer(serverInfo);
        if (registeredServer == null) {
            this.logger.error("[ServerManager] <Register> Tried to register server {}, but something went wrong!", server.getName());
            return;
        }

        this.servers.add(server);

        this.sees.call(new RegisteredServerRegisteredEvent(server.getId()));
        this.logger.info("[ServerManager] <Register> Successfully registered server {} with ID {}", server.getName(), server.getId());
    }

    private void updateServer(@NotNull UUID serverId, @NotNull Server server) {
        Server existingServer = this.getServerById(serverId);
        if (existingServer == null) return;

        existingServer.setDisplayName(server.getDisplayName());

        existingServer.setTypeId(server.getTypeId());

        existingServer.setListed(server.isListed());
        existingServer.setJoinable(server.isJoinable());
        existingServer.setWhitelistActive(server.isWhitelistActive());

        existingServer.setUnderMaintenance(server.isUnderMaintenance());
        existingServer.setMaintenanceMessage(server.getMaintenanceMessage());

        this.sees.call(new RegisteredServerUpdatedEvent(serverId));
        this.logger.info("[ServerManager] <Update> Successfully updated server {} with ID {}", existingServer.getName(), serverId);
    }

    private void unregisterServer(@NotNull Server server, boolean permanently) {
        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return;

        proxy.unregisterServer(registeredServer.getServerInfo());
        servers.remove(server);

        this.sees.call(new RegisteredServerDeletedEvent(server.getId(), permanently));
        this.logger.info("[ServerManager] <Unregister> Successfully unregistered server {} with ID {}", server.getName(), server.getTypeId());
    }
}
