package nl.devpieter.lobstar.models.virtualHost;

import nl.devpieter.lobstar.Lobstar
import nl.devpieter.lobstar.enums.HostnameCheckType
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

data class VirtualHost(
    val id: UUID,

    val issuerId: UUID,
    var serverId: UUID,

    var hostname: String,

    var priority: Int,
    var checkType: Int,
    var ignoreCase: Boolean,

    var isEnabled: Boolean,

    var useCustomMotd: Boolean,
    var motdId: UUID?
) {
    val hostnameCheckType: HostnameCheckType get() = HostnameCheckType.fromInt(checkType)

    fun compare(input: String): Boolean {
        val toCheck = if (ignoreCase) normalize(input) else input
        val toCompare = if (ignoreCase) normalize(hostname) else hostname

        return when (hostnameCheckType) {
            HostnameCheckType.Exact -> toCheck == toCompare
            HostnameCheckType.StartsWith -> toCheck.startsWith(toCompare)
            HostnameCheckType.EndsWith -> toCheck.endsWith(toCompare)
            HostnameCheckType.Contains -> toCheck.contains(toCompare)
            HostnameCheckType.Regex -> compareRegex(toCheck, toCompare)
        }
    }

    private fun normalize(input: String): String = input.lowercase()

    private fun compareRegex(toCheck: String, toCompare: String): Boolean {
        return try {
            val pattern = Pattern.compile(toCompare)
            val matcher = pattern.matcher(toCheck)

            matcher.matches()
        } catch (e: PatternSyntaxException) {
            Lobstar.getInstance().logger.error("Invalid regex pattern: {}", toCompare, e)
            false
        }
    }
}