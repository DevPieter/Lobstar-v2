package nl.devpieter.lobstar.models.whitelist;

import java.util.*

data class WhitelistEntry(
    val id: UUID,

    val issuerId: UUID,
    val playerId: UUID,
    val serverId: UUID?,

    var isWhitelisted: Boolean,
    var isSuperEntry: Boolean,

    var hasExpiration: Boolean,
    var expirationDate: Date?
)