package com.github.uscexp.jfilework.core.fs;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple watch service wrapper for refreshing a pane when directory content changes.
 */
public class FileWatcherService implements Closeable {

    private final WatchService watchService;
    private final ExecutorService executor;
    private final AtomicBoolean running;

    public FileWatcherService() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "jfilework-watch-service");
            thread.setDaemon(true);
            return thread;
        });
        this.running = new AtomicBoolean(false);
    }

    public void watch(Path directory, Runnable onChange) throws IOException {
        directory.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );

        if (running.compareAndSet(false, true)) {
            executor.submit(() -> {
                while (running.get()) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    boolean changed = false;
                    for (WatchEvent<?> ignored : key.pollEvents()) {
                        changed = true;
                    }

                    key.reset();
                    if (changed) {
                        onChange.run();
                    }
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        executor.shutdownNow();
        watchService.close();
    }
}

