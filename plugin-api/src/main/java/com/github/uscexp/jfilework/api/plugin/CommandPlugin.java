package com.github.uscexp.jfilework.api.plugin;

import java.util.List;

/**
 * Service-provider interface for external jFileWork plugins.
 */
public interface CommandPlugin {

    PluginDescriptor descriptor();

    void initialize(PluginContext context);

    List<PluginCommand> commands();

    default void destroy() {
    }
}

