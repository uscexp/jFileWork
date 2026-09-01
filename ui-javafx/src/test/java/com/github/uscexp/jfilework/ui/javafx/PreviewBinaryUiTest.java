package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

@Tag("ui")
class PreviewBinaryUiTest extends MainWindowUiTestBase {

    @Test
    void quickPreviewShowsBinaryHintForBinaryFile() {
        clickOn("#leftFileTable");
        clickOn("binary.bin");
        type(KeyCode.F3);

        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        FxAssert.verifyThat("#previewTextArea",
                TextInputControlMatchers.hasText(org.hamcrest.Matchers.containsString("Binary file preview is not supported.")));
    }
}

