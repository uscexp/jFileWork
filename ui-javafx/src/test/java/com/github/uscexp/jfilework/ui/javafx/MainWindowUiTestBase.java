package com.github.uscexp.jfilework.ui.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

