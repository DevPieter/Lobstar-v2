package nl.devpieter.lobstar.managers;

import nl.devpieter.lobstar.api.request.AsyncRequest;
import nl.devpieter.lobstar.models.GlobalWhitelistEntry;
import nl.devpieter.lobstar.models.ServerWhitelistEntry;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager implements Listener {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final String whitelistApiUrl = configManager.getString("whitelist_api_url");

    public CompletableFuture<@Nullable GlobalWhitelistEntry> getGlobalWhitelistEntry(UUID uuid) {
        URI uri = URI.create(String.format("%s/global/%s", whitelistApiUrl, uuid));
        return new AsyncRequest<GlobalWhitelistEntry>() {

            @Override
            protected @Nullable GlobalWhitelistEntry requestAsync() throws Exception {
                HttpResponse<String> response = simpleGet(uri, true);
                if (response.statusCode() != 200) throw new Exception("Failed to get global whitelist entry");

                return GSON.fromJson(response.body(), GlobalWhitelistEntry.class);
            }
        }.execute().getFuture();
    }

    public CompletableFuture<@Nullable ServerWhitelistEntry> getServerWhitelistEntry(UUID serverId, UUID playerId) {
        URI uri = URI.create(String.format("%s/server/%s/%s", whitelistApiUrl, serverId, playerId));
        return new AsyncRequest<ServerWhitelistEntry>() {

            @Override
            protected @Nullable ServerWhitelistEntry requestAsync() throws Exception {
                HttpResponse<String> response = simpleGet(uri, true);
                if (response.statusCode() != 200) throw new Exception("Failed to get server whitelist entry");

                return GSON.fromJson(response.body(), ServerWhitelistEntry.class);
            }
        }.execute().getFuture();
    }
}
