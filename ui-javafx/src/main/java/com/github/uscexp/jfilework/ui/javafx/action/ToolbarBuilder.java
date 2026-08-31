package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

import java.util.List;

/**
 * Builds toolbar buttons from configured actions.
 */
public class ToolbarBuilder {

    public void build(ToolBar toolBar, List<ConfiguredAction> actions, CommandExecutionHandler handler) {
        toolBar.getItems().clear();

        actions.stream()
                .filter(action -> action.placement().contains("toolbar.main"))
                .forEach(action -> {
                    Button button = new Button(action.label());
                    button.setId("action-" + action.id());
                    button.setDisable(!action.enabled());
                    button.setOnAction(event -> handler.execute(action.commandId(), action.params(), action.label()));
                    toolBar.getItems().add(button);
                });
    }
}

