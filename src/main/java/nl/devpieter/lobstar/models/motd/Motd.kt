package nl.devpieter.lobstar.models.motd

import nl.devpieter.lobstar.enums.MotdGetType
import nl.devpieter.lobstar.models.common.MotdSamplePlayer
import java.util.*

data class Motd(
    val id: UUID,
    val issuerId: UUID,

    var name: String,

    var onlinePlayersGetType: Int,
    var onlinePlayers: Int,
    var onlinePlayersEnabled: Boolean,

    var maximumPlayersGetType: Int,
    var maximumPlayers: Int,

    var samplePlayersGetType: Int,
    var samplePlayers: List<MotdSamplePlayer>,
    var samplePlayersEnabled: Boolean,

    var descriptionGetType: Int,
    var description: String,
    var descriptionEnabled: Boolean,

    var faviconGetType: Int,
    var favicon: String,
    var faviconEnabled: Boolean
) {
    val onlinePlayersType: MotdGetType get() = MotdGetType.fromInt(onlinePlayersGetType)
    val maximumPlayersType: MotdGetType get() = MotdGetType.fromInt(maximumPlayersGetType)
    val samplePlayersType: MotdGetType get() = MotdGetType.fromInt(samplePlayersGetType)
    val descriptionType: MotdGetType get() = MotdGetType.fromInt(descriptionGetType)
    val faviconType: MotdGetType get() = MotdGetType.fromInt(faviconGetType)
}