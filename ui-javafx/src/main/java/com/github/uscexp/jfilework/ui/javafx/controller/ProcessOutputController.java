package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.command.CancellationToken;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Displays live stdout/stderr output of an external process and allows terminating it.
 */
public class ProcessOutputController {

    private final CancellationToken cancellationToken;
    private final Stage dialogStage;
    private final TextArea outputArea;
    private final Button killButton;
    private Process process;

    public ProcessOutputController(Stage owner, CancellationToken cancellationToken, String title) {
        this.cancellationToken = cancellationToken;
        this.dialogStage = new Stage();
        this.outputArea = new TextArea();
        this.killButton = new Button("Kill Process");

        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle(title);
        dialogStage.setMinWidth(640);
        dialogStage.setMinHeight(360);

        outputArea.setId("processOutputArea");
        outputArea.setEditable(false);
        outputArea.setWrapText(false);

        killButton.setId("processKillButton");
        killButton.setOnAction(event -> {
            cancellationToken.cancel();
            if (process != null) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setId("processCloseButton");
        closeButton.setOnAction(event -> dialogStage.close());

        HBox buttons = new HBox(8, killButton, closeButton);
        VBox root = new VBox(8, outputArea, buttons);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        root.setPadding(new Insets(10));

        dialogStage.setScene(new Scene(root, 760, 420));
    }

    public void show() {
        Platform.runLater(dialogStage::show);
    }

    public void bindProcess(Process process) {
        this.process = process;
        show();
        appendLine("[info] Process started: pid=" + process.pid());
    }

    public boolean hasProcess() {
        return process != null;
    }

    public void appendStdOut(String line) {
        appendLine("[out] " + line);
    }

    public void appendStdErr(String line) {
        appendLine("[err] " + line);
    }

    public void markFinished(int exitCode) {
        appendLine("[info] Process finished with exit code " + exitCode);
        Platform.runLater(() -> killButton.setDisable(true));
    }

    public void markFailed(String message) {
        appendLine("[error] " + message);
        Platform.runLater(() -> killButton.setDisable(true));
    }

    private void appendLine(String text) {
        Platform.runLater(() -> outputArea.appendText(text + System.lineSeparator()));
    }
}


