package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.config.UiConfig;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * M3 preferences dialog for basic UI settings.
 */
public class PreferencesController {

    public Optional<UiConfig> showDialog(Stage owner, UiConfig current, String currentLeftPath, String currentRightPath) {
        UiConfig safeCurrent = current == null ? UiConfig.defaults() : current;

        Dialog<UiConfig> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Preferences");
        dialog.setHeaderText("UI settings");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<String> themeBox = new ComboBox<>(FXCollections.observableArrayList("system", "light", "dark"));
        themeBox.setValue(safeCurrent.theme() == null || safeCurrent.theme().isBlank() ? "system" : safeCurrent.theme());

        CheckBox showHiddenFiles = new CheckBox("Show hidden files");
        showHiddenFiles.setSelected(safeCurrent.showHiddenFiles());

        TextField leftPathField = new TextField(firstNonBlank(safeCurrent.leftStartPath(), currentLeftPath));
        TextField rightPathField = new TextField(firstNonBlank(safeCurrent.rightStartPath(), currentRightPath));

        GridPane content = new GridPane();
        content.setHgap(8);
        content.setVgap(8);
        content.setPadding(new Insets(8));

        content.add(new Label("Theme:"), 0, 0);
        content.add(themeBox, 1, 0);
        content.add(showHiddenFiles, 1, 1);
        content.add(new Label("Left start path:"), 0, 2);
        content.add(leftPathField, 1, 2);
        content.add(new Label("Right start path:"), 0, 3);
        content.add(rightPathField, 1, 3);

        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(button -> {
            if (button != saveButton) {
                return null;
            }
            return new UiConfig(
                    emptyToDefault(themeBox.getValue(), "system"),
                    showHiddenFiles.isSelected(),
                    leftPathField.getText() == null ? "" : leftPathField.getText().trim(),
                    rightPathField.getText() == null ? "" : rightPathField.getText().trim()
            );
        });

        return dialog.showAndWait();
    }

    private static String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return fallback == null ? "" : fallback;
    }

    private static String emptyToDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}

