package com.vinhtt.metadataeditor.viewmodel;

import com.google.inject.Inject;
import com.vinhtt.metadataeditor.model.VideoFile;
import com.vinhtt.metadataeditor.service.IMetadataService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;
import java.util.Map;

public class MainViewModel {

    private final IMetadataService metadataService;

    // State properties
    private final ObjectProperty<VideoFile> selectedFile = new SimpleObjectProperty<>();
    private final ObservableList<Map.Entry<String, String>> metadataList = FXCollections.observableArrayList();
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);

    @Inject
    public MainViewModel(IMetadataService metadataService) {
        this.metadataService = metadataService;

        // Khi selectedFile thay đổi -> Load Metadata
        selectedFile.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMetadata(newVal.getFile());
            } else {
                metadataList.clear();
            }
        });
    }

    private void loadMetadata(File file) {
        Map<String, String> data = metadataService.getMetadata(file);
        metadataList.setAll(data.entrySet());
    }

    public void removeMetadataCurrentFile() {
        if (selectedFile.get() == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                metadataService.removeMetadata(selectedFile.get().getFile());
                return null;
            }
        };

        isProcessing.bind(task.runningProperty());

        task.setOnSucceeded(e -> loadMetadata(selectedFile.get().getFile())); // Reload data
        task.setOnFailed(e -> task.getException().printStackTrace()); // Xử lý lỗi

        new Thread(task).start();
    }

    // Getters cho View binding
    public ObjectProperty<VideoFile> selectedFileProperty() { return selectedFile; }
    public ObservableList<Map.Entry<String, String>> getMetadataList() { return metadataList; }
    public BooleanProperty isProcessingProperty() { return isProcessing; }
}