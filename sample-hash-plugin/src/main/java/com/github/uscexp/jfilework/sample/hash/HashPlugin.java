package com.github.uscexp.jfilework.sample.hash;

import com.github.uscexp.jfilework.api.plugin.CommandPlugin;
import com.github.uscexp.jfilework.api.plugin.PluginCommand;
import com.github.uscexp.jfilework.api.plugin.PluginContext;
import com.github.uscexp.jfilework.api.plugin.PluginDescriptor;
import org.slf4j.Logger;

import java.util.List;

/**
 * Demo plugin contributing SHA-256 hashing commands.
 */
public class HashPlugin implements CommandPlugin {

    private static final PluginDescriptor DESCRIPTOR = new PluginDescriptor(
            "sample-hash-plugin",
            "Sample Hash Plugin",
            "1.0.0",
            "1.0"
    );

    private Logger logger;

    @Override
    public PluginDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void initialize(PluginContext context) {
        this.logger = context.logger();
        if (logger != null) {
            logger.info("Initialized plugin {}", DESCRIPTOR.id());
        }
    }

    @Override
    public List<PluginCommand> commands() {
        return List.of(new Sha256Command());
    }
}
