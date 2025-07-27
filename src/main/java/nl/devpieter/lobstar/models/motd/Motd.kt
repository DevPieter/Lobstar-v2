package nl.devpieter.lobstar.models.motd;

import nl.devpieter.lobstar.enums.MotdGetType;
import nl.devpieter.lobstar.models.common.MotdSamplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Motd {

    private final UUID id;
    private final UUID issuerId;

    private String name;

    //    private MotdGetType onlinePlayersGetType;
    private int onlinePlayersGetType;
    private int onlinePlayers;
    private boolean onlinePlayersEnabled;

    //    private MotdGetType maximumPlayersGetType;
    private int maximumPlayersGetType;
    private int maximumPlayers;

    //    private MotdGetType samplePlayersGetType;
    private int samplePlayersGetType;
    private @Nullable MotdSamplePlayer[] samplePlayers;
    private boolean samplePlayersEnabled;

    //    private MotdGetType descriptionGetType;
    private int descriptionGetType;
    private @Nullable String description;
    private boolean descriptionEnabled;

    //    private MotdGetType faviconGetType;
    private int faviconGetType;
    private @Nullable String favicon;
    private boolean faviconEnabled;

    public Motd(UUID id, UUID issuerId, String name, int onlinePlayersGetType, int onlinePlayers, boolean onlinePlayersEnabled, int maximumPlayersGetType, int maximumPlayers, int samplePlayersGetType, @Nullable MotdSamplePlayer[] samplePlayers, boolean samplePlayersEnabled, int descriptionGetType, @Nullable String description, boolean descriptionEnabled, int faviconGetType, @Nullable String favicon, boolean faviconEnabled) {
        this.id = id;
        this.issuerId = issuerId;

        this.name = name;

        this.onlinePlayersGetType = onlinePlayersGetType;
        this.onlinePlayers = onlinePlayers;
        this.onlinePlayersEnabled = onlinePlayersEnabled;

        this.maximumPlayersGetType = maximumPlayersGetType;
        this.maximumPlayers = maximumPlayers;

        this.samplePlayersGetType = samplePlayersGetType;
        this.samplePlayers = samplePlayers;
        this.samplePlayersEnabled = samplePlayersEnabled;

        this.descriptionGetType = descriptionGetType;
        this.description = description;
        this.descriptionEnabled = descriptionEnabled;

        this.faviconGetType = faviconGetType;
        this.favicon = favicon;
        this.faviconEnabled = faviconEnabled;
    }

    public UUID id() {
        return id;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int onlinePlayersGetType() {
        return onlinePlayersGetType;
    }

    public MotdGetType getOnlinePlayersGetType() {
        return MotdGetType.fromInt(this.onlinePlayersGetType);
    }

    public void setOnlinePlayersGetType(int onlinePlayersGetType) {
        this.onlinePlayersGetType = onlinePlayersGetType;
    }

    public int onlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public boolean onlinePlayersEnabled() {
        return onlinePlayersEnabled;
    }

    public void setOnlinePlayersEnabled(boolean onlinePlayersEnabled) {
        this.onlinePlayersEnabled = onlinePlayersEnabled;
    }

    public int maximumPlayersGetType() {
        return maximumPlayersGetType;
    }

    public MotdGetType getMaximumPlayersGetType() {
        return MotdGetType.fromInt(this.maximumPlayersGetType);
    }

    public void setMaximumPlayersGetType(int maximumPlayersGetType) {
        this.maximumPlayersGetType = maximumPlayersGetType;
    }

    public int maximumPlayers() {
        return maximumPlayers;
    }

    public void setMaximumPlayers(int maximumPlayers) {
        this.maximumPlayers = maximumPlayers;
    }

    public int samplePlayersGetType() {
        return samplePlayersGetType;
    }

    public MotdGetType getSamplePlayersGetType() {
        return MotdGetType.fromInt(this.samplePlayersGetType);
    }

    public void setSamplePlayersGetType(int samplePlayersGetType) {
        this.samplePlayersGetType = samplePlayersGetType;
    }

    public MotdSamplePlayer[] samplePlayers() {
        return samplePlayers;
    }

    public void setSamplePlayers(MotdSamplePlayer[] samplePlayers) {
        this.samplePlayers = samplePlayers;
    }

    public boolean samplePlayersEnabled() {
        return samplePlayersEnabled;
    }

    public void setSamplePlayersEnabled(boolean samplePlayersEnabled) {
        this.samplePlayersEnabled = samplePlayersEnabled;
    }

    public int descriptionGetType() {
        return descriptionGetType;
    }

    public MotdGetType getDescriptionGetType() {
        return MotdGetType.fromInt(this.descriptionGetType);
    }

    public void setDescriptionGetType(int descriptionGetType) {
        this.descriptionGetType = descriptionGetType;
    }

    public @Nullable String description() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public boolean descriptionEnabled() {
        return descriptionEnabled;
    }

    public void setDescriptionEnabled(boolean descriptionEnabled) {
        this.descriptionEnabled = descriptionEnabled;
    }

    public int faviconGetType() {
        return faviconGetType;
    }

    public MotdGetType getFaviconGetType() {
        return MotdGetType.fromInt(this.faviconGetType);
    }

    public void setFaviconGetType(int faviconGetType) {
        this.faviconGetType = faviconGetType;
    }

    public @Nullable String favicon() {
        return favicon;
    }

    public void setFavicon(@Nullable String favicon) {
        this.favicon = favicon;
    }

    public boolean faviconEnabled() {
        return faviconEnabled;
    }

    public void setFaviconEnabled(boolean faviconEnabled) {
        this.faviconEnabled = faviconEnabled;
    }
}
