package nl.devpieter.lobstar.models.server;

import org.jetbrains.annotations.Nullable;

public record ServerStatus(
        boolean isOnline,

        int proxyPlayers,
        int serverPlayers,
        int maxPlayers,

        @Nullable String versionName
) {
}
