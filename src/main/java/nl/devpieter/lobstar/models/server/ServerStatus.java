package nl.devpieter.lobstar.models.server;

public record ServerStatus(
        boolean isOnline,
        int playerCount
) {
}
