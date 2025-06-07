package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.lobstar.socket.events.motd.MotdCreatedEvent;
import nl.devpieter.lobstar.socket.events.motd.MotdDeletedEvent;
import nl.devpieter.lobstar.socket.events.motd.MotdUpdatedEvent;
import nl.devpieter.lobstar.socket.events.motd.SyncMotdsEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MotdManager implements Listener {

    private static MotdManager INSTANCE;

    private final List<Motd> motds = new ArrayList<>();

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private MotdManager() {
    }

    public static MotdManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MotdManager();
        return INSTANCE;
    }

    public Motd getMotdById(UUID motdId) {
        return this.motds.stream().filter(motd -> motd.id().equals(motdId)).findFirst().orElse(null);
    }

    @EventListener
    public void onSyncMotds(SyncMotdsEvent event) {
        this.logger.info("[MotdManager] <Sync> Syncing MOTDs");
        this.motds.clear();

        List<Motd> motds = event.motds().stream().toList();
        this.motds.addAll(motds);

        this.logger.info("[MotdManager] <Sync> Successfully synced {} MOTDs", this.motds.size());
    }

    @EventListener
    public void onMotdCreated(MotdCreatedEvent event) {
        Motd created = event.motd();
        Motd existingId = this.getMotdById(event.motdId());

        if (existingId != null) {
            this.logger.warn("[MotdManager] <Create> Tried to create MOTD {}, but a MOTD with the same ID already exists!", created.name());
            return;
        }

        this.motds.add(created);
        this.logger.info("[MotdManager] <Create> MOTD created: {} ({})", created.name(), event.motdId());
    }

    @EventListener
    public void onMotdUpdated(MotdUpdatedEvent event) {
        Motd existing = this.getMotdById(event.motdId());
        if (existing == null) {
            this.logger.warn("[MotdManager] <Update> Tried to update MOTD {}, but it was not found!", event.motd().name());
            return;
        }

        Motd updated = event.motd();

        existing.setName(updated.name());

        existing.setOnlinePlayersGetType(updated.onlinePlayersGetType());
        existing.setOnlinePlayers(updated.onlinePlayers());
        existing.setOnlinePlayersEnabled(updated.onlinePlayersEnabled());

        existing.setMaximumPlayersGetType(updated.maximumPlayersGetType());
        existing.setMaximumPlayers(updated.maximumPlayers());

        existing.setSamplePlayersGetType(updated.samplePlayersGetType());
        existing.setSamplePlayers(updated.samplePlayers());
        existing.setSamplePlayersEnabled(updated.samplePlayersEnabled());

        existing.setDescriptionGetType(updated.descriptionGetType());
        existing.setDescription(updated.description());
        existing.setDescriptionEnabled(updated.descriptionEnabled());

        existing.setFaviconGetType(updated.faviconGetType());
        existing.setFavicon(updated.favicon());
        existing.setFaviconEnabled(updated.faviconEnabled());

        System.out.println(existing.description());

        this.logger.info("[MotdManager] <Update> MOTD updated: {} ({})", updated.name(), updated.id());
    }

    @EventListener
    public void onMotdDeleted(MotdDeletedEvent event) {
        Motd existing = this.getMotdById(event.motdId());
        if (existing == null) {
            this.logger.warn("[MotdManager] <Delete> Tried to delete MOTD {}, but it was not found!", event.motdId());
            return;
        }

        this.motds.remove(existing);
        this.logger.info("[MotdManager] <Delete> MOTD deleted: {} ({})", existing.name(), event.motdId());
    }
}
