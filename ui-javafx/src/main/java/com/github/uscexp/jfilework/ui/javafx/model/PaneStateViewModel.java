package com.github.uscexp.jfilework.ui.javafx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;

/**
 * Mutable state for one pane (left or right).
 */
public class PaneStateViewModel {

    private final ObjectProperty<Path> currentPath = new SimpleObjectProperty<>();
    private final ObservableList<Path> selectedPaths = FXCollections.observableArrayList();

    public ObjectProperty<Path> currentPathProperty() {
        return currentPath;
    }

    public Path getCurrentPath() {
        return currentPath.get();
    }

    public void setCurrentPath(Path path) {
        currentPath.set(path);
    }

    public ObservableList<Path> getSelectedPaths() {
        return selectedPaths;
    }
}

