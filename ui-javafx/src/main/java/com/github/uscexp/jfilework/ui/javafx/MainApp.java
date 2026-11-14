package com.github.uscexp.jfilework.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX Application entry point.
 * Use {@link MainLauncher} to start the application from the command line.
 */
public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting jFileWork...");
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("main-window.fxml")
        );
        Scene scene = new Scene(loader.load(), 1200, 700);
        primaryStage.setTitle("jFileWork");
        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/image/disk.png"))
        );
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.setScene(scene);
        primaryStage.show();
        log.info("jFileWork main window opened.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

