package com.github.uscexp.jfilework.core.fs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Read-only file-system operations needed by the M1 dual-pane browser.
 */
public interface FileService {

    List<FileEntry> listDirectory(Path directory) throws IOException;

    boolean exists(Path path);

    boolean isDirectory(Path path);
}

