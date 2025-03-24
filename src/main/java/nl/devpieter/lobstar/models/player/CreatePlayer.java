package nl.devpieter.lobstar.models.player;

import java.util.UUID;

public record CreatePlayer(
        UUID minecraftUuid,
        String username
) {
}
