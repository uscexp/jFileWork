package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class FilterUiTest extends MainWindowUiTestBase {

    @Test
    void filterField_limitsVisibleRowsByName() {
        TextField filter = lookup("#leftFilterField").queryAs(TextField.class);
        clickOn(filter);
        write("alpha");

        TableView<FileItemViewModel> table = lookup("#leftFileTable").queryTableView();
        List<String> visibleNames = table.getItems().stream()
                .filter(item -> !item.isParentLink())
                .map(FileItemViewModel::getName)
                .toList();
        assertTrue(visibleNames.contains("alpha.txt"), "Expected alpha.txt to remain visible");
        assertFalse(visibleNames.contains("beta.txt"), "Expected beta.txt to be filtered out");
    }
}


