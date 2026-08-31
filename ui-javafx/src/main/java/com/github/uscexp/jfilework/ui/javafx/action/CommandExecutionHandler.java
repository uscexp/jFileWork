package com.github.uscexp.jfilework.ui.javafx.action;

import java.util.Map;

/**
 * UI callback for executing a command resolved from configuration.
 */
@FunctionalInterface
public interface CommandExecutionHandler {

    void execute(String commandId, Map<String, String> params, String actionLabel);
}

