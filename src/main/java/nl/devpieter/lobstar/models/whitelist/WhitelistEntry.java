package nl.devpieter.lobstar.models.whitelist;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record WhitelistEntry(
        UUID id,
        UUID playerId,
        @Nullable UUID serverId,
        UUID issuerId,
        boolean isWhitelisted
) {
}
