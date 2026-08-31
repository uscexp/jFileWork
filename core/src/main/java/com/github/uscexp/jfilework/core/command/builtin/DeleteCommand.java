package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Deletes selected files/directories from active pane.
 */
public class DeleteCommand implements Command {

    @Override
    public String id() {
        return "file.delete";
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        List<Path> selectedPaths = context.selection().paths();
        if (selectedPaths.isEmpty()) {
            return CommandResult.failed("No files selected.");
        }

        long processed = 0L;
        context.progressSink().onStart("Delete", selectedPaths.size());

        for (Path source : selectedPaths) {
            if (context.cancellationToken().isCancelled()) {
                return CommandResult.cancelled("Delete cancelled.", processed);
            }
            FileCommandSupport.deleteRecursively(source, context.cancellationToken());
            processed++;
            context.progressSink().onProgress("Delete", processed, selectedPaths.size());
        }

        context.progressSink().onCompleted("Delete");
        return CommandResult.success("Deleted " + processed + " item(s).", processed);
    }
}

