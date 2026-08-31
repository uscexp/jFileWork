package com.github.uscexp.jfilework.api.plugin;

/**
 * Cooperative cancellation abstraction exposed to plugins.
 */
public interface PluginCancellationToken {

    boolean isCancelled();
}

