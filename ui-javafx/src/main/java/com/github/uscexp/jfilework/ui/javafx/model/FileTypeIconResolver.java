package com.github.uscexp.jfilework.ui.javafx.model;

import java.util.Locale;

/**
 * Resolves lightweight ASCII icons for file rows.
 */
public class FileTypeIconResolver {

    public String resolve(FileItemViewModel item) {
        if (item == null) {
            return "[?]";
        }
        if (item.isParentLink()) {
            return "[..]";
        }
        if (item.isDirectory()) {
            return "[D]";
        }

        String name = item.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "[F]";
        }

        String extension = name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "txt", "md", "log", "json", "xml", "yaml", "yml", "csv" -> "[T]";
            case "java", "kt", "groovy", "js", "ts", "py", "c", "cpp", "h" -> "[C]";
            case "png", "jpg", "jpeg", "gif", "bmp", "svg" -> "[I]";
            case "zip", "jar", "7z", "rar", "tar", "gz" -> "[A]";
            case "exe", "bat", "cmd", "ps1", "sh" -> "[X]";
            default -> "[F]";
        };
    }
}

