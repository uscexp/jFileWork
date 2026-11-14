package com.github.uscexp.jfilework.ui.javafx.action;

import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.config.ActionConfig;
import com.github.uscexp.jfilework.core.config.AppConfig;
import com.github.uscexp.jfilework.core.config.SecurityConfig;
import com.github.uscexp.jfilework.core.config.UiConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UiActionMapperTest {

    private static final Command DUMMY_COMMAND = new Command() {
        @Override
        public String id() {
            return "dummy";
        }

        @Override
        public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
            return CommandResult.success("ok", 0);
        }
    };

    @Test
    void iconIsPassedThroughToConfiguredAction() {
        ActionConfig actionConfig = new ActionConfig(
                "copy", "Copy", "dummy", Map.of(), "F5", List.of("toolbar.main"), "/image/copy.png"
        );
        AppConfig appConfig = new AppConfig(
                UiConfig.defaults(), List.of(actionConfig), SecurityConfig.defaults(), null
        );

        CommandRegistry registry = new CommandRegistry() {
            @Override
            public Optional<Command> findById(String id) {
                return "dummy".equals(id) ? Optional.of(DUMMY_COMMAND) : Optional.empty();
            }
        };

        UiActionMapper mapper = new UiActionMapper(registry);
        List<ConfiguredAction> actions = mapper.map(appConfig);

        assertEquals(1, actions.size());
        assertEquals("/image/copy.png", actions.get(0).icon());
    }

    @Test
    void missingIconResultsInNull() {
        ActionConfig actionConfig = new ActionConfig(
                "hash", "Hash", "dummy", Map.of(), "", List.of("toolbar.main"), null
        );
        AppConfig appConfig = new AppConfig(
                UiConfig.defaults(), List.of(actionConfig), SecurityConfig.defaults(), null
        );

        CommandRegistry registry = new CommandRegistry() {
            @Override
            public Optional<Command> findById(String id) {
                return "dummy".equals(id) ? Optional.of(DUMMY_COMMAND) : Optional.empty();
            }
        };

        UiActionMapper mapper = new UiActionMapper(registry);
        List<ConfiguredAction> actions = mapper.map(appConfig);

        assertEquals(1, actions.size());
        assertNull(actions.get(0).icon());
    }
}
