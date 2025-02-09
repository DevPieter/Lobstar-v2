package nl.devpieter.lobstar.models;

import java.util.UUID;

public record GlobalWhitelistEntry(UUID id, UUID playerId, boolean isBanned, boolean isWhitelisted, boolean isPendingReview) {
}
