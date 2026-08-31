package com.github.uscexp.jfilework.api.plugin;

import java.util.Map;

/**
 * Executable unit contributed by a plugin.
 */
public interface PluginCommand {

    String id();

    default boolean canExecute(PluginCommandContext context) {
        return true;
    }

    PluginCommandResult execute(PluginCommandContext context, Map<String, String> params) throws Exception;
}
