package nl.devpieter.lobstar.models.player;

import nl.devpieter.lobstar.models.common.MinecraftVersion;

import java.util.UUID;

public record PlayerStatus(
        String name,

        long ping,
        boolean isOnline,

        String address,
        int port,

        UUID currentServerId,
        MinecraftVersion version
) {
}
