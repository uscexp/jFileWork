package com.github.uscexp.jfilework.ui.javafx.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionServiceTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearProperty() {
        System.clearProperty(SessionService.SESSION_PATH_PROPERTY);
    }

    @Test
    void saveAndLoadRoundTrip() {
        Path sessionFile = tempDir.resolve("session.properties");
        System.setProperty(SessionService.SESSION_PATH_PROPERTY, sessionFile.toString());

        SessionService sessionService = new SessionService();
        SessionState expected = new SessionState(10, 20, 1000, 700, "C:/left", "D:/right");
        sessionService.save(expected);

        Optional<SessionState> loaded = sessionService.load();
        assertTrue(loaded.isPresent());
        assertEquals(expected.leftPath(), loaded.get().leftPath());
        assertEquals(expected.rightPath(), loaded.get().rightPath());
        assertEquals(expected.windowWidth(), loaded.get().windowWidth());
        assertEquals(expected.windowHeight(), loaded.get().windowHeight());
    }
}

