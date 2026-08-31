package com.github.uscexp.jfilework.core.command;

import com.github.uscexp.jfilework.core.command.builtin.OsExecCommand;
import com.github.uscexp.jfilework.core.security.CommandPolicy;
import com.github.uscexp.jfilework.core.security.ParamSanitizer;
import com.github.uscexp.jfilework.core.security.PathPolicy;
import com.github.uscexp.jfilework.core.security.PolicyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsExecCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void executesWhitelistedCommandAndStreamsOutput() throws Exception {
        Path active = Files.createDirectory(tempDir.resolve("active"));
        Path inactive = Files.createDirectory(tempDir.resolve("inactive"));
        Path javaExe = javaExecutable();

        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        CommandContext context = new CommandContext(
                active,
                inactive,
                new SelectionSnapshot(List.of()),
                new CancellationToken(),
                ProgressSink.NO_OP,
                new ProcessOutputSink() {
                    @Override
                    public void onStdOut(String line) {
                        stdout.add(line);
                    }

                    @Override
                    public void onStdErr(String line) {
                        stderr.add(line);
                    }
                }
        );

        OsExecCommand command = new OsExecCommand(
                new CommandPolicy(true, List.of(javaExe.toString())),
                new PathPolicy(List.of(tempDir.toString())),
                new ParamSanitizer()
        );

        String args = "-cp \"" + System.getProperty("java.class.path") + "\" com.github.uscexp.jfilework.core.command.ProcessEchoMain";
        CommandResult result = command.execute(context, Map.of("exe", javaExe.toString(), "args", args));

        assertEquals(CommandStatus.SUCCESS, result.status());
        assertTrue(stdout.stream().anyMatch(line -> line.contains("hello-out")));
        assertTrue(stderr.stream().anyMatch(line -> line.contains("hello-err")));
    }

    @Test
    void rejectsNonWhitelistedExecutable() throws Exception {
        Path active = Files.createDirectory(tempDir.resolve("active"));
        Path inactive = Files.createDirectory(tempDir.resolve("inactive"));
        Path javaExe = javaExecutable();

        OsExecCommand command = new OsExecCommand(
                new CommandPolicy(true, List.of()),
                new PathPolicy(List.of(tempDir.toString())),
                new ParamSanitizer()
        );

        PolicyException exception = assertThrows(PolicyException.class, () -> command.execute(new CommandContext(
                        active,
                        inactive,
                        new SelectionSnapshot(List.of()),
                        new CancellationToken(),
                        ProgressSink.NO_OP,
                        ProcessOutputSink.NO_OP),
                Map.of("exe", javaExe.toString(), "args", "-version")));

        assertTrue(exception.getMessage().contains("whitelisted")
                || exception.getMessage().contains("disabled")
                || exception.getMessage().contains("exist"));
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable);
    }
}


