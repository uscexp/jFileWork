package com.github.uscexp.jfilework.core.command;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executes commands asynchronously to keep the UI thread responsive.
 */
public class CommandExecutor implements AutoCloseable {

    private final ExecutorService executorService;

    public CommandExecutor() {
        this(Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2)));
    }

    public CommandExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public CompletableFuture<CommandResult> execute(Command command, CommandContext context, Map<String, String> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!command.canExecute(context)) {
                    return CommandResult.failed("Command cannot be executed in the current context: " + command.id());
                }
                return command.execute(context, params == null ? Map.of() : params);
            } catch (Exception exception) {
                context.progressSink().onFailed(command.id(), exception);
                return CommandResult.failed(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            }
        }, executorService);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }
}

