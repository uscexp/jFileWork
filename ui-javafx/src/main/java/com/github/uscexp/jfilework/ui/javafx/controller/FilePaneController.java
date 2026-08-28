package com.github.uscexp.jfilework.ui.javafx.controller;

import com.github.uscexp.jfilework.core.fs.FileEntry;
import com.github.uscexp.jfilework.core.fs.FileService;
import com.github.uscexp.jfilework.core.fs.FileWatcherService;
import com.github.uscexp.jfilework.ui.javafx.model.FileItemViewModel;
import com.github.uscexp.jfilework.ui.javafx.model.PaneStateViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable controller for one file pane.
 */
public class FilePaneController {

    private static final Logger log = LoggerFactory.getLogger(FilePaneController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    @FXML private Label paneTitleLabel;
    @FXML private TextField pathField;
    @FXML private TableView<FileItemViewModel> fileTable;
    @FXML private TableColumn<FileItemViewModel, String> nameColumn;
    @FXML private TableColumn<FileItemViewModel, String> sizeColumn;
    @FXML private TableColumn<FileItemViewModel, String> modifiedColumn;
    @FXML private TableColumn<FileItemViewModel, String> typeColumn;

    private final PaneStateViewModel paneState = new PaneStateViewModel();

    private FileService fileService;
    private Consumer<String> statusConsumer = ignored -> { };
    private FileWatcherService fileWatcherService;
    private String paneId = "pane";

    @FXML
    public void initialize() {
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        sizeColumn.setCellValueFactory(cell -> {
            FileItemViewModel item = cell.getValue();
            String value = (item.isDirectory() || item.isParentLink()) ? "" : String.valueOf(item.getSize());
            return new SimpleStringProperty(value);
        });
        modifiedColumn.setCellValueFactory(cell -> {
            FileItemViewModel item = cell.getValue();
            String value = item.isParentLink() ? "" : DATE_TIME_FORMATTER.format(item.getLastModified());
            return new SimpleStringProperty(value);
        });
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));

        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileTable.setRowFactory(tableView -> {
            TableRow<FileItemViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    navigateByRow(row.getItem());
                }
            });
            return row;
        });

        fileTable.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<FileItemViewModel>) change -> {
            List<Path> selectedPaths = new ArrayList<>();
            for (FileItemViewModel item : fileTable.getSelectionModel().getSelectedItems()) {
                selectedPaths.add(item.getPath());
            }
            paneState.getSelectedPaths().setAll(selectedPaths);
        });
    }

    public void configure(String paneId, String paneTitle, FileService fileService, Consumer<String> statusConsumer) {
        this.paneId = paneId;
        this.fileService = fileService;
        this.statusConsumer = statusConsumer == null ? ignored -> { } : statusConsumer;

        paneTitleLabel.setText(paneTitle);
        pathField.setId(paneId + "PathField");
        fileTable.setId(paneId + "FileTable");
    }

    public void navigateTo(Path path) {
        if (path == null) {
            return;
        }
        if (!Files.exists(path)) {
            statusConsumer.accept("Path does not exist: " + path);
            return;
        }
        if (!Files.isDirectory(path)) {
            statusConsumer.accept("Path is not a directory: " + path);
            return;
        }

        paneState.setCurrentPath(path.normalize());
        pathField.setText(path.normalize().toString());
        refreshCurrentDirectory();
        restartWatcher();
    }

    public PaneStateViewModel getPaneState() {
        return paneState;
    }

    public TableView<FileItemViewModel> getFileTable() {
        return fileTable;
    }

    @FXML
    private void onPathEntered() {
        String rawPath = pathField.getText();
        if (rawPath == null || rawPath.isBlank()) {
            return;
        }
        navigateTo(Paths.get(rawPath.trim()));
    }

    public void stopWatching() {
        if (fileWatcherService != null) {
            try {
                fileWatcherService.close();
            } catch (IOException e) {
                log.debug("Could not close watch service for pane {}", paneId, e);
            }
            fileWatcherService = null;
        }
    }

    private void navigateByRow(FileItemViewModel row) {
        if (row.isDirectory() || row.isParentLink()) {
            navigateTo(row.getPath());
        }
    }

    private void refreshCurrentDirectory() {
        Path path = paneState.getCurrentPath();
        if (path == null) {
            return;
        }

        try {
            List<FileItemViewModel> rows = new ArrayList<>();
            Path parent = path.getParent();
            if (parent != null) {
                rows.add(new FileItemViewModel(FileEntry.parent(parent)));
            }

            List<FileItemViewModel> fromService = fileService
                    .listDirectory(path)
                    .stream()
                    .map(FileItemViewModel::new)
                    .toList();
            rows.addAll(fromService);
            fileTable.setItems(FXCollections.observableArrayList(rows));
            statusConsumer.accept(String.format("%s: %d item(s)", paneId, rows.size()));
        } catch (IOException e) {
            log.warn("Cannot read directory {}", path, e);
            statusConsumer.accept("Cannot read directory: " + path);
            fileTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void restartWatcher() {
        stopWatching();

        Path currentPath = paneState.getCurrentPath();
        if (currentPath == null) {
            return;
        }

        try {
            fileWatcherService = new FileWatcherService();
            fileWatcherService.watch(currentPath, () -> Platform.runLater(this::refreshCurrentDirectory));
        } catch (IOException e) {
            log.debug("Watch service disabled for pane {} at {}", paneId, currentPath, e);
        }
    }
}

