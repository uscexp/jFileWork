package com.github.uscexp.jfilework.core.command;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory command registry.
 */
public class CommandRegistry {

    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    public void register(Command command) {
        commands.put(command.id(), command);
    }

    public Optional<Command> findById(String commandId) {
        return Optional.ofNullable(commands.get(commandId));
    }
}

