package com.github.uscexp.jfilework.core.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Applies external process execution rules like feature flags and executable allow-lists.
 */
public class CommandPolicy {

    private final boolean allowOsCommands;
    private final List<Path> allowedExecutables;

    public CommandPolicy(boolean allowOsCommands, List<String> allowedExecutables) {
        this.allowOsCommands = allowOsCommands;
        this.allowedExecutables = allowedExecutables == null
                ? List.of()
                : allowedExecutables.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> Path.of(value.trim()).toAbsolutePath().normalize())
                .toList();
    }

    public void validateExternalCommand(Path executable) {
        if (!allowOsCommands) {
            throw new PolicyException("OS commands are disabled by configuration.");
        }

        if (executable == null) {
            throw new PolicyException("Missing executable path.");
        }

        Path normalized = executable.toAbsolutePath().normalize();
        if (!Files.exists(normalized)) {
            throw new PolicyException("Executable does not exist: " + normalized);
        }
        if (!Files.isRegularFile(normalized)) {
            throw new PolicyException("Executable is not a file: " + normalized);
        }
        if (allowedExecutables.stream().noneMatch(allowed -> allowed.equals(normalized))) {
            throw new PolicyException("Executable is not whitelisted: " + normalized);
        }
    }
}

