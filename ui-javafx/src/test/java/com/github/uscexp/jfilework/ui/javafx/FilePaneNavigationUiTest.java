package com.github.uscexp.jfilework.ui.javafx;

import javafx.scene.control.TextField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class FilePaneNavigationUiTest extends MainWindowUiTestBase {

    @Test
    void doubleClickingDirectory_navigatesToSubDirectory() {
        clickOn("#leftFileTable");
        doubleClickOn("sub");

        TextField pathField = lookup("#leftPathField").queryAs(TextField.class);
        String path = pathField.getText().replace('\\', '/');
        assertTrue(path.endsWith("/sub"), "Expected path to end with /sub but was: " + path);
    }
}

