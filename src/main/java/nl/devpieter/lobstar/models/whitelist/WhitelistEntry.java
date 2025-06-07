package nl.devpieter.lobstar.models.whitelist;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record WhitelistEntry(
        UUID id,
        UUID issuerId,

        UUID playerId,
        @Nullable UUID serverId,

        boolean isWhitelisted
) {
}
