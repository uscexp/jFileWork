package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * Registers and dispatches configured key bindings.
 */
public class KeyBindingService {

    public void bind(Scene scene, List<ConfiguredAction> actions, CommandExecutionHandler handler) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            for (ConfiguredAction action : actions) {
                if (!action.enabled() || action.shortcut() == null || action.shortcut().isBlank()) {
                    continue;
                }
                KeyCombination keyCombination;
                try {
                    keyCombination = KeyCombination.keyCombination(action.shortcut());
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                if (keyCombination.match(event)) {
                    handler.execute(action.commandId(), action.params(), action.label());
                    event.consume();
                    break;
                }
            }
        });
    }
}

