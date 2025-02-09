package nl.devpieter.lobstar.api.request;

import com.google.gson.Gson;
import nl.devpieter.lobstar.managers.ConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class RequestHelper {

    public static final String API_KEY_HEADER = "X-Api-Key";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String USER_AGENT = "Lobstar/1.0";

    private static final ConfigManager CONFIG_MANAGER = ConfigManager.getInstance();
    protected static final HttpClient CLIENT = HttpClient.newHttpClient();

    protected static final Gson GSON = new Gson();

    public static @NotNull HttpResponse<String> simpleGet(@NotNull URI uri, boolean withAuth) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(uri, withAuth).GET().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpRequest.Builder createRequestBuilder(@NotNull URI uri, boolean withAuth) {
        var builder = HttpRequest.newBuilder()
                .header(USER_AGENT_HEADER, USER_AGENT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .uri(uri);

        if (withAuth) {
            var key = CONFIG_MANAGER.getString("api_key");
            if (key == null) throw new IllegalStateException("API key is not set");

            builder.header(API_KEY_HEADER, key);
        }

        return builder;
    }
}