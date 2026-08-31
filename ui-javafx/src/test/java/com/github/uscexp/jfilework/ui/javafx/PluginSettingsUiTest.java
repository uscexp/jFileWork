package com.github.uscexp.jfilework.ui.javafx;

import com.github.uscexp.jfilework.sample.hash.HashPlugin;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class PluginSettingsUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void usePluginConfig() throws Exception {
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
                  "actions": []
                }
                """.formatted(jsonEscape(pluginRoot.toString()));
        Path configPath = writeConfigFile("jfilework-m5-plugin-settings", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void pluginSettingsShowLoadedDemoPlugin() {
        clickOn("View");
        clickOn("Plugin Settings…");

        TextArea area = lookup("#pluginListArea").queryAs(TextArea.class);
        assertTrue(area.getText().contains("Sample Hash Plugin"));
        assertTrue(area.getText().contains("LOADED"));

        clickOn("#pluginSettingsCloseButton");
    }
}

