package com.github.uscexp.jfilework.ui.javafx.action;

import com.github.uscexp.jfilework.ui.javafx.FxTestBase;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class ToolbarBuilderTest extends FxTestBase {

    private static final CommandExecutionHandler NOOP_HANDLER =
            (commandId, params, label) -> { };

    private ToolBar createToolbar(List<ConfiguredAction> actions) {
        ToolBar toolBar = new ToolBar();
        new ToolbarBuilder().build(toolBar, actions, NOOP_HANDLER);
        return toolBar;
    }

    @Test
    void buttonWithConfigIconShowsGraphicAndTooltip() {
        ConfiguredAction action = new ConfiguredAction(
                "copy", "Copy", "file.copy", Map.of(), "", List.of("toolbar.main"), true, "/image/copy.png"
        );

        ToolBar toolBar = createToolbar(List.of(action));
        Button button = (Button) toolBar.getItems().get(0);

        assertEquals("action-copy", button.getId());
        assertEquals("", button.getText(), "Button with icon should show empty text");
        assertTrue(button.getGraphic() != null, "Button should show icon graphic");
        assertEquals("Copy", button.getTooltip().getText());
    }

    @Test
    void buttonWithoutIconShowsTextAndTooltip() {
        ConfiguredAction action = new ConfiguredAction(
                "hash", "Hash Selection", "plugin.hash.sha256", Map.of(), "", List.of("toolbar.main"), true, null
        );

        ToolBar toolBar = createToolbar(List.of(action));
        Button button = (Button) toolBar.getItems().get(0);

        assertEquals("Hash Selection", button.getText());
        assertNull(button.getGraphic());
        assertEquals("Hash Selection", button.getTooltip().getText());
    }

    @Test
    void buttonWithBlankIconShowsText() {
        ConfiguredAction action = new ConfiguredAction(
                "hash", "Hash Selection", "plugin.hash.sha256", Map.of(), "", List.of("toolbar.main"), true, "   "
        );

        ToolBar toolBar = createToolbar(List.of(action));
        Button button = (Button) toolBar.getItems().get(0);

        assertEquals("Hash Selection", button.getText());
        assertNull(button.getGraphic());
    }

    @Test
    void disabledActionSetsButtonDisabled() {
        ConfiguredAction action = new ConfiguredAction(
                "delete", "Delete", "file.delete", Map.of(), "", List.of("toolbar.main"), false, "/image/delete.png"
        );

        ToolBar toolBar = createToolbar(List.of(action));
        Button button = (Button) toolBar.getItems().get(0);

        assertTrue(button.isDisabled());
    }

    @Test
    void onlyToolbarPlacedActionsAreAdded() {
        ConfiguredAction toolbarAction = new ConfiguredAction(
                "copy", "Copy", "file.copy", Map.of(), "", List.of("toolbar.main"), true, "/image/copy.png"
        );
        ConfiguredAction menuAction = new ConfiguredAction(
                "rename", "Rename", "file.rename", Map.of(), "", List.of("menu.file"), true, "/image/rename.png"
        );

        ToolBar toolBar = createToolbar(List.of(toolbarAction, menuAction));

        assertEquals(1, toolBar.getItems().size());
    }
}
