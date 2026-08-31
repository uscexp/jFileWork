package com.github.uscexp.jfilework.core.command;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation token for long-running commands.
 */
public class CancellationToken {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }
}

