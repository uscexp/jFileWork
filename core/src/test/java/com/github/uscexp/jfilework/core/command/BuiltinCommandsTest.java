package com.github.uscexp.jfilework.core.command;

import com.github.uscexp.jfilework.core.command.builtin.CopyCommand;
import com.github.uscexp.jfilework.core.command.builtin.DeleteCommand;
import com.github.uscexp.jfilework.core.command.builtin.MkdirCommand;
import com.github.uscexp.jfilework.core.command.builtin.MoveCommand;
import com.github.uscexp.jfilework.core.command.builtin.RenameCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinCommandsTest {

    @TempDir
    Path tempDir;

    @Test
    void copyCommand_copiesSelectedFilesToInactiveDirectory() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("alpha.txt"), "alpha");

        CommandContext context = context(sourceDir, targetDir, List.of(sourceFile));
        CommandResult result = new CopyCommand().execute(context, Map.of());

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertTrue(Files.exists(targetDir.resolve("alpha.txt")));
    }

    @Test
    void moveCommand_movesSelectedFilesToInactiveDirectory() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("move-me.txt"), "move");

        CommandContext context = context(sourceDir, targetDir, List.of(sourceFile));
        CommandResult result = new MoveCommand().execute(context, Map.of());

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertFalse(Files.exists(sourceFile));
        assertTrue(Files.exists(targetDir.resolve("move-me.txt")));
    }

    @Test
    void mkdirCommand_createsDirectoryInActivePane() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));

        CommandContext context = context(sourceDir, targetDir, List.of());
        CommandResult result = new MkdirCommand().execute(context, Map.of("name", "new-folder"));

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertTrue(Files.isDirectory(sourceDir.resolve("new-folder")));
    }

    @Test
    void deleteCommand_removesSelectedFile() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("delete-me.txt"), "delete");

        CommandContext context = context(sourceDir, targetDir, List.of(sourceFile));
        CommandResult result = new DeleteCommand().execute(context, Map.of());

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertFalse(Files.exists(sourceFile));
    }

    @Test
    void renameCommand_renamesSingleSelectedFile() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("old.txt"), "data");

        CommandContext context = context(sourceDir, targetDir, List.of(sourceFile));
        CommandResult result = new RenameCommand().execute(context, Map.of("name", "new.txt"));

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertFalse(Files.exists(sourceFile));
        assertTrue(Files.exists(sourceDir.resolve("new.txt")));
    }

    private static CommandContext context(Path activeDir, Path inactiveDir, List<Path> selectedPaths) {
        return new CommandContext(
                activeDir,
                inactiveDir,
                new SelectionSnapshot(selectedPaths),
                new CancellationToken(),
                ProgressSink.NO_OP
        );
    }
}

