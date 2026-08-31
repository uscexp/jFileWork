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
class DeleteCommandUiTest extends MainWindowUiTestBase {

    @Test
    void f8CancelKeepsFileUntouched() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        push(KeyCode.F8);

        clickOn("Cancel");
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);

        TableView<FileItemViewModel> leftTable = lookup("#leftFileTable").queryTableView();
        boolean containsFile = leftTable.getItems().stream()
                .anyMatch(item -> "alpha.txt".equals(item.getName()));

        assertTrue(containsFile, "Expected alpha.txt to remain after canceling delete");
    }
}

