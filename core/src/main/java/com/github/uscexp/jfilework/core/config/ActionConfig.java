package com.github.uscexp.jfilework.core.config;

import java.util.List;
import java.util.Map;

/**
 * Declarative binding between UI element and command invocation.
 */
public record ActionConfig(
        String id,
        String label,
        String commandId,
        Map<String, String> params,
        String shortcut,
        List<String> placement
) {
}

