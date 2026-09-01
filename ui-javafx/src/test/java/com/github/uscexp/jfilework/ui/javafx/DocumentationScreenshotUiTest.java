package com.github.uscexp.jfilework.ui.javafx;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates a deterministic screenshot of the main UI for project documentation.
 */
@Tag("ui")
class DocumentationScreenshotUiTest extends MainWindowUiTestBase {

    private static final long UI_READY_TIMEOUT_SECONDS = 10L;
    private static final long UI_SETTLE_DELAY_MILLIS = 500L;
    private static final String UI_MODULE_DIR = "ui-javafx";

    private javafx.stage.Stage primaryStage;

    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        super.start(stage);
        this.primaryStage = stage;
    }

    /**
     * Captures and stores the current main-window view as PNG for docs.
     */
    @Test
    void createCurrentUiScreenshotForDocumentation() throws Exception {
        WaitForAsyncUtils.waitFor(UI_READY_TIMEOUT_SECONDS, TimeUnit.SECONDS,
                () -> primaryStage != null && primaryStage.getScene() != null);
        WaitForAsyncUtils.sleep(UI_SETTLE_DELAY_MILLIS, TimeUnit.MILLISECONDS);

        WritableImage snapshot = snapshotRoot();
        Path screenshotPath = projectRoot().resolve("docs").resolve("screenshots").resolve("m6-main-window.png");
        Files.createDirectories(screenshotPath.getParent());
        writePng(snapshot, screenshotPath);

        assertTrue(Files.exists(screenshotPath));
        assertTrue(Files.size(screenshotPath) > 0);
    }

    /**
     * Takes a JavaFX snapshot of the current scene root on the FX thread.
     */
    private WritableImage snapshotRoot() throws Exception {
        AtomicReference<WritableImage> imageRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                imageRef.set(primaryStage.getScene().getRoot().snapshot(new SnapshotParameters(), null));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(UI_READY_TIMEOUT_SECONDS, TimeUnit.SECONDS), "Timed out waiting for UI snapshot");
        return imageRef.get();
    }

    /**
     * Writes a JavaFX image as a PNG file.
     */
    private static void writePng(WritableImage image, Path output) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(bufferedImage, "png", output.toFile());
    }

    /**
     * Resolves the project root whether the test runs from root or ui-javafx module directory.
     */
    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        if (current.getFileName() != null && UI_MODULE_DIR.equals(current.getFileName().toString())) {
            return current.getParent();
        }
        return current;
    }
}

