package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.image.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * Resolves icons from Windows executable (.exe) files.
 * Uses {@link FileSystemView} to retrieve the system icon associated with an executable.
 */
public class ExeIconResolver {

    private ExeIconResolver() {
        // utility class
    }

    /**
     * Attempts to load the icon embedded in a Windows executable file.
     *
     * @param exePath path to the .exe file
     * @return a JavaFX {@link Image} or {@code null} if the icon could not be resolved
     */
    public static Image resolve(String exePath) {
        if (exePath == null || exePath.isBlank()) {
            return null;
        }

        File file = new File(exePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            Icon icon = fsv.getSystemIcon(file);
            if (icon == null) {
                return null;
            }

            BufferedImage bufferedImage = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            return new Image(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            return null;
        }
    }
}
