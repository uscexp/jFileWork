package com.github.uscexp.jfilework.core.plugin;

/**
 * Runtime status of a discovered plugin.
 */
public record PluginRuntimeInfo(
        String id,
        String name,
        String version,
        PluginStatus status,
        String message
) {
}
