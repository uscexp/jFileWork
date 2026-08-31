package com.github.uscexp.jfilework.core.config;

import java.util.List;

/**
 * Root application configuration object.
 */
public record AppConfig(
        UiConfig ui,
        List<ActionConfig> actions,
        SecurityConfig security
) {

    public static AppConfig defaults() {
        return new AppConfig(UiConfig.defaults(), List.of(), SecurityConfig.defaults());
    }
}

