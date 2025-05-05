package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.ConfigManager;
import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.models.version.Version;
import nl.devpieter.lobstar.models.version.VersionCheckResponse;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class VersionManager {

    private static VersionManager INSTANCE;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final String version = this.configManager.getString("version");
    private final String versionApiUrl = this.configManager.getString("api_base_url") + "/api/version";

    private boolean successfullyLoaded;

    private @Nullable Version apiVersion;
    private @Nullable Version pluginVersion;
    private boolean compatible;

    private VersionManager() {
    }

    public void loadVersionInfo(Consumer<Void> callback) {
        this.makeVersionCheckRequest().thenAccept(response -> {
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

    public static VersionManager getInstance() {
        if (INSTANCE == null) INSTANCE = new VersionManager();
        return INSTANCE;
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
                if (response.statusCode() != 200) throw new Exception("API returned " + response.statusCode() + ", expected 200");

                return GSON.fromJson(response.body(), VersionCheckResponse.class);
            }
        }.execute().getFuture();
    }
}
