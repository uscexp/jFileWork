package com.github.uscexp.jfilework.ui.javafx.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Tracks which pane is currently active (focused by the user).
 */
public class ActivePaneTracker {

    public enum PaneSide { LEFT, RIGHT }

    private final ObjectProperty<PaneSide> activePane = new SimpleObjectProperty<>(PaneSide.LEFT);

    public PaneSide getActivePane() {
        return activePane.get();
    }

    public ObjectProperty<PaneSide> activePaneProperty() {
        return activePane;
    }

    public void setActivePane(PaneSide paneSide) {
        activePane.set(paneSide);
    }
}

