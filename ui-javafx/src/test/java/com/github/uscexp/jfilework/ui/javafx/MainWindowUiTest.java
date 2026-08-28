package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class MainWindowUiTest extends MainWindowUiTestBase {

    @Test
    void mainWindow_showsBothFileTablesAndDefaultSplit() {
        Set<TableView> tables = lookup(".table-view").queryAllAs(TableView.class);
        assertEquals(2, tables.size(), "Expected exactly two file tables");

        SplitPane splitPane = lookup("#splitPane").queryAs(SplitPane.class);
        double divider = splitPane.getDividerPositions()[0];
        assertTrue(divider > 0.45 && divider < 0.55, "Expected divider around 0.5 but was " + divider);
    }
}

