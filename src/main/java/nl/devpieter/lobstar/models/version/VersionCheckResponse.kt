package nl.devpieter.lobstar.models.version;

data class VersionCheckResponse(
    val api: Version,
    val requester: Version,
    val compatible: Boolean
)