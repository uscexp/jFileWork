package com.github.uscexp.jfilework.ui.javafx.model;

import com.github.uscexp.jfilework.core.fs.FileEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileTypeIconResolverTest {

    private final FileTypeIconResolver resolver = new FileTypeIconResolver();

    @Test
    void resolvesParentAndDirectoryAndTypedFiles() {
        FileItemViewModel parent = new FileItemViewModel(FileEntry.parent(Path.of("C:/tmp")));
        FileItemViewModel directory = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/folder"), "folder", true, 0L, Instant.now(), false
        ));
        FileItemViewModel textFile = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/readme.md"), "readme.md", false, 12L, Instant.now(), false
        ));
        FileItemViewModel archive = new FileItemViewModel(new FileEntry(
                Path.of("C:/tmp/archive.zip"), "archive.zip", false, 12L, Instant.now(), false
        ));

        assertEquals("[..]", resolver.resolve(parent));
        assertEquals("[D]", resolver.resolve(directory));
        assertEquals("[T]", resolver.resolve(textFile));
        assertEquals("[A]", resolver.resolve(archive));
    }
}

