package com.github.uscexp.jfilework.core.command;

import java.util.Map;

/**
 * Executable operation in jFileWork.
 */
public interface Command {

    String id();

    default boolean canExecute(CommandContext context) {
        return true;
    }

    CommandResult execute(CommandContext context, Map<String, String> params) throws Exception;
}

