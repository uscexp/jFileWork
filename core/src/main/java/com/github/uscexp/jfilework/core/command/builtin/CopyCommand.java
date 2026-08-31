package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Copies selected files/directories from active pane to target directory.
 */
public class CopyCommand implements Command {

    @Override
    public String id() {
        return "file.copy";
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        List<Path> selectedPaths = context.selection().paths();
        if (selectedPaths.isEmpty()) {
            return CommandResult.failed("No files selected.");
        }

        Path targetDirectory = FileCommandSupport.resolveTargetDirectory(context.inactiveDirectory(), params);
        FileCommandSupport.ensureDirectory(targetDirectory);

        long processed = 0L;
        context.progressSink().onStart("Copy", selectedPaths.size());

        for (Path source : selectedPaths) {
            if (context.cancellationToken().isCancelled()) {
                return CommandResult.cancelled("Copy cancelled.", processed);
            }

            Path target = targetDirectory.resolve(source.getFileName());
            if (source.normalize().equals(target.normalize())) {
                continue;
            }

            FileCommandSupport.copyRecursively(source, target, context.cancellationToken());
            processed++;
            context.progressSink().onProgress("Copy", processed, selectedPaths.size());
        }

        context.progressSink().onCompleted("Copy");
        return CommandResult.success("Copied " + processed + " item(s).", processed);
    }
}

