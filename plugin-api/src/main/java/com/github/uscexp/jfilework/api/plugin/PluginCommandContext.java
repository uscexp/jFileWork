package com.github.uscexp.jfilework.api.plugin;

import java.nio.file.Path;
import java.util.List;

/**
 * Stable execution context exposed to plugin commands.
 */
public record PluginCommandContext(
        Path activeDirectory,
        Path inactiveDirectory,
        List<Path> selection,
        PluginCancellationToken cancellationToken,
        PluginProgressSink progressSink
) {

    public PluginCommandContext {
        selection = selection == null ? List.of() : List.copyOf(selection);
        cancellationToken = cancellationToken == null ? () -> false : cancellationToken;
        progressSink = progressSink == null ? PluginProgressSink.NO_OP : progressSink;
    }
}

