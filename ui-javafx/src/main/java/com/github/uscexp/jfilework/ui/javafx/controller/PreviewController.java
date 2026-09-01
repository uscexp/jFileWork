package com.github.uscexp.jfilework.ui.javafx.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Lightweight preview dialog for text and image files.
 */
public class PreviewController {

    private static final int MAX_TEXT_LINES = 200;
    private static final long MAX_TEXT_BYTES = 2L * 1024L * 1024L;
    private static final int BINARY_SNIFF_BYTES = 1024;

    public void showPreview(Stage owner, Path path) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setTitle("Preview: " + path.getFileName());
        stage.setMinWidth(640);
        stage.setMinHeight(420);

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));

        Label pathLabel = new Label(path.toString());
        root.getChildren().add(pathLabel);

        if (Files.isDirectory(path)) {
            root.getChildren().add(new Label("Directory preview is not supported."));
        } else if (isImage(path)) {
            Image image = new Image(path.toUri().toString(), true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(900);
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            root.getChildren().add(scrollPane);
        } else {
            TextArea textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setId("previewTextArea");
            textArea.setText(loadTextPreview(path));
            VBox.setVgrow(textArea, Priority.ALWAYS);
            root.getChildren().add(textArea);
        }

        Scene scene = new Scene(root, 900, 560);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
                event.consume();
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    private static boolean isImage(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".bmp")
                || lower.endsWith(".svg");
    }

    private static String loadTextPreview(Path path) {
        try {
            long fileSize = Files.size(path);
            if (fileSize > MAX_TEXT_BYTES) {
                return "File is too large for text preview (" + fileSize + " bytes).";
            }
            if (isProbablyBinary(path)) {
                return "Binary file preview is not supported.";
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.size() <= MAX_TEXT_LINES) {
                return String.join(System.lineSeparator(), lines);
            }
            List<String> firstLines = lines.subList(0, MAX_TEXT_LINES);
            return String.join(System.lineSeparator(), firstLines)
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "... truncated after " + MAX_TEXT_LINES + " lines ...";
        } catch (IOException exception) {
            return "Cannot preview file: " + exception.getMessage();
        }
    }

    private static boolean isProbablyBinary(Path path) throws IOException {
        try (var stream = Files.newInputStream(path)) {
            byte[] buffer = new byte[BINARY_SNIFF_BYTES];
            int read = stream.read(buffer);
            if (read <= 0) {
                return false;
            }

            int suspicious = 0;
            for (int index = 0; index < read; index++) {
                byte value = buffer[index];
                if (value == 0) {
                    return true;
                }
                if ((value < 0x09) || (value > 0x0D && value < 0x20)) {
                    suspicious++;
                }
            }
            return suspicious > read / 3;
        }
    }
}


