package nl.devpieter.lobstar.models.version;

data class Version(
    val current: String,
    val latest: String,
    val updateAvailable: Boolean,
    val severity: Int
)