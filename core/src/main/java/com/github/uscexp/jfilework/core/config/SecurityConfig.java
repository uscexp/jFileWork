package com.github.uscexp.jfilework.core.config;

import java.util.List;

/**
 * Pragmatic security settings for external process execution.
 */
public record SecurityConfig(
        boolean allowOsCommands,
        List<String> allowedExecutables,
        List<String> allowedRoots
) {

    public static SecurityConfig defaults() {
        return new SecurityConfig(false, List.of(), List.of());
    }
}

