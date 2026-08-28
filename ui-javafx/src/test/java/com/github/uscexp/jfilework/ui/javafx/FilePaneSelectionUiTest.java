package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class FilePaneSelectionUiTest extends MainWindowUiTestBase {

    @Test
    void leftPane_allowsMultiSelection() {
        TableView<FileItemViewModel> table = lookup("#leftFileTable").queryTableView();

        interact(() -> {
            int rowCount = table.getItems().size();
            int firstIndex = rowCount > 1 ? 1 : 0;
            int secondIndex = rowCount > 2 ? 2 : firstIndex;
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().selectIndices(firstIndex, secondIndex);
        });

        assertTrue(table.getSelectionModel().getSelectedItems().size() >= 2,
                "Expected multiple selected rows");
    }
}

