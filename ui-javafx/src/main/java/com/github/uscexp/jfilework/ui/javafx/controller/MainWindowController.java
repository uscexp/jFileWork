package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.command.CancellationToken;
import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandExecutor;
import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.command.CommandStatus;
import com.github.uscexp.jfilework.core.command.ProgressSink;
import com.github.uscexp.jfilework.core.command.ProcessOutputSink;
import com.github.uscexp.jfilework.core.command.SelectionSnapshot;
import com.github.uscexp.jfilework.core.command.builtin.CopyCommand;
import com.github.uscexp.jfilework.core.command.builtin.DeleteCommand;
import com.github.uscexp.jfilework.core.command.builtin.MkdirCommand;
import com.github.uscexp.jfilework.core.command.builtin.MoveCommand;
import com.github.uscexp.jfilework.core.command.builtin.OsExecCommand;
import com.github.uscexp.jfilework.core.command.builtin.RenameCommand;
import com.github.uscexp.jfilework.core.config.AppConfig;
import com.github.uscexp.jfilework.core.config.ConfigService;
import com.github.uscexp.jfilework.core.config.SecurityConfig;
import com.github.uscexp.jfilework.core.fs.FileService;
import com.github.uscexp.jfilework.core.fs.LocalFileService;
import com.github.uscexp.jfilework.core.security.CommandPolicy;
import com.github.uscexp.jfilework.core.security.ParamSanitizer;
import com.github.uscexp.jfilework.core.security.PathPolicy;
import com.github.uscexp.jfilework.ui.javafx.action.CommandExecutionHandler;
import com.github.uscexp.jfilework.ui.javafx.action.ConfiguredAction;
import com.github.uscexp.jfilework.ui.javafx.action.KeyBindingService;
import com.github.uscexp.jfilework.ui.javafx.action.MenuBuilder;
import com.github.uscexp.jfilework.ui.javafx.action.ParamTemplateEngine;
import com.github.uscexp.jfilework.ui.javafx.action.ToolbarBuilder;
import com.github.uscexp.jfilework.ui.javafx.action.UiActionMapper;
import com.github.uscexp.jfilework.ui.javafx.session.SessionService;
import com.github.uscexp.jfilework.ui.javafx.session.SessionState;
import com.github.uscexp.jfilework.ui.javafx.model.ActivePaneTracker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the main application window (main-window.fxml).
 */
public class MainWindowController {

    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    @FXML private Menu fileMenu;
    @FXML private Menu toolsMenu;
    @FXML private ToolBar toolBar;
    @FXML private SplitPane splitPane;
    @FXML private VBox leftPane;
    @FXML private VBox rightPane;
    @FXML private Label statusLabel;

    private final FileService fileService = new LocalFileService();
    private final ActivePaneTracker activePaneTracker = new ActivePaneTracker();
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final CommandExecutor commandExecutor = new CommandExecutor();
    private final StatusBarController statusBarController = new StatusBarController();
    private final ConfigService configService = new ConfigService();
    private final ParamTemplateEngine paramTemplateEngine = new ParamTemplateEngine();
    private final KeyBindingService keyBindingService = new KeyBindingService();
    private final ToolbarBuilder toolbarBuilder = new ToolbarBuilder();
    private final MenuBuilder menuBuilder = new MenuBuilder();
    private final PreferencesController preferencesController = new PreferencesController();
    private final SessionService sessionService = new SessionService();

    private FilePaneController leftPaneController;
    private FilePaneController rightPaneController;
    private List<ConfiguredAction> configuredActions = List.of();
    private boolean keyBindingsInstalled;
    private AppConfig appConfig = AppConfig.defaults();
    private SessionState loadedSession;

