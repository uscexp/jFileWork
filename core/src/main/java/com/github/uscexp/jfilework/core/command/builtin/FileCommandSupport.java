package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.CancellationToken;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

final class FileCommandSupport {

    private FileCommandSupport() {
    }

    static Path resolveTargetDirectory(Path defaultTarget, java.util.Map<String, String> params) {
        String configured = params.get("target");
        if (configured == null || configured.isBlank()) {
            return defaultTarget;
        }
        return Path.of(configured.trim());
    }

    static void ensureDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException("Target is not a directory: " + directory);
        }
    }

    static void copyRecursively(Path source, Path target, CancellationToken token) throws IOException {
        if (token.isCancelled()) {
            return;
        }

        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (token.isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }
                    Path relative = source.relativize(dir);
                    Path targetDir = target.resolve(relative);
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (token.isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }
                    Path relative = source.relativize(file);
                    Files.copy(file, target.resolve(relative), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    return FileVisitResult.CONTINUE;
                }
            });
            return;
        }

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    static void deleteRecursively(Path source, CancellationToken token) throws IOException {
        if (!Files.exists(source)) {
            return;
        }

        if (!Files.isDirectory(source)) {
            Files.deleteIfExists(source);
            return;
        }

        try (var walk = Files.walk(source)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                if (!token.isCancelled()) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            });
        } catch (RuntimeException runtimeException) {
            if (runtimeException.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw runtimeException;
        }
    }
}

