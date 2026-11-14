package com.github.uscexp.jfilework.ui.javafx.action;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * Builds toolbar buttons from configured actions.
 * Buttons show icons (configurable via action.icon) with the label as tooltip.
 * For os.exec actions without a configured icon, the icon of the executable is used if available.
 */
public class ToolbarBuilder {

    private static final int ICON_SIZE = 20;

    public void build(ToolBar toolBar, List<ConfiguredAction> actions, CommandExecutionHandler handler) {
        toolBar.getItems().clear();

        actions.stream()
                .filter(action -> action.placement().contains("toolbar.main"))
                .forEach(action -> {
                    Button button = new Button();
                    button.setId("action-" + action.id());
                    button.setDisable(!action.enabled());
                    button.setOnAction(event -> handler.execute(action.commandId(), action.params(), action.label()));

                    Image image = resolveIcon(action);
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(ICON_SIZE);
                        imageView.setFitHeight(ICON_SIZE);
                        imageView.setPreserveRatio(true);
                        button.setGraphic(imageView);
                    } else {
                        button.setText(action.label());
                    }

                    button.setTooltip(new Tooltip(action.label()));
                    toolBar.getItems().add(button);
                });
    }

    /**
     * Resolves the icon for a toolbar action.
     * <p>
     * Priority:
     * <ol>
     *     <li>{@link ConfiguredAction#icon()} from app-config.json (if set)</li>
     *     <li>For {@code os.exec} actions: icon extracted from the configured {@code exe} path</li>
     * </ol>
     *
     * @return a JavaFX {@link Image} or {@code null} if no icon could be resolved
     */
    private Image resolveIcon(ConfiguredAction action) {
        String iconPath = action.icon();
        if (iconPath != null && !iconPath.isBlank()) {
            Image image = new Image(getClass().getResourceAsStream(iconPath));
            if (image != null) {
                return image;
            }
        }

        if ("os.exec".equals(action.commandId())) {
            String exePath = action.params().get("exe");
            return ExeIconResolver.resolve(exePath);
        }

        return null;
    }
}
