package com.github.uscexp.jfilework.core.plugin;

import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.config.PluginConfig;
import com.github.uscexp.jfilework.sample.hash.HashPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsEnabledPluginAndRegistersItsCommand() throws Exception {
        Path pluginDirectory = materializeSamplePluginDirectory();
        CommandRegistry registry = new CommandRegistry();
        PluginManager pluginManager = new PluginManager(registry);

        List<PluginRuntimeInfo> infos = pluginManager.loadPlugins(
                new PluginConfig(pluginDirectory.toString(), List.of("sample-hash-plugin")),
                "0.0.1-Alpha-SNAPSHOT"
        );

        assertTrue(registry.findById("plugin.hash.sha256").isPresent());
        assertEquals(1, infos.stream().filter(info -> info.status() == PluginStatus.LOADED).count());
        assertTrue(infos.stream().anyMatch(info -> "sample-hash-plugin".equals(info.id())));

        pluginManager.close();
    }

    @Test
    void loadsEnabledPluginFromJarAndRegistersItsCommand() throws Exception {
        Path pluginDirectory = materializeSamplePluginJarDirectory();
        CommandRegistry registry = new CommandRegistry();
        PluginManager pluginManager = new PluginManager(registry);

        List<PluginRuntimeInfo> infos = pluginManager.loadPlugins(
                new PluginConfig(pluginDirectory.toString(), List.of("sample-hash-plugin")),
                "0.0.1-Alpha-SNAPSHOT"
        );

        assertTrue(registry.findById("plugin.hash.sha256").isPresent());
        assertEquals(1, infos.stream().filter(info -> info.status() == PluginStatus.LOADED).count());
        assertTrue(infos.stream().anyMatch(info -> "sample-hash-plugin".equals(info.id())));

        pluginManager.close();
    }

    private Path materializeSamplePluginDirectory() throws Exception {
        Path compiledPluginDir = Path.of(HashPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path pluginRoot = tempDir.resolve("plugins");
        Files.createDirectories(pluginRoot);

        if (Files.isDirectory(compiledPluginDir)) {
            Path targetDir = pluginRoot.resolve("sample-hash-plugin");
            copyDirectory(compiledPluginDir, targetDir);
        } else {
            Path targetJar = pluginRoot.resolve("sample-hash-plugin.jar");
            Files.copy(compiledPluginDir, targetJar, StandardCopyOption.REPLACE_EXISTING);
        }
        return pluginRoot;
    }

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

    private Path materializeSamplePluginJarDirectory() throws Exception {
        Path compiledPluginDir = Path.of(HashPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path pluginRoot = tempDir.resolve("plugins-jar");
        Files.createDirectories(pluginRoot);
        Path targetJar = pluginRoot.resolve("sample-hash-plugin.jar");

        if (Files.isDirectory(compiledPluginDir)) {
            createJarFromDirectory(compiledPluginDir, targetJar);
        } else {
            Files.copy(compiledPluginDir, targetJar, StandardCopyOption.REPLACE_EXISTING);
        }
        return pluginRoot;
    }

    private static void createJarFromDirectory(Path sourceDir, Path jarPath) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(jarPath);
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream);
             var stream = Files.walk(sourceDir)) {

            for (Path current : stream.toList()) {
                if (Files.isDirectory(current)) {
                    continue;
                }

                String entryName = sourceDir.relativize(current).toString().replace('\\', '/');
                JarEntry jarEntry = new JarEntry(entryName);
                jarOutputStream.putNextEntry(jarEntry);
                try (InputStream inputStream = Files.newInputStream(current)) {
                    inputStream.transferTo(jarOutputStream);
                }
                jarOutputStream.closeEntry();
            }
        }
    }
}

