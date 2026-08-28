package com.github.uscexp.jfilework.ui.javafx.model;

import com.github.uscexp.jfilework.core.fs.FileEntry;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.nio.file.Path;
import java.time.Instant;

/**
 * JavaFX projection for a file entry row.
 */
public class FileItemViewModel {

    private final ReadOnlyStringWrapper name;
    private final ReadOnlyLongWrapper size;
    private final ReadOnlyObjectWrapper<Instant> lastModified;
    private final ReadOnlyStringWrapper type;
    private final ReadOnlyObjectWrapper<Path> path;
    private final ReadOnlyBooleanWrapper directory;
    private final ReadOnlyBooleanWrapper parentLink;

    public FileItemViewModel(FileEntry entry) {
        this.name = new ReadOnlyStringWrapper(entry.name());
        this.size = new ReadOnlyLongWrapper(entry.size());
        this.lastModified = new ReadOnlyObjectWrapper<>(entry.lastModified());
        this.type = new ReadOnlyStringWrapper(entry.parentLink() ? "Parent" : (entry.directory() ? "Directory" : "File"));
        this.path = new ReadOnlyObjectWrapper<>(entry.path());
        this.directory = new ReadOnlyBooleanWrapper(entry.directory());
        this.parentLink = new ReadOnlyBooleanWrapper(entry.parentLink());
    }

    public String getName() { return name.get(); }
    public long getSize() { return size.get(); }
    public Instant getLastModified() { return lastModified.get(); }
    public String getType() { return type.get(); }
    public Path getPath() { return path.get(); }
    public boolean isDirectory() { return directory.get(); }
    public boolean isParentLink() { return parentLink.get(); }
}

