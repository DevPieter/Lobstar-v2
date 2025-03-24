package nl.devpieter.lobstar.models.player;

import java.util.UUID;

public record PlayerStatus(
        String name,
        boolean isOnline,
        UUID currentServerId
) {
}
