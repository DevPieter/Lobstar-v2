package nl.devpieter.lobstar.models.player;

import nl.devpieter.lobstar.models.common.MinecraftVersion
import java.util.*

data class PlayerStatus(
    val name: String,

    val ping: Long,
    val isOnline: Boolean,

    val address: String,
    val port: Int,

    val currentServerId: UUID,
    val version: MinecraftVersion
)