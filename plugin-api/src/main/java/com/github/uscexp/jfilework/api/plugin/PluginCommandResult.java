package com.github.uscexp.jfilework.api.plugin;

/**
 * Result object for plugin commands.
 */
public record PluginCommandResult(PluginCommandStatus status, String message, long processedItems) {

    public static PluginCommandResult success(String message, long processedItems) {
        return new PluginCommandResult(PluginCommandStatus.SUCCESS, message, processedItems);
    }

    public static PluginCommandResult failed(String message) {
        return new PluginCommandResult(PluginCommandStatus.FAILED, message, 0L);
    }
}

