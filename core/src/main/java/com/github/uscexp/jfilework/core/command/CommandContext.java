package com.github.uscexp.jfilework.core.command;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable execution context for commands.
 */
public record CommandContext(
        Path activeDirectory,
        Path inactiveDirectory,
        SelectionSnapshot selection,
        CancellationToken cancellationToken,
        ProgressSink progressSink
) {

    public CommandContext {
        activeDirectory = Objects.requireNonNull(activeDirectory, "activeDirectory");
        inactiveDirectory = Objects.requireNonNull(inactiveDirectory, "inactiveDirectory");
        selection = selection == null ? new SelectionSnapshot(java.util.List.of()) : selection;
        cancellationToken = cancellationToken == null ? new CancellationToken() : cancellationToken;
        progressSink = progressSink == null ? ProgressSink.NO_OP : progressSink;
    }
}

