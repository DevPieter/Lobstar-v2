package nl.devpieter.lobstar.models.version;

public record Version(
        String current,
        String latest,
        boolean updateAvailable,
        int severity
) {
}
