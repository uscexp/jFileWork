package com.github.uscexp.jfilework.ui.javafx.session;

/**
 * Persisted UI session data.
 */
public record SessionState(
        double windowX,
        double windowY,
        double windowWidth,
        double windowHeight,
        String leftPath,
        String rightPath
) {
}

