package nl.devpieter.lobstar.managers;

import java.util.HashMap;

public class ConfigManager {

    private static ConfigManager instance;

    private final HashMap<String, String> config = new HashMap<>();

    private ConfigManager() {
        config.put("api_key", "my-super-secret-api-key");

        config.put("socket_url", "http://127.0.0.1:5200/hub/plugin");
        config.put("whitelist_api_url", "http://localhost:5200/api/whitelist");

        config.put("version", "1.0.0");
        config.put("version_api_url", "http://localhost:5200/api/version");
    }

    public String getString(String key) {
        return config.get(key);
    }

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }
}
