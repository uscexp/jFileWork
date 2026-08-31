package com.github.uscexp.jfilework.sample.hash;

import com.github.uscexp.jfilework.api.plugin.PluginCommand;
import com.github.uscexp.jfilework.api.plugin.PluginCommandContext;
import com.github.uscexp.jfilework.api.plugin.PluginCommandResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Calculates SHA-256 hashes for the selected files.
 */
public class Sha256Command implements PluginCommand {

    @Override
    public String id() {
        return "plugin.hash.sha256";
    }

    @Override
    public PluginCommandResult execute(PluginCommandContext context, Map<String, String> params) throws Exception {
        List<Path> selected = context.selection();
        if (selected.isEmpty()) {
            return PluginCommandResult.failed("No files selected for hashing.");
        }

        context.progressSink().onStart("SHA-256", selected.size());
        StringBuilder message = new StringBuilder();
        int processed = 0;

        for (Path path : selected) {
            if (context.cancellationToken().isCancelled()) {
                return new PluginCommandResult(com.github.uscexp.jfilework.api.plugin.PluginCommandStatus.CANCELLED,
                        "Hash calculation cancelled.", processed);
            }
            if (!Files.isRegularFile(path)) {
                continue;
            }
            processed++;
            String hash = sha256(path);
            if (!message.isEmpty()) {
                message.append(" | ");
            }
            message.append(path.getFileName()).append(": ").append(hash);
            context.progressSink().onProgress("SHA-256", processed, selected.size());
        }

        context.progressSink().onCompleted("SHA-256");
        return PluginCommandResult.success("SHA-256 " + message, processed);
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (InputStream inputStream = Files.newInputStream(path)) {
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                if (read > 0) {
                    digest.update(buffer, 0, read);
                }
            }
        } catch (IOException exception) {
            throw new IOException("Cannot hash file: " + path, exception);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}

