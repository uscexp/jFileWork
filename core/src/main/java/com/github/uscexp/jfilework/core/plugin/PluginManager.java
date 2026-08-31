package com.github.uscexp.jfilework.core.plugin;

import com.github.uscexp.jfilework.api.plugin.CommandPlugin;
import com.github.uscexp.jfilework.api.plugin.PluginContext;
import com.github.uscexp.jfilework.api.plugin.PluginDescriptor;
import com.github.uscexp.jfilework.api.plugin.PluginCommand;
import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.config.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Discovers and loads command plugins from a configured directory.
 */
public class PluginManager implements AutoCloseable {

    public static final String SUPPORTED_PLUGIN_API_VERSION = "1.0";

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private final PluginClassLoaderFactory classLoaderFactory;
    private final CommandRegistry commandRegistry;
    private final List<PluginRuntimeInfo> runtimeInfos = new ArrayList<>();
    private final List<LoadedPlugin> loadedPlugins = new ArrayList<>();

    public PluginManager(CommandRegistry commandRegistry) {
        this(commandRegistry, new PluginClassLoaderFactory());
    }

    public PluginManager(CommandRegistry commandRegistry, PluginClassLoaderFactory classLoaderFactory) {
        this.commandRegistry = commandRegistry;
        this.classLoaderFactory = classLoaderFactory;
    }

    public List<PluginRuntimeInfo> loadPlugins(PluginConfig pluginConfig, String applicationVersion) {
        runtimeInfos.clear();
        closeLoadedPlugins();

        PluginConfig effectiveConfig = pluginConfig == null ? PluginConfig.defaults() : pluginConfig;
        Path directory = Path.of(effectiveConfig.directory()).toAbsolutePath().normalize();
        if (!Files.exists(directory)) {
            return List.of();
        }

        try (var locations = Files.list(directory)) {
            locations
                    .filter(path -> Files.isDirectory(path) || path.getFileName().toString().endsWith(".jar"))
                    .sorted()
                    .forEach(location -> loadPluginLocation(location, directory, effectiveConfig, applicationVersion));
        } catch (IOException exception) {
            runtimeInfos.add(new PluginRuntimeInfo("plugin-scan", "Plugin Scan", "n/a", PluginStatus.FAILED, exception.getMessage()));
            log.warn("Cannot scan plugins directory {}", directory, exception);
        }

        return List.copyOf(runtimeInfos);
    }

    public List<PluginRuntimeInfo> getRuntimeInfos() {
        return List.copyOf(runtimeInfos);
    }

    @Override
    public void close() {
        closeLoadedPlugins();
    }

    private void loadPluginLocation(Path location, Path pluginsDirectory, PluginConfig pluginConfig, String applicationVersion) {
        try {
            URLClassLoader classLoader = classLoaderFactory.create(location);
            ServiceLoader<CommandPlugin> serviceLoader = ServiceLoader.load(CommandPlugin.class, classLoader);

            boolean anyProvider = false;
            for (CommandPlugin plugin : serviceLoader) {
                anyProvider = true;
                registerPlugin(plugin, classLoader, pluginsDirectory, pluginConfig, applicationVersion);
            }

            if (!anyProvider) {
                classLoader.close();
            }
        } catch (Exception exception) {
            runtimeInfos.add(new PluginRuntimeInfo(location.getFileName().toString(), location.getFileName().toString(), "n/a", PluginStatus.FAILED, exception.getMessage()));
            log.warn("Cannot load plugin from {}", location, exception);
        }
    }

    private void registerPlugin(CommandPlugin plugin,
                                URLClassLoader classLoader,
                                Path pluginsDirectory,
                                PluginConfig pluginConfig,
                                String applicationVersion) {
        PluginDescriptor descriptor = plugin.descriptor();
        if (descriptor == null) {
            runtimeInfos.add(new PluginRuntimeInfo("unknown", "unknown", "n/a", PluginStatus.FAILED, "Plugin descriptor is missing."));
            closeQuietly(classLoader);
            return;
        }

        if (!SUPPORTED_PLUGIN_API_VERSION.equals(descriptor.pluginApiVersion())) {
            runtimeInfos.add(new PluginRuntimeInfo(descriptor.id(), descriptor.name(), descriptor.version(), PluginStatus.INCOMPATIBLE,
                    "Expected plugin API " + SUPPORTED_PLUGIN_API_VERSION + " but found " + descriptor.pluginApiVersion()));
            closeQuietly(classLoader);
            return;
        }

        if (!isEnabled(pluginConfig, descriptor.id())) {
            runtimeInfos.add(new PluginRuntimeInfo(descriptor.id(), descriptor.name(), descriptor.version(), PluginStatus.DISABLED,
                    "Plugin is not enabled in configuration."));
            closeQuietly(classLoader);
            return;
        }

        try {
            plugin.initialize(new PluginContext(applicationVersion, SUPPORTED_PLUGIN_API_VERSION, pluginsDirectory, log));
            List<PluginCommand> commands = plugin.commands() == null ? List.of() : plugin.commands();
            for (PluginCommand command : commands) {
                commandRegistry.register(new PluginCommandAdapter(command));
            }
            loadedPlugins.add(new LoadedPlugin(plugin, classLoader));
            runtimeInfos.add(new PluginRuntimeInfo(descriptor.id(), descriptor.name(), descriptor.version(), PluginStatus.LOADED,
                    "Loaded " + commands.size() + " command(s)."));
        } catch (Exception exception) {
            runtimeInfos.add(new PluginRuntimeInfo(descriptor.id(), descriptor.name(), descriptor.version(), PluginStatus.FAILED, exception.getMessage()));
            closeQuietly(classLoader);
            log.warn("Cannot initialize plugin {}", descriptor.id(), exception);
        }
    }

    private static boolean isEnabled(PluginConfig pluginConfig, String pluginId) {
        List<String> enabled = pluginConfig.enabled();
        return enabled == null || enabled.isEmpty() || enabled.stream().anyMatch(pluginId::equals);
    }

    private void closeLoadedPlugins() {
        for (LoadedPlugin loadedPlugin : loadedPlugins) {
            try {
                loadedPlugin.plugin().destroy();
            } catch (Exception exception) {
                log.debug("Plugin destroy failed", exception);
            }
            closeQuietly(loadedPlugin.classLoader());
        }
        loadedPlugins.clear();
    }

    private static void closeQuietly(URLClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (IOException ignored) {
        }
    }

    private record LoadedPlugin(CommandPlugin plugin, URLClassLoader classLoader) {
    }
}

