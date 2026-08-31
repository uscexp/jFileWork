package com.github.uscexp.jfilework.core.command;

/**
 * Result of command execution.
 */
public record CommandResult(CommandStatus status, String message, long processedItems) {

    public static CommandResult success(String message, long processedItems) {
        return new CommandResult(CommandStatus.SUCCESS, message, processedItems);
    }

    public static CommandResult partial(String message, long processedItems) {
        return new CommandResult(CommandStatus.PARTIAL, message, processedItems);
    }

    public static CommandResult failed(String message) {
        return new CommandResult(CommandStatus.FAILED, message, 0L);
    }

    public static CommandResult cancelled(String message, long processedItems) {
        return new CommandResult(CommandStatus.CANCELLED, message, processedItems);
    }
}

