package com.github.uscexp.jfilework.core.command;

import java.nio.file.Path;
import java.util.List;

/**
 * Immutable snapshot of selected paths to avoid mutable UI state during execution.
 */
public record SelectionSnapshot(List<Path> paths) {

    public SelectionSnapshot {
        paths = List.copyOf(paths);
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }
}

