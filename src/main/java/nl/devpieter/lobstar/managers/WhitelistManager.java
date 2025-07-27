package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.socket.events.whitelist.SyncWhitelistEntriesEvent;
import nl.devpieter.lobstar.socket.events.whitelist.WhitelistEntryCreatedEvent;
import nl.devpieter.lobstar.socket.events.whitelist.WhitelistEntryDeletedEvent;
import nl.devpieter.lobstar.socket.events.whitelist.WhitelistEntryUpdatedEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager implements Listener {

    private static WhitelistManager INSTANCE;

    // Although the WhitelistEntries are normally not stored separately, we do it here for faster lookups.
    private final HashMap<UUID, WhitelistEntry> entryIndex = new HashMap<>(); // [entryId] -> WhitelistEntry
    private final HashMap<UUID, WhitelistEntry> globalEntries = new HashMap<>(); // [playerId] -> WhitelistEntry
    private final HashMap<UUID, HashMap<UUID, WhitelistEntry>> serverEntries = new HashMap<>(); // [serverId] -> [playerId] -> WhitelistEntry

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private WhitelistManager() {
    }

    public static WhitelistManager getInstance() {
        if (INSTANCE == null) INSTANCE = new WhitelistManager();
        return INSTANCE;
    }

    public @Nullable WhitelistEntry getEntryById(UUID entryId) {
        return this.entryIndex.get(entryId);
    }

    public @Nullable WhitelistEntry getGlobalEntry(UUID playerId) {
        return this.globalEntries.get(playerId);
    }

    public @Nullable WhitelistEntry getServerEntry(UUID serverId, UUID playerId) {
        Map<UUID, WhitelistEntry> entries = this.serverEntries.get(serverId);
        if (entries == null) return null;

        return entries.get(playerId);
    }

    @EventListener
    public void onSyncWhitelist(SyncWhitelistEntriesEvent event) {
        this.logger.info("[WhitelistManager] <Sync> Syncing whitelist");

        this.entryIndex.clear();
        this.globalEntries.clear();
        this.serverEntries.clear();

        for (WhitelistEntry entry : event.entries()) {
            this.entryIndex.put(entry.getId(), entry);

            if (entry.getServerId() == null) this.globalEntries.put(entry.getPlayerId(), entry);
            else this.serverEntries.computeIfAbsent(entry.getServerId(), k -> new HashMap<>()).put(entry.getPlayerId(), entry);
        }

        this.logger.info("[WhitelistManager] <Sync> Whitelist synced, {} global entries, {} server entries", this.globalEntries.size(), this.serverEntries.size());
    }

    @EventListener
    public void onWhitelistEntryCreated(WhitelistEntryCreatedEvent event) {
        WhitelistEntry created = event.entry();
        WhitelistEntry existingId = this.getEntryById(created.getId());

        if (existingId != null) {
            this.logger.warn("[WhitelistManager] <Create> Tried to create whitelist entry for player {}, but an entry with the same ID already exists!", created.getPlayerId());
            return;
        }

        this.entryIndex.put(created.getId(), created);

        if (created.getServerId() == null) {
            this.globalEntries.put(created.getPlayerId(), created);
            this.logger.info("[WhitelistManager] <Create> Global whitelist entry created for player {} ({})", created.getPlayerId(), created.getId());
        } else {
            this.serverEntries.computeIfAbsent(created.getServerId(), k -> new HashMap<>()).put(created.getPlayerId(), created);
            this.logger.info("[WhitelistManager] <Create> Server whitelist entry created for player {} on server {} ({})", created.getPlayerId(), created.getServerId(), created.getId());
        }
    }

    @EventListener
    public void onWhitelistEntryUpdated(WhitelistEntryUpdatedEvent event) {
        WhitelistEntry existing = this.getEntryById(event.entryId());
        if (existing == null) {
            this.logger.warn("[WhitelistManager] <Update> Tried to update whitelist entry for player {}, but it was not found!", event.entry().getPlayerId());
            return;
        }

        WhitelistEntry updated = event.entry();

        existing.setWhitelisted(updated.isWhitelisted());
        existing.setSuperEntry(updated.isSuperEntry());

        existing.setHasExpiration(updated.getHasExpiration());
        existing.setExpirationDate(updated.getExpirationDate());

        this.logger.info("[WhitelistManager] <Update> Whitelist entry updated for player {} ({})", updated.getPlayerId(), updated.getId());
    }

    @EventListener
    public void onWhitelistEntryDeleted(WhitelistEntryDeletedEvent event) {
        WhitelistEntry existing = this.getEntryById(event.entryId());
        if (existing == null) {
            this.logger.warn("[WhitelistManager] <Delete> Tried to delete whitelist entry {}, but it was not found!", event.entryId());
            return;
        }

        this.entryIndex.remove(existing.getId());

        if (existing.getServerId() == null) {
            this.globalEntries.remove(existing.getPlayerId());
        } else {
            Map<UUID, WhitelistEntry> entries = this.serverEntries.get(existing.getServerId());
            if (entries != null) entries.remove(existing.getPlayerId());
        }

        this.logger.info("[WhitelistManager] <Delete> Whitelist entry deleted for player {} ({})", existing.getPlayerId(), existing.getId());
    }
}
