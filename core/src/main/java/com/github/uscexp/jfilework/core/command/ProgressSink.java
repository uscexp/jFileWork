package com.github.uscexp.jfilework.core.command;

/**
 * Callback sink used by commands to report execution progress.
 */
public interface ProgressSink {

    ProgressSink NO_OP = new ProgressSink() { };

    default void onStart(String label, long totalWork) {
    }

    default void onProgress(String label, long completedWork, long totalWork) {
    }

    default void onCompleted(String label) {
    }

    default void onFailed(String label, Exception exception) {
    }
}

