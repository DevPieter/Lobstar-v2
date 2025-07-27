package nl.devpieter.lobstar.models.server;

import nl.devpieter.lobstar.models.common.MinecraftVersion;

public record ServerStatus(
        boolean isOnline,

        int onlinePlayers,
        int maxPlayers,

        MinecraftVersion version
) {
}
