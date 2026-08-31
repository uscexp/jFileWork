package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.NodeMatchers;

@Tag("ui")
class ProgressDialogUiTest extends MainWindowUiTestBase {

    @AfterEach
    void clearDelayProperty() {
        System.clearProperty("jfilework.command.artificialDelayMs");
    }

    @Test
    void progressDialogShowsCancelButtonDuringCommand() {
        System.setProperty("jfilework.command.artificialDelayMs", "1200");

        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        push(KeyCode.F5);

        FxAssert.verifyThat("#progressCancelButton", NodeMatchers.isVisible());
    }
}

