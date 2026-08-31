package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * Moves selected files/directories from active pane to target directory.
 */
public class MoveCommand implements Command {

    @Override
    public String id() {
        return "file.move";
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
        context.progressSink().onStart("Move", selectedPaths.size());

        for (Path source : selectedPaths) {
            if (context.cancellationToken().isCancelled()) {
                return CommandResult.cancelled("Move cancelled.", processed);
            }

            Path target = targetDirectory.resolve(source.getFileName());
            if (source.normalize().equals(target.normalize())) {
                continue;
            }

            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception exception) {
                FileCommandSupport.copyRecursively(source, target, context.cancellationToken());
                FileCommandSupport.deleteRecursively(source, context.cancellationToken());
            }

            processed++;
            context.progressSink().onProgress("Move", processed, selectedPaths.size());
        }

        context.progressSink().onCompleted("Move");
        return CommandResult.success("Moved " + processed + " item(s).", processed);
    }
}

