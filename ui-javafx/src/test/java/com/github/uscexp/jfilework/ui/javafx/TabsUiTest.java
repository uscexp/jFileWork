package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.TabPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("ui")
class TabsUiTest extends MainWindowUiTestBase {

    @Test
    void canOpenAndCloseLeftTabs() {
        TabPane leftTabs = lookup("#leftTabPane").queryAs(TabPane.class);
        assertEquals(1, leftTabs.getTabs().size(), "Expected one left tab at startup");

        clickOn("View");
        clickOn("New Left Tab");
        assertEquals(2, leftTabs.getTabs().size(), "Expected second left tab after menu action");

        clickOn("View");
        clickOn("Close Active Tab");
        assertEquals(1, leftTabs.getTabs().size(), "Expected active left tab to be closed");
    }
}

