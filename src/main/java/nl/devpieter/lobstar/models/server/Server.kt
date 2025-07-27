package nl.devpieter.lobstar.models.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Server {

    private final UUID id;

    private final UUID issuerId;
    private final UUID ownerId;

    private final String name;
    private String displayName;

    private UUID typeId;

    private boolean isListed;
    private boolean isJoinable;
    private boolean isWhitelistActive;

    private boolean isUnderMaintenance;
    private @Nullable String maintenanceMessage;

    private final String address;
    private final int port;

    public Server(UUID id, UUID issuerId, UUID ownerId, String name, String displayName, UUID typeId, boolean isListed, boolean isJoinable, boolean isWhitelistActive, boolean isUnderMaintenance, @Nullable String maintenanceMessage, String address, int port) {
        this.id = id;

        this.issuerId = issuerId;
        this.ownerId = ownerId;

        this.name = name;
        this.displayName = displayName;

        this.typeId = typeId;

        this.isListed = isListed;
        this.isJoinable = isJoinable;
        this.isWhitelistActive = isWhitelistActive;

        this.isUnderMaintenance = isUnderMaintenance;
        this.maintenanceMessage = maintenanceMessage;

        this.address = address;
        this.port = port;
    }

    public @Nullable RegisteredServer findRegisteredServer() {
        return Lobstar.getInstance().getProxy().getServer(this.name()).orElse(null);
    }

    public boolean isCriticallyDifferent(@NotNull Server server) {
        return !server.name().equals(this.name) ||
                !server.address().equals(this.address) ||
                server.port() != this.port;
    }

    public UUID id() {
        return id;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID typeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public boolean isListed() {
        return isListed;
    }

    public void setListed(boolean listed) {
        isListed = listed;
    }

    public boolean isJoinable() {
        return isJoinable;
    }

    public void setJoinable(boolean joinable) {
        isJoinable = joinable;
    }

    public boolean isWhitelistActive() {
        return isWhitelistActive;
    }

    public void setWhitelistActive(boolean whitelistActive) {
        isWhitelistActive = whitelistActive;
    }

    public boolean isUnderMaintenance() {
        return isUnderMaintenance;
    }

    public void setUnderMaintenance(boolean underMaintenance) {
        isUnderMaintenance = underMaintenance;
    }

    public @Nullable String maintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(@Nullable String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }
}
