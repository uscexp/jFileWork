package com.github.uscexp.jfilework.ui.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Shared setup for M1 main-window UI tests.
 */
public abstract class MainWindowUiTestBase extends FxTestBase {

    protected static Path leftRoot;
    protected static Path rightRoot;

    @BeforeAll
    static void prepareFileSystemFixture() throws IOException {
        leftRoot = Files.createTempDirectory("jfilework-left-");
        rightRoot = Files.createTempDirectory("jfilework-right-");

        Files.createDirectory(leftRoot.resolve("sub"));
        Files.writeString(leftRoot.resolve("alpha.txt"), "alpha");
        Files.writeString(leftRoot.resolve("beta.txt"), "beta");

        Files.writeString(rightRoot.resolve("right.txt"), "right");

        System.setProperty("jfilework.leftStartPath", leftRoot.toString());
        System.setProperty("jfilework.rightStartPath", rightRoot.toString());
    }

    @AfterAll
    static void cleanupFixture() {
        System.clearProperty("jfilework.leftStartPath");
        System.clearProperty("jfilework.rightStartPath");
        System.clearProperty("jfilework.config");
        System.clearProperty("jfilework.session");
        System.clearProperty("jfilework.command.artificialDelayMs");
    }

    protected static Path writeConfigFile(String fileName, String content) throws IOException {
        Path configFile = Files.createTempFile(fileName, ".json");
        Files.writeString(configFile, content, StandardOpenOption.TRUNCATE_EXISTING);
        return configFile;
    }

    protected static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    protected static Path materializePluginDirectory(Path sourceDirectory, String name) throws IOException {
        Path pluginRoot = Files.createTempDirectory("jfilework-plugin-root-");
        if (Files.isDirectory(sourceDirectory)) {
            Path targetDirectory = pluginRoot.resolve(name);
            copyDirectory(sourceDirectory, targetDirectory);
        } else {
            Path targetJar = pluginRoot.resolve(name + ".jar");
            Files.copy(sourceDirectory, targetJar, StandardCopyOption.REPLACE_EXISTING);
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

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/github/uscexp/jfilework/ui/javafx/main-window.fxml")
        );
        stage.setScene(new Scene(loader.load(), 1200, 700));
        stage.show();
    }
}

