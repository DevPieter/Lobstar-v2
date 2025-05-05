package nl.devpieter.lobstar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.leangen.geantyref.TypeToken;
import nl.devpieter.lobstar.utils.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

public class ConfigManager {

    private static ConfigManager INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String configPath = "./plugins/lobstar/config.json";

    private final Logger logger;

    private final LinkedHashMap<String, String> defaultConfig = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> staticConfig = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> config = new LinkedHashMap<>();

    private ConfigManager() {
        Lobstar lobstar = Lobstar.getInstance();
        this.logger = lobstar.getLogger();

        this.defaultConfig.put("api_key", "my-secret-api-key");
        this.defaultConfig.put("api_base_url", "http://127.0.0.1:5200");

        this.staticConfig.put("version", BuildConstants.VERSION);
        this.load();
    }

    public static ConfigManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ConfigManager();
        return INSTANCE;
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
            this.config.putAll(this.staticConfig);

            this.logger.info("Config loaded");
        } catch (Exception e) {
            this.logger.info("Config file not found, creating new one with default values");
            this.reset();
        }
    }

    public void reset() {
        this.config.clear();
        this.config.putAll(this.defaultConfig);
        this.config.putAll(this.staticConfig);

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
