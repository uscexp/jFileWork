package com.github.uscexp.jfilework.core.security;

import java.nio.file.Path;
import java.util.List;

/**
 * Validates that file system paths stay within an optional allow-list of roots.
 */
public class PathPolicy {

    private final List<Path> allowedRoots;

    public PathPolicy(List<String> allowedRoots) {
        this.allowedRoots = allowedRoots == null
                ? List.of()
                : allowedRoots.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> Path.of(value.trim()).toAbsolutePath().normalize())
                .toList();
    }

    public void validatePath(Path path) {
        if (path == null || allowedRoots.isEmpty()) {
            return;
        }

        Path normalized = path.toAbsolutePath().normalize();
        boolean matches = allowedRoots.stream().anyMatch(normalized::startsWith);
        if (!matches) {
            throw new PolicyException("Path is outside allowed roots: " + normalized);
        }
    }
}

