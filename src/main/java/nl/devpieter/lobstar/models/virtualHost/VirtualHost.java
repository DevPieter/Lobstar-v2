package nl.devpieter.lobstar.models.virtualHost;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.HostnameCheckType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualHost {

    private final UUID id;

    public final UUID serverId;
    public final UUID issuerId;

    public String hostname;
    public String normalizedHostname;

    public boolean ignoreCase;
    public int checkType;

    public boolean isDefault;
    public boolean isEnabled;

    public VirtualHost(UUID id, UUID serverId, UUID issuerId, String hostname, boolean ignoreCase, int checkType, boolean isDefault, boolean isEnabled) {
        this.id = id;

        this.serverId = serverId;
        this.issuerId = issuerId;

        this.hostname = hostname;
        this.normalizedHostname = this.normalize(hostname);

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

    public String normalizedHostname() {
        return normalizedHostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.normalizedHostname = this.normalize(hostname);
    }

    public boolean ignoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
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

    public String normalize(@NotNull String hostname) {
        return hostname.toLowerCase();
    }

    public boolean compare(@NotNull String hostname) {
        String toCheck = this.ignoreCase ? this.normalize(hostname) : hostname;
        String toCompare = this.ignoreCase ? this.normalizedHostname : this.hostname;

        return switch (this.getCheckType()) {
            case Exact -> toCheck.equals(toCompare);
            case StartsWith -> toCheck.startsWith(toCompare);
            case EndsWith -> toCheck.endsWith(toCompare);
            case Contains -> toCheck.contains(toCompare);
            case Regex -> this.compareRegex(toCheck, toCompare);
        };
    }

    private boolean compareRegex(@NotNull String toCheck, @NotNull String toCompare) {
        try {
            Pattern pattern = Pattern.compile(toCompare);
            Matcher matcher = pattern.matcher(toCheck);

            return matcher.matches();
        } catch (PatternSyntaxException e) {
            Lobstar.getInstance().getLogger().error("Invalid regex pattern: {}", toCompare, e);
            return false;
        }
    }
}
