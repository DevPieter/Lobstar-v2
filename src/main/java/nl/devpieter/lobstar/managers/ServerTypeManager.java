package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.serverType.ServerType;
import nl.devpieter.lobstar.socket.events.server.type.ServerTypeCreatedEvent;
import nl.devpieter.lobstar.socket.events.server.type.ServerTypeDeletedEvent;
import nl.devpieter.lobstar.socket.events.server.type.ServerTypeUpdatedEvent;
import nl.devpieter.lobstar.socket.events.server.type.SyncServerTypesEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerTypeManager implements Listener {

    private static ServerTypeManager INSTANCE;

    private final List<ServerType> serverTypes = new ArrayList<>();

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private ServerTypeManager() {
    }

    public static ServerTypeManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ServerTypeManager();
        return INSTANCE;
    }

    public List<ServerType> getServerTypes() {
        return this.serverTypes;
    }

    public List<ServerType> getLobbyLikeServerTypes() {
        return this.serverTypes.stream().filter(ServerType::isLobbyLike).toList();
    }

    public ServerType getServerTypeById(UUID serverTypeId) {
        return this.serverTypes.stream().filter(serverType -> serverType.getId().equals(serverTypeId)).findFirst().orElse(null);
    }

    @EventListener
    public void onSyncServerTypes(SyncServerTypesEvent event) {
        this.logger.info("[ServerTypeManager] <Sync> Syncing server types");
        this.serverTypes.clear();

        List<ServerType> serverTypes = event.serverTypes().stream().toList();
        this.serverTypes.addAll(serverTypes);

        this.logger.info("[ServerTypeManager] <Sync> Successfully synced {} server types", this.serverTypes.size());
    }

    @EventListener
    public void onServerTypeCreated(ServerTypeCreatedEvent event) {
        ServerType created = event.serverType();
        ServerType existingId = this.getServerTypeById(event.serverTypeId());

        if (existingId != null) {
            this.logger.warn("[ServerTypeManager] <Create> Tried to create server type {}, but a server type with the same ID already exists!", created.getName());
            return;
        }

        this.serverTypes.add(created);
        this.logger.info("[ServerTypeManager] <Create> Server type created: {} ({})", created.getName(), event.serverTypeId());
    }

    @EventListener
    public void onServerTypeUpdated(ServerTypeUpdatedEvent event) {
        ServerType existing = this.getServerTypeById(event.serverTypeId());
        if (existing == null) {
            this.logger.warn("[ServerTypeManager] <Update> Tried to update server type {}, but it was not found!", event.serverType().getName());
            return;
        }

        ServerType updated = event.serverType();

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());

        existing.setActive(updated.isActive());
        existing.setLobbyLike(updated.isLobbyLike());

        this.logger.info("[ServerTypeManager] <Update> Server type updated: {} ({})", updated.getName(), updated.getId());
    }

    @EventListener
    public void onServerTypeDeleted(ServerTypeDeletedEvent event) {
        ServerType existing = this.getServerTypeById(event.serverTypeId());
        if (existing == null) {
            this.logger.warn("[ServerTypeManager] <Delete> Tried to delete server type {}, but it was not found!", event.serverTypeId());
            return;
        }

        this.serverTypes.remove(existing);
        this.logger.info("[ServerTypeManager] <Delete> Server type deleted: {} ({})", existing.getName(), event.serverTypeId());
    }
}
