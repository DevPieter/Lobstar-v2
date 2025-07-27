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
        return this.motds.stream().filter(motd -> motd.getId().equals(motdId)).findFirst().orElse(null);
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
            this.logger.warn("[MotdManager] <Create> Tried to create MOTD {}, but a MOTD with the same ID already exists!", created.getName());
            return;
        }

        this.motds.add(created);
        this.logger.info("[MotdManager] <Create> MOTD created: {} ({})", created.getName(), event.motdId());
    }

    @EventListener
    public void onMotdUpdated(MotdUpdatedEvent event) {
        Motd existing = this.getMotdById(event.motdId());
        if (existing == null) {
            this.logger.warn("[MotdManager] <Update> Tried to update MOTD {}, but it was not found!", event.motd().getName());
            return;
        }

        Motd updated = event.motd();

        existing.setName(updated.getName());

        existing.setOnlinePlayersGetType(updated.getOnlinePlayersGetType());
        existing.setOnlinePlayers(updated.getOnlinePlayers());
        existing.setOnlinePlayersEnabled(updated.getOnlinePlayersEnabled());

        existing.setMaximumPlayersGetType(updated.getMaximumPlayersGetType());
        existing.setMaximumPlayers(updated.getMaximumPlayers());

        existing.setSamplePlayersGetType(updated.getSamplePlayersGetType());
        existing.setSamplePlayers(updated.getSamplePlayers());
        existing.setSamplePlayersEnabled(updated.getSamplePlayersEnabled());

        existing.setDescriptionGetType(updated.getDescriptionGetType());
        existing.setDescription(updated.getDescription());
        existing.setDescriptionEnabled(updated.getDescriptionEnabled());

        existing.setFaviconGetType(updated.getFaviconGetType());
        existing.setFavicon(updated.getFavicon());
        existing.setFaviconEnabled(updated.getFaviconEnabled());

        this.logger.info("[MotdManager] <Update> MOTD updated: {} ({})", updated.getName(), updated.getId());
    }

    @EventListener
    public void onMotdDeleted(MotdDeletedEvent event) {
        Motd existing = this.getMotdById(event.motdId());
        if (existing == null) {
            this.logger.warn("[MotdManager] <Delete> Tried to delete MOTD {}, but it was not found!", event.motdId());
            return;
        }

        this.motds.remove(existing);
        this.logger.info("[MotdManager] <Delete> MOTD deleted: {} ({})", existing.getName(), event.motdId());
    }
}
