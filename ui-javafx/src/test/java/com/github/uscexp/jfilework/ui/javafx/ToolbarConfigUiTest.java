package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class ToolbarConfigUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void useToolbarConfig() throws Exception {
        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "actions": [
                    {
                      "id": "copyBtn",
                      "label": "Copy via Toolbar",
                      "commandId": "file.copy",
                      "params": { "target": "${inactivePane.path}" },
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ]
                }
                """;
        Path configPath = writeConfigFile("jfilework-m3-toolbar", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void toolbarButtonFromConfigExecutesCommand() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        clickOn("#action-copyBtn");

        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        TableView<FileItemViewModel> rightTable = lookup("#rightFileTable").queryTableView();
        boolean containsCopiedFile = rightTable.getItems().stream()
                .anyMatch(item -> "alpha.txt".equals(item.getName()));
        assertTrue(containsCopiedFile, "Expected alpha.txt copied via configured toolbar button");
    }
}

