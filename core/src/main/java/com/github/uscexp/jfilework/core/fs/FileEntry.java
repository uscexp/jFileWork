package com.github.uscexp.jfilework.core.fs;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Immutable file-system item metadata used by the UI layer.
 */
public record FileEntry(
        Path path,
        String name,
        boolean directory,
        long size,
        Instant lastModified,
        boolean parentLink
) {

    public static FileEntry parent(Path parentPath) {
        return new FileEntry(parentPath, "..", true, 0L, Instant.EPOCH, true);
    }
}

