package nl.devpieter.lobstar.models;

import java.util.UUID;

public record ServerWhitelistEntry(
        UUID id,
        UUID playerId,
        UUID serverId,
        boolean isBanned,
        boolean isWhitelisted,
        boolean isPendingReview
) {
}
