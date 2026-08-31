package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class DisabledActionUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void useInvalidCommandConfig() throws Exception {
        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "actions": [
                    {
                      "id": "brokenAction",
                      "label": "Broken Action",
                      "commandId": "missing.command",
                      "params": {},
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ]
                }
                """;
        Path configPath = writeConfigFile("jfilework-m3-disabled", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void unknownCommandActionIsDisabledInToolbar() {
        Button button = lookup("#action-brokenAction").queryAs(Button.class);
        assertTrue(button.isDisable(), "Expected toolbar action to be disabled for unknown command");
    }
}