    @FXML
    public void initialize() {
        statusBarController.bind(statusLabel);
        appConfig = configService.load();
        registerBuiltInCommands(appConfig.security());
        loadedSession = sessionService.load().orElse(null);

        try {
            leftPaneController = mountFilePane(leftPane, "left", "Left pane");
            rightPaneController = mountFilePane(rightPane, "right", "Right pane");

            Path leftStartPath = resolveStartPath(
                    "jfilework.leftStartPath",
                    appConfig.ui().leftStartPath(),
                    loadedSession == null ? null : loadedSession.leftPath()
            );
            Path rightStartPath = resolveStartPath(
                    "jfilework.rightStartPath",
                    appConfig.ui().rightStartPath(),
                    loadedSession == null ? null : loadedSession.rightPath()
            );

            leftPaneController.navigateTo(leftStartPath);
            rightPaneController.navigateTo(rightStartPath);

            configuredActions = new UiActionMapper(commandRegistry).map(appConfig);
            CommandExecutionHandler handler = this::executeConfiguredCommand;
            toolbarBuilder.build(toolBar, configuredActions, handler);
            menuBuilder.build(fileMenu, toolsMenu, configuredActions, handler);

            statusBarController.showMessage("Ready");
            log.info("Main window initialised with left={} and right={}", leftStartPath, rightStartPath);
        } catch (IOException e) {
            log.error("Cannot initialise file panes", e);
            statusBarController.showMessage("Cannot initialize UI: " + e.getMessage());
        }

        splitPane.sceneProperty().addListener((ignoredObs, ignoredOldScene, newScene) -> {
            if (newScene != null) {
                if (!keyBindingsInstalled) {
                    keyBindingService.bind(newScene, configuredActions, this::executeConfiguredCommand);
                    keyBindingsInstalled = true;
                }
                if (newScene.getWindow() instanceof Stage stage) {
                    applySessionToStage(stage);
                    stage.setOnCloseRequest(ignoredEvent -> saveSession(stage));
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Menu: File
    // -------------------------------------------------------------------------

    @FXML
    private void onExit() {
        if (statusLabel.getScene() != null && statusLabel.getScene().getWindow() instanceof Stage stage) {
            saveSession(stage);
        }
        if (leftPaneController != null) {
            leftPaneController.stopWatching();
        }
        if (rightPaneController != null) {
            rightPaneController.stopWatching();
        }
        commandExecutor.close();
        Platform.exit();
    }

    private boolean confirmDelete() {
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete selected files?",
                deleteButton,
                cancelButton);
        alert.setTitle("Delete");
        alert.setHeaderText("Confirm delete");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    // -------------------------------------------------------------------------
    // Menu: View
    // -------------------------------------------------------------------------

    @FXML
    private void onPreferences() {
        Stage owner = statusLabel.getScene() == null ? null : (Stage) statusLabel.getScene().getWindow();
        appConfig = ensureNonNullUiConfig(appConfig);

        Optional<com.github.uscexp.jfilework.core.config.UiConfig> maybeUpdated = preferencesController.showDialog(
                owner,
                appConfig.ui(),
                leftPaneController == null || leftPaneController.getCurrentPath() == null ? "" : leftPaneController.getCurrentPath().toString(),
                rightPaneController == null || rightPaneController.getCurrentPath() == null ? "" : rightPaneController.getCurrentPath().toString()
        );

        if (maybeUpdated.isEmpty()) {
            statusBarController.showMessage("Preferences unchanged.");
            return;
        }

        appConfig = new AppConfig(maybeUpdated.get(), appConfig.actions(), appConfig.security());
        try {
            configService.save(configService.resolveConfigPath(), appConfig);
            statusBarController.showMessage("Preferences saved to " + configService.resolveConfigPath());
        } catch (IOException exception) {
            log.warn("Cannot save preferences", exception);
            statusBarController.showMessage("Cannot save preferences: " + exception.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Menu: Help
    // -------------------------------------------------------------------------

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About jFileWork");
        alert.setHeaderText("jFileWork  v0.0.1-Alpha");
        alert.setContentText("Dual-pane file manager built with Java 26 and JavaFX 23.");
        alert.showAndWait();
    }

    private void registerBuiltInCommands(SecurityConfig securityConfig) {
        SecurityConfig effectiveSecurity = securityConfig == null ? SecurityConfig.defaults() : securityConfig;
        commandRegistry.register(new CopyCommand());
        commandRegistry.register(new MoveCommand());
        commandRegistry.register(new MkdirCommand());
        commandRegistry.register(new DeleteCommand());
        commandRegistry.register(new RenameCommand());
        commandRegistry.register(new OsExecCommand(
                new CommandPolicy(effectiveSecurity.allowOsCommands(), effectiveSecurity.allowedExecutables()),
                new PathPolicy(effectiveSecurity.allowedRoots()),
                new ParamSanitizer()
        ));
    }

    private void executeConfiguredCommand(String commandId, Map<String, String> params, String actionLabel) {
        if ("file.delete".equals(commandId) && !confirmDelete()) {
            statusBarController.showMessage("Delete cancelled.");
            return;
        }

        executeCommand(commandId, params, actionLabel);
    }

    private void executeCommand(String commandId, Map<String, String> params, String actionLabel) {
        Optional<Command> optionalCommand = commandRegistry.findById(commandId);
        if (optionalCommand.isEmpty()) {
            statusBarController.showMessage("Unknown command: " + commandId);
            return;
        }

        CommandContext context = buildContext();
        if (context == null) {
            return;
        }

        Map<String, String> resolvedParams = resolveActionParameters(params, context, actionLabel);
        if (resolvedParams == null) {
            statusBarController.showMessage(actionLabel + " cancelled.");
            return;
        }

        Stage owner = statusLabel.getScene() == null ? null : (Stage) statusLabel.getScene().getWindow();
        boolean externalProcessCommand = "os.exec".equals(commandId);
        ProgressDialogController progressDialog = !externalProcessCommand && owner != null
                ? new ProgressDialogController(owner, context.cancellationToken(), "Running: " + commandId)
                : null;
        ProcessOutputController processOutputController = externalProcessCommand && owner != null
                ? new ProcessOutputController(owner, context.cancellationToken(), "Process Output: " + actionLabel)
                : null;

        if (progressDialog != null) {
            progressDialog.show();
        }

        Command wrapped = wrapWithArtificialDelay(optionalCommand.get());
        CommandContext instrumentedContext = new CommandContext(
                context.activeDirectory(),
                context.inactiveDirectory(),
                context.selection(),
                context.cancellationToken(),
                new ProgressSink() {
                    @Override
                    public void onProgress(String label, long completedWork, long totalWork) {
                        if (progressDialog != null) {
                            progressDialog.setMessage(label + " " + completedWork + "/" + totalWork);
                        }
                    }
                },
                new ProcessOutputSink() {
                    @Override
                    public void onProcessStarted(Process process) {
                        if (processOutputController != null) {
                            processOutputController.bindProcess(process);
                        }
                    }

                    @Override
                    public void onStdOut(String line) {
                        if (processOutputController != null) {
                            processOutputController.appendStdOut(line);
                        }
                    }

                    @Override
                    public void onStdErr(String line) {
                        if (processOutputController != null) {
                            processOutputController.appendStdErr(line);
                        }
                    }

                    @Override
                    public void onFinished(int exitCode) {
                        if (processOutputController != null) {
                            processOutputController.markFinished(exitCode);
                        }
                    }
                }
        );

        CompletableFuture<CommandResult> future = commandExecutor.execute(wrapped, instrumentedContext, resolvedParams);
        future.thenAccept(result -> Platform.runLater(() -> {
            if (progressDialog != null) {
                progressDialog.close();
            }
            if (processOutputController != null && processOutputController.hasProcess() && result.status() == CommandStatus.FAILED) {
                processOutputController.markFailed(result.message());
            }
            leftPaneController.refreshCurrentDirectoryView();
            rightPaneController.refreshCurrentDirectoryView();
            statusBarController.showMessage(result.message());
            if (result.status() == CommandStatus.FAILED) {
                log.warn("Command '{}' failed: {}", commandId, result.message());
                showErrorDialog(actionLabel, result.message());
            }
        }));
    }

    private Command wrapWithArtificialDelay(Command delegate) {
        String rawDelay = System.getProperty("jfilework.command.artificialDelayMs", "0");
        long delayMillis;
        try {
            delayMillis = Long.parseLong(rawDelay);
        } catch (NumberFormatException ignored) {
            delayMillis = 0L;
        }

        if (delayMillis <= 0L) {
            return delegate;
        }

        final long finalDelayMillis = delayMillis;
        return new Command() {
            @Override
            public String id() {
                return delegate.id();
            }

            @Override
            public boolean canExecute(CommandContext context) {
                return delegate.canExecute(context);
            }

            @Override
            public CommandResult execute(CommandContext context, Map<String, String> params) throws Exception {
                Thread.sleep(finalDelayMillis);
                return delegate.execute(context, params);
            }
        };
    }

    private Map<String, String> resolveActionParameters(Map<String, String> params, CommandContext context, String actionLabel) {
        Map<String, String> resolved = paramTemplateEngine.resolve(params, context);
        Map<String, String> finalParams = new HashMap<>();
        for (Map.Entry<String, String> entry : resolved.entrySet()) {
            String value = entry.getValue();
            if (value != null && value.startsWith("${prompt:") && value.endsWith("}")) {
                String question = value.substring("${prompt:".length(), value.length() - 1).trim();
                String promptLabel = question.isBlank() ? entry.getKey() : question;

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle(actionLabel);
                dialog.setHeaderText(actionLabel);
                dialog.setContentText(promptLabel + ":");
                Optional<String> userInput = dialog.showAndWait();
                if (userInput.isEmpty()) {
                    return null;
                }
                finalParams.put(entry.getKey(), userInput.get().trim());
            } else {
                finalParams.put(entry.getKey(), value);
            }
        }
        return finalParams;
    }

    private CommandContext buildContext() {
        FilePaneController active = activePaneTracker.getActivePane() == ActivePaneTracker.PaneSide.LEFT
                ? leftPaneController
                : rightPaneController;
        FilePaneController inactive = activePaneTracker.getActivePane() == ActivePaneTracker.PaneSide.LEFT
                ? rightPaneController
                : leftPaneController;

        if (active == null || inactive == null) {
            statusBarController.showMessage("Panes are not ready yet.");
            return null;
        }

        Path activePath = active.getCurrentPath();
        Path inactivePath = inactive.getCurrentPath();
        if (activePath == null || inactivePath == null) {
            statusBarController.showMessage("Please navigate both panes first.");
            return null;
        }

        List<Path> selection = active.getSelectedPathsForOperations();
        return new CommandContext(
                activePath,
                inactivePath,
                new SelectionSnapshot(selection),
                new CancellationToken(),
                ProgressSink.NO_OP,
                ProcessOutputSink.NO_OP
        );
    }

    private FilePaneController mountFilePane(VBox target, String paneId, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/uscexp/jfilework/ui/javafx/file-pane.fxml"));
        Parent paneRoot = loader.load();
        FilePaneController controller = loader.getController();
        controller.configure(paneId, title, fileService, this::updateStatus);

        target.getChildren().setAll(paneRoot);
        target.focusedProperty().addListener((ignoredObs, ignoredOldValue, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
                statusBarController.showMessage("Active pane: " + activePaneTracker.getActivePane());
            }
        });

        Node table = controller.getFileTable();
        table.focusedProperty().addListener((ignoredObs, ignoredOldValue, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
            }
        });

        return controller;
    }

    private Path resolveStartPath(String propertyKey, String configuredUiPath, String sessionPath) {
        String configuredPath = System.getProperty(propertyKey);
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path path = Paths.get(configuredPath.trim());
            if (fileService.exists(path) && fileService.isDirectory(path)) {
                return path;
            }
            log.warn("Configured start path '{}' is invalid for {}", configuredPath, propertyKey);
        }

        if (sessionPath != null && !sessionPath.isBlank()) {
            Path path = Paths.get(sessionPath.trim());
            if (fileService.exists(path) && fileService.isDirectory(path)) {
                return path;
            }
            log.warn("Session start path '{}' is invalid for {}", sessionPath, propertyKey);
        }

        if (configuredUiPath != null && !configuredUiPath.isBlank()) {
            Path path = Paths.get(configuredUiPath.trim());
            if (fileService.exists(path) && fileService.isDirectory(path)) {
                return path;
            }
            log.warn("Configured UI start path '{}' is invalid for {}", configuredUiPath, propertyKey);
        }

        return Paths.get(System.getProperty("user.home"));
    }

    private void updateStatus(String message) {
        statusBarController.showMessage(message);
    }

    private static AppConfig ensureNonNullUiConfig(AppConfig config) {
        if (config == null) {
            return AppConfig.defaults();
        }
        if (config.ui() == null) {
            return new AppConfig(com.github.uscexp.jfilework.core.config.UiConfig.defaults(),
                    config.actions() == null ? List.of() : config.actions(),
                    config.security() == null ? SecurityConfig.defaults() : config.security());
        }
        return config;
    }

    private void showErrorDialog(String actionLabel, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(actionLabel + " failed");
        alert.setHeaderText(actionLabel + " could not be completed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applySessionToStage(Stage stage) {
        if (loadedSession == null) {
            return;
        }

        if (loadedSession.windowWidth() > 200) {
            stage.setWidth(loadedSession.windowWidth());
        }
        if (loadedSession.windowHeight() > 200) {
            stage.setHeight(loadedSession.windowHeight());
        }
        if (loadedSession.windowX() >= 0) {
            stage.setX(loadedSession.windowX());
        }
        if (loadedSession.windowY() >= 0) {
            stage.setY(loadedSession.windowY());
        }
    }

    private void saveSession(Stage stage) {
        String leftPath = leftPaneController == null || leftPaneController.getCurrentPath() == null
                ? ""
                : leftPaneController.getCurrentPath().toString();
        String rightPath = rightPaneController == null || rightPaneController.getCurrentPath() == null
                ? ""
                : rightPaneController.getCurrentPath().toString();

        SessionState sessionState = new SessionState(
                stage.getX(),
                stage.getY(),
                stage.getWidth(),
                stage.getHeight(),
                leftPath,
                rightPath
        );
        sessionService.save(sessionState);
    }
}
