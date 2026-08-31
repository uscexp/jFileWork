package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Renames a single selected file or directory inside the active pane.
 */
public class RenameCommand implements Command {

    @Override
    public String id() {
        return "file.rename";
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        List<Path> selectedPaths = context.selection().paths();
        if (selectedPaths.size() != 1) {
            return CommandResult.failed("Rename requires exactly one selected item.");
        }

        String newName = params.get("name");
        if (newName == null || newName.isBlank()) {
            return CommandResult.failed("Missing new name.");
        }

        Path source = selectedPaths.getFirst();
        Path parent = source.getParent();
        if (parent == null) {
            return CommandResult.failed("Cannot rename root path.");
        }

        Path target = parent.resolve(newName.trim());
        if (Files.exists(target)) {
            return CommandResult.failed("Target already exists: " + target.getFileName());
        }

        Files.move(source, target);
        return CommandResult.success("Renamed to: " + target.getFileName(), 1L);
    }
}

