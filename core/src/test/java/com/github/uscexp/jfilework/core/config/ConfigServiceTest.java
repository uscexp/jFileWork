package com.github.uscexp.jfilework.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void iconIsParsedFromConfig() throws Exception {
        Path configFile = tempDir.resolve("app-config.json");
        String json = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "actions": [
                    {
                      "id": "copyToOtherPane",
                      "label": "Copy",
                      "commandId": "file.copy",
                      "params": { "target": "${inactivePane.path}" },
                      "shortcut": "F5",
                      "placement": ["toolbar.main", "menu.file"],
                      "icon": "/image/copy.png"
                    },
                    {
                      "id": "hashSelection",
                      "label": "Hash Selection",
                      "commandId": "plugin.hash.sha256",
                      "params": {},
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ],
                  "security": { "allowOsCommands": false, "allowedExecutables": [], "allowedRoots": [] },
                  "plugins": { "directory": "", "enabled": [] }
                }
                """;
        java.nio.file.Files.writeString(configFile, json);

        System.setProperty("jfilework.config", configFile.toString());
        try {
            ConfigService service = new ConfigService();
            AppConfig config = service.load();

            assertEquals(2, config.actions().size());
            assertEquals("/image/copy.png", config.actions().get(0).icon());
            assertNull(config.actions().get(1).icon());
        } finally {
            System.clearProperty("jfilework.config");
        }
    }
}
