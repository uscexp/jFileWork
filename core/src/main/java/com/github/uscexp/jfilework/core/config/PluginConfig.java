package com.github.uscexp.jfilework.core.config;

import java.util.List;

/**
 * Plugin discovery configuration.
 */
public record PluginConfig(
        String directory,
        List<String> enabled
) {

    public static PluginConfig defaults() {
        return new PluginConfig("plugins", List.of());
    }
}

