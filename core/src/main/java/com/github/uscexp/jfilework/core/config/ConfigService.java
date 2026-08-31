package com.github.uscexp.jfilework.core.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Loads app configuration from external file or bundled defaults.
 */
public class ConfigService {

    public static final String CONFIG_PATH_PROPERTY = "jfilework.config";
    public static final String DEFAULT_EXTERNAL_CONFIG_PATH = "config/app-config.json";
    public static final String DEFAULT_CLASSPATH_RESOURCE = "/default-app-config.json";

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ObjectMapper objectMapper;

    public ConfigService() {
        this.objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public AppConfig load() {
        try {
            Path configuredPath = resolveConfigPath();
            if (configuredPath != null && Files.exists(configuredPath)) {
                log.info("Loading configuration from {}", configuredPath.toAbsolutePath());
                return sanitize(objectMapper.readValue(configuredPath.toFile(), AppConfig.class));
            }

            try (InputStream inputStream = ConfigService.class.getResourceAsStream(DEFAULT_CLASSPATH_RESOURCE)) {
                if (inputStream == null) {
                    log.warn("Bundled default config not found: {}", DEFAULT_CLASSPATH_RESOURCE);
                    return AppConfig.defaults();
                }
                log.info("Loading bundled default configuration from {}", DEFAULT_CLASSPATH_RESOURCE);
                return sanitize(objectMapper.readValue(inputStream, AppConfig.class));
            }
        } catch (IOException exception) {
            log.warn("Cannot load app config, fallback to defaults", exception);
            return AppConfig.defaults();
        }
    }

    public void save(Path targetPath, AppConfig config) throws IOException {
        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetPath.toFile(), sanitize(config));
    }

    public Path resolveConfigPath() {
        return resolveConfigPathInternal();
    }

    private static Path resolveConfigPathInternal() {
        String fromProperty = System.getProperty(CONFIG_PATH_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return Path.of(fromProperty.trim());
        }
        return Path.of(DEFAULT_EXTERNAL_CONFIG_PATH);
    }

    private static AppConfig sanitize(AppConfig rawConfig) {
        if (rawConfig == null) {
            return AppConfig.defaults();
        }

        UiConfig ui = rawConfig.ui() == null ? UiConfig.defaults() : rawConfig.ui();
        List<ActionConfig> actions = rawConfig.actions() == null ? List.of() : rawConfig.actions().stream()
                .filter(action -> action != null
                        && action.id() != null && !action.id().isBlank()
                        && action.label() != null && !action.label().isBlank()
                        && action.commandId() != null && !action.commandId().isBlank())
                .map(action -> new ActionConfig(
                        action.id().trim(),
                        action.label().trim(),
                        action.commandId().trim(),
                        action.params() == null ? Map.of() : action.params(),
                        action.shortcut() == null ? "" : action.shortcut().trim(),
                        action.placement() == null ? List.of() : action.placement().stream().map(String::trim).toList()
                ))
                .toList();

        return new AppConfig(ui, actions);
    }
}




