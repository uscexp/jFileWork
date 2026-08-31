package com.github.uscexp.jfilework.ui.javafx.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Persists and loads basic UI session state.
 */
public class SessionService {

    public static final String SESSION_PATH_PROPERTY = "jfilework.session";
    public static final String DEFAULT_SESSION_PATH = "config/session.properties";

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    public Optional<SessionState> load() {
        Path sessionPath = resolveSessionPath();
        if (!Files.exists(sessionPath)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(sessionPath)) {
            properties.load(inputStream);
            SessionState state = new SessionState(
                    parseDouble(properties.getProperty("window.x"), 80.0),
                    parseDouble(properties.getProperty("window.y"), 80.0),
                    parseDouble(properties.getProperty("window.width"), 1200.0),
                    parseDouble(properties.getProperty("window.height"), 700.0),
                    properties.getProperty("pane.left.path", ""),
                    properties.getProperty("pane.right.path", "")
            );
            return Optional.of(state);
        } catch (IOException exception) {
            log.warn("Cannot read session file {}", sessionPath.toAbsolutePath(), exception);
            return Optional.empty();
        }
    }

    public void save(SessionState state) {
        Properties properties = new Properties();
        properties.setProperty("window.x", String.valueOf(state.windowX()));
        properties.setProperty("window.y", String.valueOf(state.windowY()));
        properties.setProperty("window.width", String.valueOf(state.windowWidth()));
        properties.setProperty("window.height", String.valueOf(state.windowHeight()));
        properties.setProperty("pane.left.path", nullToEmpty(state.leftPath()));
        properties.setProperty("pane.right.path", nullToEmpty(state.rightPath()));

        Path sessionPath = resolveSessionPath();
        try {
            Path parent = sessionPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(sessionPath)) {
                properties.store(outputStream, "jFileWork session");
            }
        } catch (IOException exception) {
            log.warn("Cannot save session file {}", sessionPath.toAbsolutePath(), exception);
        }
    }

    public Path resolveSessionPath() {
        String pathFromProperty = System.getProperty(SESSION_PATH_PROPERTY);
        if (pathFromProperty != null && !pathFromProperty.isBlank()) {
            return Path.of(pathFromProperty.trim());
        }
        return Path.of(DEFAULT_SESSION_PATH);
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

