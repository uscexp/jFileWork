package com.github.uscexp.jfilework.core.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanitizes and tokenizes configured process arguments without invoking a shell.
 */
public class ParamSanitizer {

    public List<String> tokenizeArguments(String rawArgs) {
        if (rawArgs == null || rawArgs.isBlank()) {
            return List.of();
        }
        if (rawArgs.indexOf('\0') >= 0 || rawArgs.contains("\n") || rawArgs.contains("\r")) {
            throw new PolicyException("Arguments contain forbidden control characters.");
        }
        if (rawArgs.contains("&&") || rawArgs.contains("||")) {
            throw new PolicyException("Arguments contain forbidden shell chaining operators.");
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < rawArgs.length(); i++) {
            char ch = rawArgs.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (Character.isWhitespace(ch) && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            throw new PolicyException("Arguments contain unmatched quotes.");
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return List.copyOf(tokens);
    }
}

