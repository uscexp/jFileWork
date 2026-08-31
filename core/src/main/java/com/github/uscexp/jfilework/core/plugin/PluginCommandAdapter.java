package com.github.uscexp.jfilework.core.plugin;

import com.github.uscexp.jfilework.api.plugin.PluginCancellationToken;
import com.github.uscexp.jfilework.api.plugin.PluginCommand;
import com.github.uscexp.jfilework.api.plugin.PluginCommandContext;
import com.github.uscexp.jfilework.api.plugin.PluginCommandResult;
import com.github.uscexp.jfilework.api.plugin.PluginCommandStatus;
import com.github.uscexp.jfilework.api.plugin.PluginProgressSink;
import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.command.CommandStatus;

import java.util.Map;

/**
 * Bridges a plugin command onto the internal command interface.
 */
public class PluginCommandAdapter implements Command {

    private final PluginCommand delegate;

    public PluginCommandAdapter(PluginCommand delegate) {
        this.delegate = delegate;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public boolean canExecute(CommandContext context) {
        return delegate.canExecute(toPluginContext(context));
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        PluginCommandResult result = delegate.execute(toPluginContext(context), params);
        return new CommandResult(mapStatus(result.status()), result.message(), result.processedItems());
    }

    private PluginCommandContext toPluginContext(CommandContext context) {
        PluginCancellationToken cancellationToken = context.cancellationToken()::isCancelled;
        PluginProgressSink progressSink = new PluginProgressSink() {
            @Override
            public void onStart(String label, long totalWork) {
                context.progressSink().onStart(label, totalWork);
            }

            @Override
            public void onProgress(String label, long completedWork, long totalWork) {
                context.progressSink().onProgress(label, completedWork, totalWork);
            }

            @Override
            public void onCompleted(String label) {
                context.progressSink().onCompleted(label);
            }

            @Override
            public void onFailed(String label, Exception exception) {
                context.progressSink().onFailed(label, exception);
            }
        };

        return new PluginCommandContext(
                context.activeDirectory(),
                context.inactiveDirectory(),
                context.selection().paths(),
                cancellationToken,
                progressSink
        );
    }

    private static CommandStatus mapStatus(PluginCommandStatus status) {
        if (status == null) {
            return CommandStatus.FAILED;
        }
        return switch (status) {
            case SUCCESS -> CommandStatus.SUCCESS;
            case PARTIAL -> CommandStatus.PARTIAL;
            case FAILED -> CommandStatus.FAILED;
            case CANCELLED -> CommandStatus.CANCELLED;
        };
    }
}

