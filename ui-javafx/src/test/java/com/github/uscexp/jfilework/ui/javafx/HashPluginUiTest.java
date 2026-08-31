package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.sample.hash.HashPlugin;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

@Tag("ui")
class HashPluginUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void usePluginActionConfig() throws Exception {
        Path pluginSource = Path.of(HashPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path pluginRoot = materializePluginDirectory(pluginSource, "sample-hash-plugin");

        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "security": { "allowOsCommands": false, "allowedExecutables": [], "allowedRoots": [] },
                  "plugins": {
                    "directory": "%s",
                    "enabled": ["sample-hash-plugin"]
                  },
                  "actions": [
                    {
                      "id": "hashSelection",
                      "label": "Hash Selection",
                      "commandId": "plugin.hash.sha256",
                      "params": {},
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ]
                }
                """.formatted(jsonEscape(pluginRoot.toString()));
        Path configPath = writeConfigFile("jfilework-m5-hash-plugin", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void configuredPluginCommandAppearsAndShowsResultDialog() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        clickOn("#action-hashSelection");

        WaitForAsyncUtils.sleep(800, TimeUnit.MILLISECONDS);
        FxAssert.verifyThat("#commandResultArea", NodeMatchers.isVisible());

        TextArea resultArea = lookup("#commandResultArea").queryAs(TextArea.class);
        assertTrue(resultArea.getText().contains("SHA-256"));
        assertTrue(resultArea.getText().contains("alpha.txt"));
        clickOn("#commandResultCloseButton");
    }
}





