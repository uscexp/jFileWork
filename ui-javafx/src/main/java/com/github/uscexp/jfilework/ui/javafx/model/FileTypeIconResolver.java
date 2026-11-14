package com.github.uscexp.jfilework.ui.javafx.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Locale;

/**
 * Resolves file type icons for file rows.
 */
public class FileTypeIconResolver {

    private static final int ICON_SIZE = 20;

    public ImageView resolve(FileItemViewModel item) {
        if (item == null) {
            return createImageView("/image/file.png");
        }
        if (item.isParentLink()) {
            return createImageView("/image/folder.png");
        }
        if (item.isDirectory()) {
            return createImageView("/image/folder.png");
        }

        String name = item.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return createImageView("/image/file.png");
        }

        String extension = name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "txt", "md", "log", "json", "xml", "yaml", "yml", "csv" -> createImageView("/image/text.png");
            case "java", "kt", "groovy", "js", "ts", "py", "c", "cpp", "h" -> createImageView("/image/code.png");
            case "png", "jpg", "jpeg", "gif", "bmp", "svg" -> createImageView("/image/image.png");
            case "zip", "jar", "7z", "rar", "tar", "gz" -> createImageView("/image/archive.png");
            case "exe", "bat", "cmd", "ps1", "sh" -> createImageView("/image/executable.png");
            default -> createImageView("/image/file.png");
        };
    }

    private ImageView createImageView(String path) {
        Image image = new Image(getClass().getResourceAsStream(path));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(ICON_SIZE);
        imageView.setFitHeight(ICON_SIZE);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}
