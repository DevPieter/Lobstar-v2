package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.socket.events.virtualHost.SyncVirtualHostsEvent;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostCreatedEvent;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostDeletedEvent;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostUpdatedEvent;
import nl.devpieter.sees.Annotations.EventListener;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualHostManager implements Listener {

    private static VirtualHostManager INSTANCE;

    private final List<VirtualHost> virtualHosts = new ArrayList<>();

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private VirtualHostManager() {
    }

    public static VirtualHostManager getInstance() {
        if (INSTANCE == null) INSTANCE = new VirtualHostManager();
        return INSTANCE;
    }

    public List<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public VirtualHost getVirtualHostById(UUID virtualHostId) {
        return this.virtualHosts.stream().filter(virtualHost -> virtualHost.id().equals(virtualHostId)).findFirst().orElse(null);
    }

    public @Nullable VirtualHost findMatchingVirtualHost(@NotNull String hostname) {
        for (VirtualHost virtualHost : this.virtualHosts) {
            if (!virtualHost.isEnabled() || !virtualHost.compare(hostname)) continue;
            return virtualHost;
        }

        return null;
    }

    @EventListener
    public void onSyncVirtualHosts(SyncVirtualHostsEvent event) {
        this.logger.info("[VirtualHostManager] <Sync> Syncing virtual hosts");
        this.virtualHosts.clear();

        List<VirtualHost> enabledVirtualHosts = event.virtualHosts().stream().filter(VirtualHost::isEnabled).toList();
        this.virtualHosts.addAll(enabledVirtualHosts);

        this.logger.info("[VirtualHostManager] <Sync> Successfully synced {} virtual hosts", this.virtualHosts.size());
    }

    @EventListener
    public void onVirtualHostCreated(VirtualHostCreatedEvent event) {
        VirtualHost created = event.virtualHost();
        VirtualHost existingId = this.getVirtualHostById(event.virtualHostId());

        if (existingId != null) {
            this.logger.warn("[VirtualHostManager] <Create> Tried to create virtual host {}, but a virtual host with the same ID already exists!", created.hostname());
            return;
        }

        if (!created.isEnabled()) {
            this.logger.info("[VirtualHostManager] <Create> Tried to create virtual host {}, but it is not enabled, skipping.", created.hostname());
            return;
        }

        this.virtualHosts.add(event.virtualHost());
        this.logger.info("[VirtualHostManager] <Create> Virtual host created: {} ({})", created.hostname(), event.virtualHostId());
    }

    @EventListener
    public void onVirtualHostUpdated(VirtualHostUpdatedEvent event) {
        VirtualHost existing = this.getVirtualHostById(event.virtualHostId());
        if (existing == null) {
            this.logger.warn("[VirtualHostManager] <Update> Tried to update virtual host {}, but it was not found!", event.virtualHost().hostname());
            return;
        }

        VirtualHost updated = event.virtualHost();

        existing.setHostname(updated.hostname());
        existing.setIgnoreCase(updated.ignoreCase());
        existing.setCheckType(updated.checkType());
        existing.setDefault(updated.isDefault());
        existing.setEnabled(updated.isEnabled()); // TODO - Should we remove/create the virtual host instead?

        this.logger.info("[VirtualHostManager] <Update> Virtual host updated: {} ({})", updated.hostname(), updated.id());
    }

    @EventListener
    public void onVirtualHostDeleted(VirtualHostDeletedEvent event) {
        VirtualHost existing = this.getVirtualHostById(event.virtualHostId());
        if (existing == null) {
            this.logger.warn("[VirtualHostManager] <Delete> Tried to delete virtual host {}, but it was not found!", event.virtualHostId());
            return;
        }

        this.virtualHosts.remove(existing);
        this.logger.info("[VirtualHostDeletedEvent] Virtual host deleted: {} ({})", existing.hostname(), existing.id());
    }
}
