package nl.devpieter.lobstar.models.version;

public record VersionCheckResponse(
        Version api,
        Version plugin,
        boolean compatible
) {
}
