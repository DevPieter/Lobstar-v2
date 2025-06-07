package nl.devpieter.lobstar.models.virtualHost;

import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.HostnameCheckType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualHost {

    private final UUID id;

    private final UUID issuerId;
    private @Nullable UUID serverId;

    private String hostname;
    private String normalizedHostname;

    public int priority;
    public int checkType;
    public boolean ignoreCase;

    public boolean isEnabled;
    public boolean useCustomMotd;

    public @Nullable UUID motdId;

    public VirtualHost(UUID id, UUID issuerId, @Nullable UUID serverId, String hostname, int priority, int checkType, boolean ignoreCase, boolean isEnabled, boolean useCustomMotd, @Nullable UUID motdId) {
        this.id = id;

        this.issuerId = issuerId;
        this.serverId = serverId;

        this.hostname = hostname;

        this.priority = priority;
        this.checkType = checkType;
        this.ignoreCase = ignoreCase;

        this.isEnabled = isEnabled;
        this.useCustomMotd = useCustomMotd;

        this.motdId = motdId;
    }

    public UUID id() {
        return id;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public @Nullable UUID serverId() {
        return serverId;
    }

    public void setServerId(@Nullable UUID serverId) {
        this.serverId = serverId;
    }

    public String hostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.normalizedHostname = this.normalize(hostname);
    }

    public String normalizedHostname() {
        if (this.normalizedHostname == null) this.normalizedHostname = this.normalize(hostname);
        return normalizedHostname;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int checkType() {
        return checkType;
    }

    public HostnameCheckType getCheckType() {
        return HostnameCheckType.fromInt(checkType);
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public boolean ignoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean useCustomMotd() {
        return useCustomMotd;
    }

    public void setUseCustomMotd(boolean useCustomMotd) {
        this.useCustomMotd = useCustomMotd;
    }

    public @Nullable UUID motdId() {
        return motdId;
    }

    public void setMotdId(@Nullable UUID motdId) {
        this.motdId = motdId;
    }

    public String normalize(@NotNull String hostname) {
        return hostname.toLowerCase();
    }

    public boolean compare(@NotNull String hostname) {
        String toCheck = this.ignoreCase ? this.normalize(hostname) : hostname;
        String toCompare = this.ignoreCase ? this.normalizedHostname() : this.hostname;

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
