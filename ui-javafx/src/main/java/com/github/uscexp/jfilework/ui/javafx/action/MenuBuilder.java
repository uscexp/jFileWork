package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;

/**
 * Builds menu entries from configured actions.
 */
public class MenuBuilder {

    public void build(Menu fileMenu, Menu toolsMenu, List<ConfiguredAction> actions, CommandExecutionHandler handler) {
        removeDynamicItems(fileMenu);
        removeDynamicItems(toolsMenu);

        addItems(fileMenu, actions, "menu.file", handler);
        addItems(toolsMenu, actions, "menu.tools", handler);
    }

    private void addItems(Menu menu, List<ConfiguredAction> actions, String placement, CommandExecutionHandler handler) {
        boolean hasStaticItems = !menu.getItems().isEmpty();
        int inserted = 0;

        for (ConfiguredAction action : actions) {
            if (!action.placement().contains(placement)) {
                continue;
            }

            MenuItem item = new MenuItem(action.label());
            item.setId("action-" + action.id());
            item.getProperties().put("dynamicAction", Boolean.TRUE);
            item.setDisable(!action.enabled());
            item.setOnAction(event -> handler.execute(action.commandId(), action.params(), action.label()));
            menu.getItems().add(0, item);
            inserted++;
        }

        if (hasStaticItems && inserted > 0 && !menu.getItems().isEmpty()) {
            boolean alreadySeparated = menu.getItems().stream().findFirst().orElse(null) instanceof SeparatorMenuItem;
            if (!alreadySeparated) {
                SeparatorMenuItem separator = new SeparatorMenuItem();
                separator.getProperties().put("dynamicAction", Boolean.TRUE);
                menu.getItems().add(inserted, separator);
            }
        }
    }

    private static void removeDynamicItems(Menu menu) {
        menu.getItems().removeIf(item -> Boolean.TRUE.equals(item.getProperties().get("dynamicAction")));
    }
}

