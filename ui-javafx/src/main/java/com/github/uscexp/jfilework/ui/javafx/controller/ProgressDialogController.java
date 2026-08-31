package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.command.CancellationToken;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Small modal progress dialog used by M2 built-in commands.
 */
public class ProgressDialogController {

    private final CancellationToken cancellationToken;
    private final Stage dialogStage;
    private final Label messageLabel;

    public ProgressDialogController(Stage owner, CancellationToken cancellationToken, String title) {
        this.cancellationToken = cancellationToken;
        this.dialogStage = new Stage();
        this.messageLabel = new Label("Working...");

        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle(title);
        dialogStage.setResizable(false);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        Button cancelButton = new Button("Cancel");
        cancelButton.setId("progressCancelButton");
        cancelButton.setOnAction(event -> cancellationToken.cancel());

        VBox root = new VBox(10,
                messageLabel,
                indicator,
                new HBox(cancelButton)
        );
        root.setPadding(new Insets(12));

        dialogStage.setScene(new Scene(root, 260, 130));
    }

    public void show() {
        Platform.runLater(dialogStage::show);
    }

    public void setMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    public void close() {
        Platform.runLater(dialogStage::close);
    }
}

