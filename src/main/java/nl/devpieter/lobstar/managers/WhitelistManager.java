package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager implements Listener {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final String apiUrl = configManager.getString("api_base_url") + "/api";

    public CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(UUID playerId) {
        URI uri = URI.create(String.format("%s/player/%s/whitelist", apiUrl, playerId));
        return getWhitelistEntry(uri);
    }

    public CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(UUID serverId, UUID playerId) {
        URI uri = URI.create(String.format("%s/server/%s/whitelist/%s", apiUrl, serverId, playerId));
        return getWhitelistEntry(uri);
    }

    private CompletableFuture<@Nullable WhitelistEntry> getWhitelistEntry(URI uri) {
        return new AsyncRequest<WhitelistEntry>() {

            @Override
            protected @Nullable WhitelistEntry requestAsync() throws Exception {
                HttpResponse<String> response = simpleGet(uri, true);

                if (response.statusCode() == 404) return null;
                if (response.statusCode() != 200) throw new Exception("Failed to get whitelist entry");

                return GSON.fromJson(response.body(), WhitelistEntry.class);
            }
        }.execute().getFuture();
    }
}
