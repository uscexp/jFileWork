package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.plugin.PluginRuntimeInfo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Read-only dialog showing discovered plugin states.
 */
public class PluginSettingsController {

    public void showDialog(Stage owner, List<PluginRuntimeInfo> plugins) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Plugin Settings");

        TextArea pluginListArea = new TextArea(formatPlugins(plugins));
        pluginListArea.setId("pluginListArea");
        pluginListArea.setEditable(false);
        pluginListArea.setWrapText(true);

        Button closeButton = new Button("Close");
        closeButton.setId("pluginSettingsCloseButton");
        closeButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(8, pluginListArea, closeButton);
        root.setPadding(new Insets(10));
        VBox.setVgrow(pluginListArea, Priority.ALWAYS);

        dialog.setScene(new Scene(root, 540, 320));
        dialog.showAndWait();
    }

    private static String formatPlugins(List<PluginRuntimeInfo> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            return "No plugins discovered.";
        }

        StringBuilder builder = new StringBuilder();
        for (PluginRuntimeInfo plugin : plugins) {
            if (!builder.isEmpty()) {
                builder.append(System.lineSeparator()).append(System.lineSeparator());
            }
            builder.append(plugin.name())
                    .append(" [").append(plugin.id()).append("]")
                    .append(System.lineSeparator())
                    .append("Version: ").append(plugin.version())
                    .append(System.lineSeparator())
                    .append("Status: ").append(plugin.status())
                    .append(System.lineSeparator())
                    .append("Message: ").append(plugin.message());
        }
        return builder.toString();
    }
}

