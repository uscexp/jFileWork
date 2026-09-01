package com.github.uscexp.jfilework.ui.javafx;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class DragDropUiTest extends MainWindowUiTestBase {

    @Test
    void dragAndDropBetweenPanes_copiesFileToTargetDirectory() {
        clickOn("#leftFileTable");
        drag("alpha.txt").dropTo("#rightFileTable");
        WaitForAsyncUtils.sleep(800, TimeUnit.MILLISECONDS);

        assertTrue(Files.exists(rightRoot.resolve("alpha.txt")),
                "Expected alpha.txt to be copied to right pane directory");
    }
}

