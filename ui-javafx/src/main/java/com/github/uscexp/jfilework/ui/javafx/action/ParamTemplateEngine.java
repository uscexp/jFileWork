package com.github.uscexp.jfilework.ui.javafx.action;

import com.github.uscexp.jfilework.core.command.CommandContext;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolves simple placeholders used by command parameter templates.
 */
public class ParamTemplateEngine {

    public Map<String, String> resolve(Map<String, String> templateParams, CommandContext context) {
        Map<String, String> resolved = new HashMap<>();
        if (templateParams == null) {
            return resolved;
        }

        for (Map.Entry<String, String> entry : templateParams.entrySet()) {
            resolved.put(entry.getKey(), resolveValue(entry.getValue(), context));
        }
        return resolved;
    }

    private String resolveValue(String value, CommandContext context) {
        if (value == null) {
            return "";
        }

        String result = value;
        result = result.replace("${activePane.path}", context.activeDirectory().toString());
        result = result.replace("${inactivePane.path}", context.inactiveDirectory().toString());
        result = result.replace("${selection.paths}", context.selection().paths().stream()
                .map(Path::toString)
                .collect(Collectors.joining(" ")));
        return result;
    }
}

