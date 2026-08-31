package com.github.uscexp.jfilework.core.command.builtin;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.security.CommandPolicy;
import com.github.uscexp.jfilework.core.security.ParamSanitizer;
import com.github.uscexp.jfilework.core.security.PathPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Executes a whitelisted local OS command without going through a shell.
 */
public class OsExecCommand implements Command {

    private final CommandPolicy commandPolicy;
    private final PathPolicy pathPolicy;
    private final ParamSanitizer paramSanitizer;

    public OsExecCommand(CommandPolicy commandPolicy, PathPolicy pathPolicy, ParamSanitizer paramSanitizer) {
        this.commandPolicy = commandPolicy;
        this.pathPolicy = pathPolicy;
        this.paramSanitizer = paramSanitizer;
    }

    @Override
    public String id() {
        return "os.exec";
    }

    @Override
    public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
        String exe = params.get("exe");
        String args = params.getOrDefault("args", "");
        if (exe == null || exe.isBlank()) {
            return CommandResult.failed("Missing executable path.");
        }

        pathPolicy.validatePath(context.activeDirectory());
        pathPolicy.validatePath(context.inactiveDirectory());
        for (Path selectedPath : context.selection().paths()) {
            pathPolicy.validatePath(selectedPath);
        }

        Path executable = Path.of(exe.trim());
        commandPolicy.validateExternalCommand(executable);

        List<String> commandLine = new ArrayList<>();
        commandLine.add(executable.toAbsolutePath().normalize().toString());
        commandLine.addAll(paramSanitizer.tokenizeArguments(args));

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.directory(context.activeDirectory().toFile());

        context.progressSink().onStart("OS command", 1);
        Process process = processBuilder.start();
        context.processOutputSink().onProcessStarted(process);

        CountDownLatch latch = new CountDownLatch(2);
        Thread stdOutThread = createReaderThread(process, false, context, latch);
        Thread stdErrThread = createReaderThread(process, true, context, latch);
        stdOutThread.start();
        stdErrThread.start();

        int exitCode = waitForProcess(process, context);
        latch.await();
        context.processOutputSink().onFinished(exitCode);

        if (context.cancellationToken().isCancelled()) {
            return CommandResult.cancelled("OS command cancelled.", 0L);
        }
        if (exitCode != 0) {
            return CommandResult.failed("OS command exited with code " + exitCode + ".");
        }

        context.progressSink().onCompleted("OS command");
        return CommandResult.success("OS command finished successfully.", 1L);
    }

    private Thread createReaderThread(Process process, boolean stdErr, CommandContext context, CountDownLatch latch) {
        Runnable reader = () -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    stdErr ? process.getErrorStream() : process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (stdErr) {
                        context.processOutputSink().onStdErr(line);
                    } else {
                        context.processOutputSink().onStdOut(line);
                    }
                }
            } catch (IOException ignored) {
                // Stream might be closed by process termination.
            } finally {
                latch.countDown();
            }
        };

        Thread thread = new Thread(reader, stdErr ? "os-exec-stderr" : "os-exec-stdout");
        thread.setDaemon(true);
        return thread;
    }

    private int waitForProcess(Process process, CommandContext context) throws InterruptedException {
        while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
            if (context.cancellationToken().isCancelled()) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                process.waitFor();
                break;
            }
        }
        return process.waitFor();
    }
}


