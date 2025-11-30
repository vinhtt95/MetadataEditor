package com.vinhtt.metadataeditor.view;

import com.google.inject.Inject;
import com.vinhtt.metadataeditor.model.VideoFile;
import com.vinhtt.metadataeditor.util.AppConstants;
import com.vinhtt.metadataeditor.viewmodel.MainViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Map;

public class MainViewController {

    @FXML private TreeView<VideoFile> fileTreeView;
    @FXML private TableView<Map.Entry<String, String>> metadataTable;
    @FXML private TableColumn<Map.Entry<String, String>, String> colKey;
    @FXML private TableColumn<Map.Entry<String, String>, String> colValue;
    @FXML private Button btnRemoveSingle;
    @FXML private ProgressBar progressBar;

    private final MainViewModel viewModel;

    @Inject
    public MainViewController(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    public void initialize() {
        // 1. Setup Table Columns
        colKey.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        colValue.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));

        // 2. Bind Data
        metadataTable.setItems(viewModel.getMetadataList());

        // 3. Bind Selection TreeView -> ViewModel
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                viewModel.selectedFileProperty().set(newVal.getValue());
            }
        });

        // 4. Bind Loading State
        progressBar.visibleProperty().bind(viewModel.isProcessingProperty());
        btnRemoveSingle.disableProperty().bind(viewModel.isProcessingProperty());
    }

    @FXML
    public void onSelectFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        File dir = dc.showDialog(null);
        if (dir != null) {
            TreeItem<VideoFile> root = new TreeItem<>(new VideoFile(dir));
            buildTree(root, dir);
            fileTreeView.setRoot(root);
            root.setExpanded(true);
        }
    }

    private void buildTree(TreeItem<VideoFile> parent, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            // Bỏ qua file ẩn và file hệ thống
            if (f.isHidden() || f.getName().startsWith(".") || AppConstants.IGNORED_FILES.contains(f.getName().toLowerCase())) {
                continue;
            }

            boolean isVideo = f.isFile() && isVideoFile(f.getName());

            // Chỉ hiển thị Folder hoặc Video File
            if (f.isDirectory() || isVideo) {
                TreeItem<VideoFile> item = new TreeItem<>(new VideoFile(f));
                parent.getChildren().add(item);

                // Đệ quy nếu là thư mục
                if (f.isDirectory()) {
                    buildTree(item, f);
                }
            }
        }
    }

    private boolean isVideoFile(String name) {
        String lowerName = name.toLowerCase();
        return AppConstants.VIDEO_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
    }

    @FXML
    public void onRemoveSingle() {
        viewModel.removeMetadataCurrentFile();
    }
}