package nl.devpieter.lobstar.models.serverType;

import java.util.*

data class ServerType(
    val id: UUID,

    val issuerId: UUID,
    val ownerId: UUID,

    var name: String,
    var description: String,

    var isActive: Boolean,
    var isLobbyLike: Boolean
)