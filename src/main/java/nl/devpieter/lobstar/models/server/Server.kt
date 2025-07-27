package nl.devpieter.lobstar.models.server;

import com.velocitypowered.api.proxy.server.RegisteredServer
import nl.devpieter.lobstar.Lobstar
import java.util.*

data class Server(
    val id: UUID,

    val issuerId: UUID,
    val ownerId: UUID,

    val name: String,
    var displayName: String,

    var typeId: UUID,

    var isListed: Boolean,
    var isJoinable: Boolean,
    var isWhitelistActive: Boolean,

    var isUnderMaintenance: Boolean,
    var maintenanceMessage: String?, // TODO - Make this non-nullable when API is updated

    val address: String,
    val port: Int
) {
    fun findRegisteredServer(): RegisteredServer? {
        return Lobstar.getInstance().proxy.getServer(this.name).orElse(null)
    }

    fun isCriticallyDifferent(other: Server): Boolean {
        return other.name != this.name || other.address != this.address || other.port != this.port
    }
}