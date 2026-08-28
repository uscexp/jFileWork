package com.github.uscexp.jfilework.core.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileServiceTest {

    private final LocalFileService fileService = new LocalFileService();

    @Test
    void listDirectory_sortsDirectoriesBeforeFiles(@TempDir Path tempDir) throws IOException {
        Files.createDirectory(tempDir.resolve("z-dir"));
        Files.createDirectory(tempDir.resolve("a-dir"));
        Files.writeString(tempDir.resolve("b-file.txt"), "hello");
        Files.writeString(tempDir.resolve("a-file.txt"), "hello");

        List<FileEntry> entries = fileService.listDirectory(tempDir);

        assertEquals(4, entries.size());
        assertTrue(entries.get(0).directory());
        assertEquals("a-dir", entries.get(0).name());
        assertTrue(entries.get(1).directory());
        assertEquals("z-dir", entries.get(1).name());
        assertEquals("a-file.txt", entries.get(2).name());
        assertEquals("b-file.txt", entries.get(3).name());
    }

    @Test
    void listDirectory_setsSizeForRegularFiles(@TempDir Path tempDir) throws IOException {
        Path sample = tempDir.resolve("sample.txt");
        Files.writeString(sample, "12345");

        List<FileEntry> entries = fileService.listDirectory(tempDir);

        assertEquals(1, entries.size());
        FileEntry entry = entries.get(0);
        assertEquals("sample.txt", entry.name());
        assertEquals(5L, entry.size());
        assertTrue(!entry.directory());
    }
}

