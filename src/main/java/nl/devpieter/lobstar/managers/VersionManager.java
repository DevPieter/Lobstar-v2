package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.models.version.Version;
import nl.devpieter.lobstar.models.version.VersionCheckResponse;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class VersionManager {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final String version = configManager.getString("version");
    private final String versionApiUrl = configManager.getString("version_api_url");

    private boolean successfullyLoaded;

    private @Nullable Version apiVersion;
    private @Nullable Version pluginVersion;
    private boolean compatible;

    public void loadVersionInfo(Consumer<Void> callback) {
        makeVersionCheckRequest().thenAccept(response -> {
            if (response == null) {
                this.successfullyLoaded = false;
                callback.accept(null);
                return;
            }

            this.apiVersion = response.api();
            this.pluginVersion = response.requester();
            this.compatible = response.compatible();

            this.successfullyLoaded = true;
            callback.accept(null);
        }).exceptionally(e -> {
            this.successfullyLoaded = false;
            callback.accept(null);
            return null;
        });
    }

    public boolean isSuccessfullyLoaded() {
        return this.successfullyLoaded;
    }

    public @Nullable Version getApiVersion() {
        return this.apiVersion;
    }

    public @Nullable Version getPluginVersion() {
        return this.pluginVersion;
    }

    public boolean isCompatible() {
        return this.compatible;
    }

    private CompletableFuture<@Nullable VersionCheckResponse> makeVersionCheckRequest() {
        URI uri = URI.create(String.format("%s/check/plugin?version=%s", this.versionApiUrl, this.version));
        return new AsyncRequest<VersionCheckResponse>() {

            @Override
            protected @Nullable VersionCheckResponse requestAsync() throws Exception {
                HttpResponse<String> response = simpleGet(uri, true);
                if (response.statusCode() != 200) throw new Exception("Failed to check version");

                return GSON.fromJson(response.body(), VersionCheckResponse.class);
            }
        }.execute().getFuture();
    }
}
