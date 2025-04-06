package nl.devpieter.lobstar.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.leangen.geantyref.TypeToken;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.utils.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ConfigManager {

    private static ConfigManager instance;

    private final Logger logger = Lobstar.getInstance().getLogger();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String configPath = "./plugins/lobstar/config.json";

    private final LinkedHashMap<String, String> defaultConfig = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> config = new LinkedHashMap<>();

    private ConfigManager() {
        this.defaultConfig.put("version", "1.0.0");
        this.defaultConfig.put("api_key", "my-secret-api-key");
        this.defaultConfig.put("api_base_url", "http://127.0.0.1:5200");
        this.load();
    }

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public String getString(String key) {
        return this.config.get(key);
    }

    public void load() {
        File file = this.getConfigFile();
        if (file == null) {
            this.logger.error("Failed to load config, file is null");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();

            LinkedHashMap<String, String> loadedConfig = this.gson.fromJson(reader, type);
            if (loadedConfig == null) throw new Exception("Loaded config is null");

            this.config.clear();
            this.config.putAll(loadedConfig);

            this.logger.info("Config loaded");
        } catch (Exception e) {
            this.logger.info("Config file not found, creating new one with default values");
            this.reset();
        }
    }

    public void reset() {
        this.config.clear();
        this.config.putAll(this.defaultConfig);

        this.logger.info("Config reset to default values");
        this.save();
    }

    private @Nullable File getConfigFile() {
        File file = new File(this.configPath);
        if (FileUtils.tryCreateFile(file)) return file;

        this.logger.error("Failed to create config file: {}", file.getAbsolutePath());
        return null;
    }

    private void save() {
        File file = this.getConfigFile();
        if (file == null) {
            this.logger.error("Failed to save config, file is null");
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            this.gson.toJson(this.config, writer);
            this.logger.info("Config saved");
        } catch (Exception e) {
            this.logger.error("Failed to write config: {}", e.getMessage());
        }
    }
}
