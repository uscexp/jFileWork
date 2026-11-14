package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExeIconResolverTest {

    @Test
    void resolveNullPathReturnsNull() {
        assertNull(ExeIconResolver.resolve(null));
    }

    @Test
    void resolveBlankPathReturnsNull() {
        assertNull(ExeIconResolver.resolve(""));
        assertNull(ExeIconResolver.resolve("   "));
    }

    @Test
    void resolveNonExistentFileReturnsNull() {
        assertNull(ExeIconResolver.resolve("/nonexistent/path/to/app.exe"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void resolveExistingExeReturnsImage() {
        Path systemExe = Path.of("C:/Windows/System32/notepad.exe");
        if (Files.exists(systemExe)) {
            Image icon = ExeIconResolver.resolve(systemExe.toString());
            assertNotNull(icon, "notepad.exe icon should be resolvable on Windows");
        }
    }
}
