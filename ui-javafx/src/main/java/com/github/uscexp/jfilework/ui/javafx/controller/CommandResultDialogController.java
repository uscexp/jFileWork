package com.github.uscexp.jfilework.ui.javafx.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Simple read-only dialog for command result messages.
 */
public class CommandResultDialogController {

    public void showDialog(Stage owner, String title, String message) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);

        TextArea resultArea = new TextArea(message == null ? "" : message);
        resultArea.setId("commandResultArea");
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        Button closeButton = new Button("OK");
        closeButton.setId("commandResultCloseButton");
        closeButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(8, resultArea, closeButton);
        root.setPadding(new Insets(10));
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        dialog.setScene(new Scene(root, 560, 280));
        dialog.showAndWait();
    }
}

