package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class ProcessOutputUiTest extends MainWindowUiTestBase {

    @BeforeAll
    static void useOsExecConfig() throws Exception {
        String javaExe = jsonEscape(javaExecutable().toString());
        String leftAllowed = jsonEscape(leftRoot.toString());
        String rightAllowed = jsonEscape(rightRoot.toString());
        String args = jsonEscape("-cp \"" + System.getProperty("java.class.path") + "\" com.github.uscexp.jfilework.ui.javafx.ProcessOutputTestMain 8000");

        String config = """
                {
                  "ui": { "theme": "system", "showHiddenFiles": false, "leftStartPath": "", "rightStartPath": "" },
                  "security": {
                    "allowOsCommands": true,
                    "allowedExecutables": ["%s"],
                    "allowedRoots": ["%s", "%s"]
                  },
                  "actions": [
                    {
                      "id": "runProcess",
                      "label": "Run Test Process",
                      "commandId": "os.exec",
                      "params": {
                        "exe": "%s",
                        "args": "%s"
                      },
                      "shortcut": "",
                      "placement": ["toolbar.main"]
                    }
                  ]
                }
                """.formatted(javaExe, leftAllowed, rightAllowed, javaExe, args);
        Path configPath = writeConfigFile("jfilework-m4-process-output", config);
        System.setProperty("jfilework.config", configPath.toString());
    }

    @Test
    void processOutputWindowShowsLiveOutputAndKillTerminatesProcess() throws Exception {
        clickOn("#action-runProcess");

        WaitForAsyncUtils.waitFor(8, TimeUnit.SECONDS, () -> {
            try {
                return lookup("#processOutputArea").tryQuery().isPresent();
            } catch (EmptyNodeQueryException exception) {
                return false;
            }
        });
        FxAssert.verifyThat("#processOutputArea", NodeMatchers.isVisible());
        WaitForAsyncUtils.sleep(700, TimeUnit.MILLISECONDS);

        TextArea outputArea = lookup("#processOutputArea").queryAs(TextArea.class);
        assertTrue(outputArea.getText().contains("ui-test-out"), "Expected stdout line in process output window");

        clickOn("#processKillButton");
        WaitForAsyncUtils.sleep(700, TimeUnit.MILLISECONDS);

        Label status = lookup("#statusLabel").queryAs(Label.class);
        assertTrue(status.getText().toLowerCase().contains("cancel"), "Expected cancel status after killing process");

        clickOn("#processCloseButton");
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable);
    }
}


