package com.github.uscexp.jfilework.api.plugin;

import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Stable host services exposed during plugin initialization.
 */
public record PluginContext(
        String applicationVersion,
        String pluginApiVersion,
        Path pluginsDirectory,
        Logger logger
) {
}

