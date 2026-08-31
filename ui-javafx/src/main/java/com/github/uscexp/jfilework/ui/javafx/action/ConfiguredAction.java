package com.github.uscexp.jfilework.ui.javafx.action;

import java.util.List;
import java.util.Map;

/**
 * Runtime action model generated from configuration.
 */
public record ConfiguredAction(
        String id,
        String label,
        String commandId,
        Map<String, String> params,
        String shortcut,
        List<String> placement,
        boolean enabled
) {
}

