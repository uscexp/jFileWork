package com.github.uscexp.jfilework.core.integration;

import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandExecutor;
import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.command.CommandStatus;
import com.github.uscexp.jfilework.core.command.CancellationToken;
import com.github.uscexp.jfilework.core.command.ProcessOutputSink;
import com.github.uscexp.jfilework.core.command.ProgressSink;
import com.github.uscexp.jfilework.core.command.SelectionSnapshot;
import com.github.uscexp.jfilework.core.command.builtin.CopyCommand;
import com.github.uscexp.jfilework.core.command.builtin.MoveCommand;
import com.github.uscexp.jfilework.core.config.PluginConfig;
import com.github.uscexp.jfilework.core.plugin.PluginManager;
import com.github.uscexp.jfilework.core.plugin.PluginStatus;
import com.github.uscexp.jfilework.sample.hash.HashPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration tests for the most critical M6 command flows.
 */
class CriticalFlowsIntegrationTest {

    private static final long EXECUTION_TIMEOUT_SECONDS = 10L;

    @TempDir
    Path tempDir;

    /**
     * Verifies file copy through registry lookup and asynchronous executor path.
     */
    @Test
    void copyFlow_viaRegistryAndExecutorCopiesSelectedFiles() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("alpha.txt"), "alpha");

        CommandRegistry registry = new CommandRegistry();
        registry.register(new CopyCommand());

        CommandResult result = execute(registry, sourceDir, targetDir, List.of(sourceFile), "file.copy", Map.of());

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertTrue(Files.exists(targetDir.resolve("alpha.txt")));
        assertEquals("alpha", Files.readString(targetDir.resolve("alpha.txt")));
    }

    /**
     * Verifies file move through registry lookup and asynchronous executor path.
     */
    @Test
    void moveFlow_viaRegistryAndExecutorMovesSelectedFiles() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("move-me.txt"), "move");

        CommandRegistry registry = new CommandRegistry();
        registry.register(new MoveCommand());

        CommandResult result = execute(registry, sourceDir, targetDir, List.of(sourceFile), "file.move", Map.of());

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertFalse(Files.exists(sourceFile));
        assertTrue(Files.exists(targetDir.resolve("move-me.txt")));
        assertEquals("move", Files.readString(targetDir.resolve("move-me.txt")));
    }

    /**
     * Verifies plugin discovery/registration and execution of a contributed command.
     */
    @Test
    void pluginLoadFlow_loadsDemoPluginAndExecutesRegisteredCommand() throws Exception {
        Path pluginRoot = materializeSamplePluginDirectory();
        Path sourceDir = Files.createDirectory(tempDir.resolve("left"));
        Path targetDir = Files.createDirectory(tempDir.resolve("right"));
        Path sourceFile = Files.writeString(sourceDir.resolve("hash-me.txt"), "hash me");

        CommandRegistry registry = new CommandRegistry();
        try (PluginManager pluginManager = new PluginManager(registry);
             CommandExecutor executor = new CommandExecutor()) {

            var infos = pluginManager.loadPlugins(new PluginConfig(pluginRoot.toString(), List.of("sample-hash-plugin")), "0.0.1-Alpha-SNAPSHOT");
            assertTrue(infos.stream().anyMatch(info -> "sample-hash-plugin".equals(info.id()) && info.status() == PluginStatus.LOADED));

            CommandResult result = execute(registry, sourceDir, targetDir, List.of(sourceFile), "plugin.hash.sha256", Map.of(), executor);
            assertEquals(CommandStatus.SUCCESS, result.status());
            assertEquals(1L, result.processedItems());
            assertNotNull(result.message());
            assertTrue(result.message().contains("hash-me.txt"));
            assertTrue(result.message().contains("SHA-256"));
        }
    }

    /**
     * Executes the command with a short-lived executor owned by this helper.
     */
    private CommandResult execute(CommandRegistry registry,
                                  Path activeDir,
                                  Path inactiveDir,
                                  List<Path> selectedPaths,
                                  String commandId,
                                  Map<String, String> params) throws Exception {
        try (CommandExecutor executor = new CommandExecutor()) {
            return execute(registry, activeDir, inactiveDir, selectedPaths, commandId, params, executor);
        }
    }

    /**
     * Executes the command with the provided executor to support multi-step scenarios.
     */
    private CommandResult execute(CommandRegistry registry,
                                  Path activeDir,
                                  Path inactiveDir,
                                  List<Path> selectedPaths,
                                  String commandId,
                                  Map<String, String> params,
                                  CommandExecutor executor) throws Exception {
        var command = registry.findById(commandId).orElseThrow(() -> new IllegalStateException("Missing command: " + commandId));
        CommandContext context = new CommandContext(
                activeDir,
                inactiveDir,
                new SelectionSnapshot(selectedPaths),
                new CancellationToken(),
                ProgressSink.NO_OP,
                ProcessOutputSink.NO_OP
        );

        return executor.execute(command, context, params).get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Creates a temporary plugin directory from the sample plugin test artifact.
     */
    private Path materializeSamplePluginDirectory() throws Exception {
        Path compiledPluginLocation = Path.of(HashPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path pluginRoot = tempDir.resolve("plugins");
        Files.createDirectories(pluginRoot);

        if (Files.isDirectory(compiledPluginLocation)) {
            copyDirectory(compiledPluginLocation, pluginRoot.resolve("sample-hash-plugin"));
        } else {
            Files.copy(compiledPluginLocation, pluginRoot.resolve("sample-hash-plugin.jar"), StandardCopyOption.REPLACE_EXISTING);
        }

        return pluginRoot;
    }

    /**
     * Copies all files from source to target while preserving relative paths.
     */
    private static void copyDirectory(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            for (Path current : stream.toList()) {
                Path relative = source.relativize(current);
                Path destination = target.resolve(relative.toString());
                if (Files.isDirectory(current)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(current, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}

