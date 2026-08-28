package com.github.uscexp.jfilework.core.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Local file-system implementation based on java.nio.file APIs.
 */
public class LocalFileService implements FileService {

    @Override
    public List<FileEntry> listDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directory);
        }

        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .map(this::toEntry)
                    .sorted(Comparator
                            .comparing(FileEntry::directory).reversed()
                            .thenComparing(entry -> entry.name().toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    private FileEntry toEntry(Path path) {
        boolean directory = Files.isDirectory(path);
        long size = 0L;
        Instant lastModified = Instant.EPOCH;

        try {
            if (!directory) {
                size = Files.size(path);
            }
            FileTime fileTime = Files.getLastModifiedTime(path);
            lastModified = fileTime.toInstant();
        } catch (IOException ignored) {
            // Keep fallback metadata so one unreadable file does not break listing.
        }

        return new FileEntry(path, path.getFileName().toString(), directory, size, lastModified, false);
    }
}

