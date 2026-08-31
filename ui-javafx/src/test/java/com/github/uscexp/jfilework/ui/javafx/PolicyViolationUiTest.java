package com.github.uscexp.jfilework.ui.javafx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.NodeMatchers;

import java.nio.file.Path;

@Tag("ui")
class PolicyViolationUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void useBlockedOsExecConfig() throws Exception {
        String javaExe = jsonEscape(javaExecutable().toString());
        String leftAllowed = jsonEscape(leftRoot.toString());
        String rightAllowed = jsonEscape(rightRoot.toString());

        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "security": {
                    "allowOsCommands": false,
                    "allowedExecutables": ["%s"],
                    "allowedRoots": ["%s", "%s"]
                  },
                  "actions": [
                    {
                      "id": "blockedProcess",
                      "label": "Blocked Process",
                      "commandId": "os.exec",
                      "params": {
                        "exe": "%s",
                        "args": "-version"
                      },
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ]
                }
                """.formatted(javaExe, leftAllowed, rightAllowed, javaExe);
        Path configPath = writeConfigFile("jfilework-m4-policy-blocked", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void policyViolationShowsErrorDialog() {
        clickOn("#action-blockedProcess");
        FxAssert.verifyThat("OS commands are disabled by configuration.", NodeMatchers.isVisible());
        clickOn("OK");
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable);
    }
}

