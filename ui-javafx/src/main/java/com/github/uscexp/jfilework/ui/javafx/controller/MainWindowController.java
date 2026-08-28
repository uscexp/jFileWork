package com.github.uscexp.jfilework.ui.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main application window (main-window.fxml).
 *
 * <p>M0: placeholder panels only. Full file-pane integration follows in M1.</p>
 */
public class MainWindowController {

    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    @FXML private MenuBar  menuBar;
    @FXML private ToolBar  toolBar;
    @FXML private SplitPane splitPane;
    @FXML private Label    leftPanePlaceholder;
    @FXML private Label    rightPanePlaceholder;
    @FXML private Label    statusLabel;

    @FXML
    public void initialize() {
        log.info("Main window initialised (M0 — placeholder UI).");
        statusLabel.setText("Ready  ·  jFileWork v0.0.1-Alpha");
    }

    // -------------------------------------------------------------------------
    // Menu: File
    // -------------------------------------------------------------------------

    @FXML
    private void onExit() {
        log.info("Exit requested.");
        Platform.exit();
    }

    // -------------------------------------------------------------------------
    // Menu: View
    // -------------------------------------------------------------------------

    @FXML
    private void onPreferences() {
        statusLabel.setText("Preferences — available in M3");
    }

    // -------------------------------------------------------------------------
    // Menu: Help
    // -------------------------------------------------------------------------

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About jFileWork");
        alert.setHeaderText("jFileWork  v0.0.1-Alpha");
        alert.setContentText("""
                A dual-pane file manager built with Java 26 & JavaFX 23.

                Architecture: Multi-module Maven project
                  • plugin-api   — stable plugin contract
                  • core         — command engine, file services
                  • ui-javafx    — this window

                Development plan: see PLAN.md
                """);
        alert.showAndWait();
    }
}

