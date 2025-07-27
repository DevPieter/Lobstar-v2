package nl.devpieter.lobstar.models.server;

import nl.devpieter.lobstar.models.common.MinecraftVersion;

data class ServerStatus(
    val isOnline: Boolean,

    val onlinePlayers: Int,
    val maxPlayers: Int,

    val version: MinecraftVersion
)