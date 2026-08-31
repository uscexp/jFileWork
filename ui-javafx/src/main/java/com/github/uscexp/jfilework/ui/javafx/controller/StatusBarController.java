package com.github.uscexp.jfilework.ui.javafx.controller;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Minimal status bar adapter to centralize UI-safe status updates.
 */
public class StatusBarController {

    private Label statusLabel;

    public void bind(Label statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void showMessage(String message) {
        if (statusLabel == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(message);
        } else {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }
}

