package com.github.uscexp.jfilework.api.plugin;

/**
 * Progress callback exposed to plugin commands.
 */
public interface PluginProgressSink {

    PluginProgressSink NO_OP = new PluginProgressSink() { };

    default void onStart(String label, long totalWork) {
    }

    default void onProgress(String label, long completedWork, long totalWork) {
    }

    default void onCompleted(String label) {
    }

    default void onFailed(String label, Exception exception) {
    }
}

