package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.ConfigManager;
import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager implements Listener {

    private static WhitelistManager INSTANCE;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final String apiUrl = this.configManager.getString("api_base_url") + "/api";

    private final List<UUID> pendingRequests = new ArrayList<>();

    private WhitelistManager() {
    }

    public static WhitelistManager getInstance() {
        if (INSTANCE == null) INSTANCE = new WhitelistManager();
        return INSTANCE;
    }

    public boolean hasPendingRequest(UUID playerId) {
        return this.pendingRequests.contains(playerId);
    }

    public @Nullable CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(UUID playerId) {
        if (this.hasPendingRequest(playerId)) return null;
        this.pendingRequests.add(playerId);

        URI uri = URI.create(String.format("%s/player/%s/whitelist", this.apiUrl, playerId));
        return this.getWhitelistEntry(playerId, uri);
    }

    public @Nullable CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(UUID serverId, UUID playerId) {
        if (this.hasPendingRequest(playerId)) return null;
        this.pendingRequests.add(playerId);

        URI uri = URI.create(String.format("%s/server/%s/whitelist/%s", this.apiUrl, serverId, playerId));
        return this.getWhitelistEntry(playerId, uri);
    }

    private CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(UUID playerId, URI uri) {
        return new AsyncRequest<WhitelistEntry>() {

            @Override
            protected @Nullable WhitelistEntry requestAsync() throws Exception {
                try {
                    HttpResponse<String> response = simpleGet(uri, true);

                    if (response.statusCode() == 404) return null;
                    if (response.statusCode() != 200) throw new Exception("API returned " + response.statusCode() + ", expected 200 or 404");

                    return GSON.fromJson(response.body(), WhitelistEntry.class);
                } finally {
                    pendingRequests.remove(playerId);
                }
            }
        }.execute().getFuture();
    }
}
