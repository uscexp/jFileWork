package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Creates a directory in the active pane.
 */
public class MkdirCommand implements Command {

    @Override
    public String id() {
        return "file.mkdir";
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        String name = params.get("name");
        if (name == null || name.isBlank()) {
            return CommandResult.failed("Missing directory name.");
        }

        Path targetDirectory = FileCommandSupport.resolveTargetDirectory(context.activeDirectory(), params);
        FileCommandSupport.ensureDirectory(targetDirectory);

        Path newDirectory = targetDirectory.resolve(name.trim());
        if (Files.exists(newDirectory)) {
            return CommandResult.failed("Directory already exists: " + newDirectory.getFileName());
        }

        Files.createDirectories(newDirectory);
        return CommandResult.success("Created directory: " + newDirectory.getFileName(), 1L);
    }
}

