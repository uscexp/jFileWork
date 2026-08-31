package com.github.uscexp.jfilework.core.command;

/**
 * Receives live process output for commands that execute external processes.
 */
public interface ProcessOutputSink {

    ProcessOutputSink NO_OP = new ProcessOutputSink() { };

    default void onProcessStarted(Process process) {
    }

    default void onStdOut(String line) {
    }

    default void onStdErr(String line) {
    }

    default void onFinished(int exitCode) {
    }
}

