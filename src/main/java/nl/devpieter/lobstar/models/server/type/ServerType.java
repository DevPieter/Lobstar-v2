package nl.devpieter.lobstar.models.server.type;

import java.util.UUID;

public class ServerType {

    private final UUID id;

    private final UUID issuerId;
    private final UUID ownerId;

    private String name;
    private String description;

    private boolean isLobbyLike;

    public ServerType(UUID id, UUID issuerId, UUID ownerId, String name, String description, boolean isLobbyLike) {
        this.id = id;

        this.issuerId = issuerId;
        this.ownerId = ownerId;

        this.name = name;
        this.description = description;

        this.isLobbyLike = isLobbyLike;
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

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLobbyLike() {
        return isLobbyLike;
    }

    public void setLobbyLike(boolean lobbyLike) {
        isLobbyLike = lobbyLike;
    }
}
