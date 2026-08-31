package com.github.uscexp.jfilework.core.config;

/**
 * UI related user configuration.
 */
public record UiConfig(
        String theme,
        boolean showHiddenFiles,
        String leftStartPath,
        String rightStartPath
) {

    public static UiConfig defaults() {
        return new UiConfig("system", false, null, null);
    }
}

