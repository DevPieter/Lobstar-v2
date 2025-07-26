package nl.devpieter.lobstar.models.whitelist;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public record WhitelistEntry(
        UUID id,
        UUID issuerId,

        UUID playerId,
        @Nullable UUID serverId,

        boolean isWhitelisted,
        boolean isSuperEntry,

        boolean hasExpiration
//        @Nullable LocalDateTime expirationDate
) {
}
