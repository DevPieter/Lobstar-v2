package nl.devpieter.lobstar.models.version;

public record Version(
        String currentVersion,
        String latestVersion,
        boolean updateAvailable
) {
}
