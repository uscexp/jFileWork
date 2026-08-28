package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.fs.FileService;
import com.github.uscexp.jfilework.core.fs.LocalFileService;
import com.github.uscexp.jfilework.ui.javafx.model.ActivePaneTracker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for the main application window (main-window.fxml).
 */
public class MainWindowController {

    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    @FXML private MenuBar menuBar;
    @FXML private ToolBar toolBar;
    @FXML private SplitPane splitPane;
    @FXML private VBox leftPane;
    @FXML private VBox rightPane;
    @FXML private Label statusLabel;

    private final FileService fileService = new LocalFileService();
    private final ActivePaneTracker activePaneTracker = new ActivePaneTracker();

    private FilePaneController leftPaneController;
    private FilePaneController rightPaneController;

    @FXML
    public void initialize() {
        try {
            leftPaneController = mountFilePane(leftPane, "left", "Left pane");
            rightPaneController = mountFilePane(rightPane, "right", "Right pane");

            Path leftStartPath = resolveStartPath("jfilework.leftStartPath");
            Path rightStartPath = resolveStartPath("jfilework.rightStartPath");

            leftPaneController.navigateTo(leftStartPath);
            rightPaneController.navigateTo(rightStartPath);

            statusLabel.setText("Ready");
            log.info("Main window initialised with left={} and right={}", leftStartPath, rightStartPath);
        } catch (IOException e) {
            log.error("Cannot initialise file panes", e);
            statusLabel.setText("Cannot initialize UI: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Menu: File
    // -------------------------------------------------------------------------

    @FXML
    private void onExit() {
        if (leftPaneController != null) {
            leftPaneController.stopWatching();
        }
        if (rightPaneController != null) {
            rightPaneController.stopWatching();
        }
        Platform.exit();
    }

    // -------------------------------------------------------------------------
    // Menu: View
    // -------------------------------------------------------------------------

    @FXML
    private void onPreferences() {
        statusLabel.setText("Preferences are available in M3");
    }

    // -------------------------------------------------------------------------
    // Menu: Help
    // -------------------------------------------------------------------------

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About jFileWork");
        alert.setHeaderText("jFileWork  v0.0.1-Alpha");
        alert.setContentText("Dual-pane file manager built with Java 26 and JavaFX 23.");
        alert.showAndWait();
    }

    private FilePaneController mountFilePane(VBox target, String paneId, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/uscexp/jfilework/ui/javafx/file-pane.fxml"));
        Parent paneRoot = loader.load();
        FilePaneController controller = loader.getController();
        controller.configure(paneId, title, fileService, this::updateStatus);

        target.getChildren().setAll(paneRoot);
        target.focusedProperty().addListener((obs, oldV, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
                updateStatus("Active pane: " + activePaneTracker.getActivePane());
            }
        });

        Node table = controller.getFileTable();
        table.focusedProperty().addListener((obs, oldV, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
            }
        });

        return controller;
    }

    private Path resolveStartPath(String propertyKey) {
        String configuredPath = System.getProperty(propertyKey);
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path path = Paths.get(configuredPath.trim());
            if (fileService.exists(path) && fileService.isDirectory(path)) {
                return path;
            }
            log.warn("Configured start path '{}' is invalid for {}", configuredPath, propertyKey);
        }

        return Paths.get(System.getProperty("user.home"));
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}
