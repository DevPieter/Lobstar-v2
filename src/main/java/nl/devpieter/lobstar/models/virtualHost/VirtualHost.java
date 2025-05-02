package nl.devpieter.lobstar.models.virtualHost;

import nl.devpieter.lobstar.enums.HostnameCheckType;

import java.util.UUID;

public class VirtualHost {

    private final UUID id;

    public final UUID serverId;
    public final UUID issuerId;

    public String hostname;

    public boolean ignoreCase;
    public int checkType;

    public boolean isDefault;
    public boolean isEnabled;

    public VirtualHost(UUID id, UUID serverId, UUID issuerId, String hostname, boolean ignoreCase, int checkType, boolean isDefault, boolean isEnabled) {
        this.id = id;

        this.serverId = serverId;
        this.issuerId = issuerId;

        this.hostname = hostname;

        this.ignoreCase = ignoreCase;
        this.checkType = checkType;

        this.isDefault = isDefault;
        this.isEnabled = isEnabled;
    }

    public UUID id() {
        return id;
    }

    public UUID serverId() {
        return serverId;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public String hostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean ignoreCase() {
        return ignoreCase;
    }

    public void ignoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public int checkType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public HostnameCheckType getCheckType() {
        return HostnameCheckType.fromInt(checkType);
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
