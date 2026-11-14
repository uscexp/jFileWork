package com.github.uscexp.jfilework.ui.javafx.action;

import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.config.ActionConfig;
import com.github.uscexp.jfilework.core.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Converts configuration entries into runtime actions and marks unknown commands as disabled.
 */
public class UiActionMapper {

    private static final Logger log = LoggerFactory.getLogger(UiActionMapper.class);

    private final CommandRegistry commandRegistry;

    public UiActionMapper(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public List<ConfiguredAction> map(AppConfig appConfig) {
        if (appConfig == null || appConfig.actions() == null) {
            return List.of();
        }

        return appConfig.actions().stream()
                .map(this::mapAction)
                .toList();
    }

    private ConfiguredAction mapAction(ActionConfig actionConfig) {
        boolean available = commandRegistry.findById(actionConfig.commandId()).isPresent();
        if (!available) {
            log.warn("Action '{}' references unknown command '{}'", actionConfig.id(), actionConfig.commandId());
        }

        return new ConfiguredAction(
                actionConfig.id(),
                actionConfig.label(),
                actionConfig.commandId(),
                actionConfig.params() == null ? Map.of() : actionConfig.params(),
                actionConfig.shortcut() == null ? "" : actionConfig.shortcut(),
                actionConfig.placement() == null ? List.of() : actionConfig.placement(),
                available,
                actionConfig.icon()
        );
    }
}

