package com.github.uscexp.jfilework.core.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * Creates isolated class loaders for plugin locations.
 */
public class PluginClassLoaderFactory {

    public URLClassLoader create(Path pluginLocation) throws IOException {
        URL url = pluginLocation.toUri().toURL();
        return new URLClassLoader(new URL[]{url}, getClass().getClassLoader());
    }
}

