package com.github.uscexp.jfilework.api.plugin;

/**
 * Immutable metadata for a plugin.
 */
public record PluginDescriptor(
        String id,
        String name,
        String version,
        String pluginApiVersion
) {
}

