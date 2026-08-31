package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class HotkeyBindingUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void useHotkeyConfig() throws Exception {
        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "actions": [
                    {
                      "id": "copyByF9",
                      "label": "Copy with F9",
                      "commandId": "file.copy",
                      "params": { "target": "${inactivePane.path}" },
                      "shortcut": "F9",
                      "placement": ["menu.tools"]
                    }
                  ]
                }
                """;
        Path configPath = writeConfigFile("jfilework-m3-hotkey", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void configuredHotkeyTriggersCommandWithoutMouse() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        push(KeyCode.F9);

        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        TableView<FileItemViewModel> rightTable = lookup("#rightFileTable").queryTableView();
        boolean containsCopiedFile = rightTable.getItems().stream()
                .anyMatch(item -> "alpha.txt".equals(item.getName()));
        assertTrue(containsCopiedFile, "Expected alpha.txt copied via configured F9 hotkey");
    }
}

