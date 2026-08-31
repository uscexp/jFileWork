package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class CopyCommandUiTest extends MainWindowUiTestBase {

    @Test
    void f5CopiesSelectionToOtherPane() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        push(KeyCode.F5);

        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);

        TableView<FileItemViewModel> rightTable = lookup("#rightFileTable").queryTableView();
        boolean containsCopiedFile = rightTable.getItems().stream()
                .anyMatch(item -> "alpha.txt".equals(item.getName()));

        assertTrue(containsCopiedFile, "Expected copied file alpha.txt in right pane");
    }
}

