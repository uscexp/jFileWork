package com.github.uscexp.jfilework.ui.javafx.model;

import com.github.uscexp.jfilework.core.fs.FileEntry;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileTypeIconResolverTest {

    private final FileTypeIconResolver resolver = new FileTypeIconResolver();

    @Test
    void resolvesParentAndDirectoryToFolderIcon() {
        FileItemViewModel parent = new FileItemViewModel(FileEntry.parent(Path.of("C:/tmp")));
        FileItemViewModel directory = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/folder"), "folder", true, 0L, Instant.now(), false
        ));

        ImageView parentIcon = resolver.resolve(parent);
        ImageView dirIcon = resolver.resolve(directory);

        assertNotNull(parentIcon);
        assertNotNull(parentIcon.getImage());
        assertNotNull(dirIcon);
        assertNotNull(dirIcon.getImage());
    }

    @Test
    void resolvesTextFilesToTextIcon() {
        FileItemViewModel textFile = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/readme.md"), "readme.md", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(textFile);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesCodeFilesToCodeIcon() {
        FileItemViewModel codeFile = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/Main.java"), "Main.java", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(codeFile);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesImageFilesToImageIcon() {
        FileItemViewModel imageFile = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/photo.png"), "photo.png", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(imageFile);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesArchiveFilesToArchiveIcon() {
        FileItemViewModel archive = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/archive.zip"), "archive.zip", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(archive);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesExecutableFilesToExecutableIcon() {
        FileItemViewModel exe = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/app.exe"), "app.exe", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(exe);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesUnknownFilesToFileIcon() {
        FileItemViewModel unknown = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/data.xyz"), "data.xyz", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(unknown);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void resolvesNullToFileIcon() {
        ImageView icon = resolver.resolve(null);

        assertNotNull(icon);
        assertNotNull(icon.getImage());
    }

    @Test
    void iconHasExpectedSize() {
        FileItemViewModel file = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/test.txt"), "test.txt", false, 12L, Instant.now(), false
        ));

        ImageView icon = resolver.resolve(file);

        assertEquals(20, icon.getFitWidth());
        assertEquals(20, icon.getFitHeight());
        assertTrue(icon.isPreserveRatio());
    }

}
