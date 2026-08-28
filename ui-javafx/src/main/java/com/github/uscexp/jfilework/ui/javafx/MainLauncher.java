package com.github.uscexp.jfilework.ui.javafx;

/**
 * Application launcher — separate from {@link MainApp} so the JAR manifest
 * does not require a JavaFX-aware class loader as main class.
 */
public class MainLauncher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}

