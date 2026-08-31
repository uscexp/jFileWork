package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.command.CancellationToken;
import com.github.uscexp.jfilework.core.command.Command;
import com.github.uscexp.jfilework.core.command.CommandContext;
import com.github.uscexp.jfilework.core.command.CommandExecutor;
import com.github.uscexp.jfilework.core.command.CommandRegistry;
import com.github.uscexp.jfilework.core.command.CommandResult;
import com.github.uscexp.jfilework.core.command.ProgressSink;
import com.github.uscexp.jfilework.core.command.SelectionSnapshot;
import com.github.uscexp.jfilework.core.command.builtin.CopyCommand;
import com.github.uscexp.jfilework.core.command.builtin.DeleteCommand;
import com.github.uscexp.jfilework.core.command.builtin.MkdirCommand;
import com.github.uscexp.jfilework.core.command.builtin.MoveCommand;
import com.github.uscexp.jfilework.core.command.builtin.RenameCommand;
import com.github.uscexp.jfilework.core.fs.FileService;
import com.github.uscexp.jfilework.core.fs.LocalFileService;
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the main application window (main-window.fxml).
 */
public class MainWindowController {

    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    @FXML private MenuBar menuBar;
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

    private FilePaneController leftPaneController;
    private FilePaneController rightPaneController;

    @FXML
    public void initialize() {
        statusBarController.bind(statusLabel);
        registerBuiltInCommands();

        try {
            leftPaneController = mountFilePane(leftPane, "left", "Left pane");
            rightPaneController = mountFilePane(rightPane, "right", "Right pane");

            Path leftStartPath = resolveStartPath("jfilework.leftStartPath");
            Path rightStartPath = resolveStartPath("jfilework.rightStartPath");

            leftPaneController.navigateTo(leftStartPath);
            rightPaneController.navigateTo(rightStartPath);

            statusBarController.showMessage("Ready");
            log.info("Main window initialised with left={} and right={}", leftStartPath, rightStartPath);
        } catch (IOException e) {
            log.error("Cannot initialise file panes", e);
            statusBarController.showMessage("Cannot initialize UI: " + e.getMessage());
        }

        splitPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::onGlobalKeyPressed);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Menu: File
    // -------------------------------------------------------------------------

    @FXML
    private void onExit() {
        if (leftPaneController != null) {
            leftPaneController.stopWatching();
        }
        if (rightPaneController != null) {
            rightPaneController.stopWatching();
        }
        commandExecutor.close();
        Platform.exit();
    }

    @FXML
    private void onCopy() {
        executeCommand("file.copy", Map.of());
    }

    @FXML
    private void onMove() {
        executeCommand("file.move", Map.of());
    }

    @FXML
    private void onMkdir() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Folder");
        dialog.setHeaderText("Create folder in active pane");
        dialog.setContentText("Folder name:");
        Optional<String> value = dialog.showAndWait();
        value.ifPresent(name -> executeCommand("file.mkdir", Map.of("name", name)));
    }

    @FXML
    private void onDelete() {
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete selected files?",
                deleteButton,
                cancelButton);
        alert.setTitle("Delete");
        alert.setHeaderText("Confirm delete");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            executeCommand("file.delete", Map.of());
        } else {
            statusBarController.showMessage("Delete cancelled.");
        }
    }

    @FXML
    private void onRename() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename selected file/folder");
        dialog.setContentText("New name:");
        Optional<String> value = dialog.showAndWait();
        value.ifPresent(name -> executeCommand("file.rename", Map.of("name", name)));
    }

    // -------------------------------------------------------------------------
    // Menu: View
    // -------------------------------------------------------------------------

    @FXML
    private void onPreferences() {
        statusBarController.showMessage("Preferences are available in M3");
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

    private void registerBuiltInCommands() {
        commandRegistry.register(new CopyCommand());
        commandRegistry.register(new MoveCommand());
        commandRegistry.register(new MkdirCommand());
        commandRegistry.register(new DeleteCommand());
        commandRegistry.register(new RenameCommand());
    }

    private void onGlobalKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F5) {
            onCopy();
            event.consume();
        } else if (event.getCode() == KeyCode.F6) {
            onMove();
            event.consume();
        } else if (event.getCode() == KeyCode.F7) {
            onMkdir();
            event.consume();
        } else if (event.getCode() == KeyCode.F8) {
            onDelete();
            event.consume();
        } else if (event.getCode() == KeyCode.F2) {
            onRename();
            event.consume();
        }
    }

    private void executeCommand(String commandId, Map<String, String> params) {
        Optional<Command> optionalCommand = commandRegistry.findById(commandId);
        if (optionalCommand.isEmpty()) {
            statusBarController.showMessage("Unknown command: " + commandId);
            return;
        }

        CommandContext context = buildContext();
        if (context == null) {
            return;
        }

        Stage owner = statusLabel.getScene() == null ? null : (Stage) statusLabel.getScene().getWindow();
        ProgressDialogController progressDialog = owner == null
                ? null
                : new ProgressDialogController(owner, context.cancellationToken(), "Running: " + commandId);

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
                }
        );

        CompletableFuture<CommandResult> future = commandExecutor.execute(wrapped, instrumentedContext, params);
        future.thenAccept(result -> Platform.runLater(() -> {
            if (progressDialog != null) {
                progressDialog.close();
            }
            leftPaneController.refreshCurrentDirectoryView();
            rightPaneController.refreshCurrentDirectoryView();
            statusBarController.showMessage(result.message());
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
                ProgressSink.NO_OP
        );
    }

    private FilePaneController mountFilePane(VBox target, String paneId, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/uscexp/jfilework/ui/javafx/file-pane.fxml"));
        Parent paneRoot = loader.load();
        FilePaneController controller = loader.getController();
        controller.configure(paneId, title, fileService, this::updateStatus);

        target.getChildren().setAll(paneRoot);
        target.focusedProperty().addListener((obs, oldV, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
                statusBarController.showMessage("Active pane: " + activePaneTracker.getActivePane());
            }
        });

        Node table = controller.getFileTable();
        table.focusedProperty().addListener((obs, oldV, focused) -> {
            if (focused) {
                activePaneTracker.setActivePane("left".equals(paneId)
                        ? ActivePaneTracker.PaneSide.LEFT
                        : ActivePaneTracker.PaneSide.RIGHT);
            }
        });

        return controller;
    }

    private Path resolveStartPath(String propertyKey) {
        String configuredPath = System.getProperty(propertyKey);
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path path = Paths.get(configuredPath.trim());
            if (fileService.exists(path) && fileService.isDirectory(path)) {
                return path;
            }
            log.warn("Configured start path '{}' is invalid for {}", configuredPath, propertyKey);
        }

        return Paths.get(System.getProperty("user.home"));
    }

    private void updateStatus(String message) {
        statusBarController.showMessage(message);
    }
}
