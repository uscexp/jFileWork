package com.github.uscexp.jfilework.sample.hash;

import com.github.uscexp.jfilework.api.plugin.PluginCancellationToken;
import com.github.uscexp.jfilework.api.plugin.PluginCommandContext;
import com.github.uscexp.jfilework.api.plugin.PluginCommandResult;
import com.github.uscexp.jfilework.api.plugin.PluginCommandStatus;
import com.github.uscexp.jfilework.api.plugin.PluginProgressSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sha256CommandTest {

    @TempDir
    Path tempDir;

    @Test
    void hashesSelectedFiles() throws Exception {
        Path file = Files.writeString(tempDir.resolve("alpha.txt"), "alpha");
        Sha256Command command = new Sha256Command();

        PluginCommandResult result = command.execute(new PluginCommandContext(
                tempDir,
                tempDir,
                List.of(file),
                new PluginCancellationToken() {
                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                },
                PluginProgressSink.NO_OP
        ), Map.of());

        assertEquals(PluginCommandStatus.SUCCESS, result.status());
        assertTrue(result.message().contains("alpha.txt"));
        assertTrue(result.message().contains("8ed3f6ad"));
    }
}

