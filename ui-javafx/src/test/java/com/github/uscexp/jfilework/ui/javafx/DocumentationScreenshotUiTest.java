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

@Tag("ui")
class DocumentationScreenshotUiTest extends MainWindowUiTestBase {

    private javafx.stage.Stage stage;

    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        super.start(stage);
        this.stage = stage;
    }

    @Test
    void createCurrentUiScreenshotForDocumentation() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> stage != null && stage.getScene() != null);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        WritableImage snapshot = snapshotRoot();
        Path screenshotPath = projectRoot().resolve("docs").resolve("screenshots").resolve("m6-main-window.png");
        Files.createDirectories(screenshotPath.getParent());
        writePng(snapshot, screenshotPath);

        assertTrue(Files.exists(screenshotPath));
        assertTrue(Files.size(screenshotPath) > 0);
    }

    private WritableImage snapshotRoot() throws Exception {
        AtomicReference<WritableImage> imageRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                imageRef.set(stage.getScene().getRoot().snapshot(new SnapshotParameters(), null));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for UI snapshot");
        return imageRef.get();
    }

    private static void writePng(WritableImage image, Path output) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(bufferedImage, "png", output.toFile());
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        if (current.getFileName() != null && "ui-javafx".equals(current.getFileName().toString())) {
            return current.getParent();
        }
        return current;
    }
}

