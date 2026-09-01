package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

@Tag("ui")
class PreviewUiTest extends MainWindowUiTestBase {

    @Test
    void quickPreviewOpensTextPreviewForSelectedFile() {
        clickOn("#leftFileTable");
        clickOn("alpha.txt");
        type(KeyCode.F3);

        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        FxAssert.verifyThat("#previewTextArea", NodeMatchers.isVisible());
        FxAssert.verifyThat("#previewTextArea", TextInputControlMatchers.hasText(org.hamcrest.Matchers.containsString("alpha")));

        TextArea preview = lookup("#previewTextArea").queryAs(TextArea.class);
        clickOn(preview);
        type(KeyCode.ESCAPE);
    }
}

