package com.vinhtt.metadataeditor.service.impl;

import com.vinhtt.metadataeditor.model.VideoFile;
import com.vinhtt.metadataeditor.service.IFileService;
import com.vinhtt.metadataeditor.util.AppConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFileService implements IFileService {

    @Override
    public List<VideoFile> getVideoFiles(File directory) {
        List<VideoFile> results = new ArrayList<>();

        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isValidVideoFile(file)) {
                        results.add(new VideoFile(file));
                    }
                }
            }
        }
        return results;
    }

    private boolean isValidVideoFile(File file) {
        if (!file.isFile() || file.isHidden()) return false;

        String name = file.getName().toLowerCase();

        // Bỏ qua file rác hệ thống
        if (name.startsWith(".") || AppConstants.IGNORED_FILES.contains(name)) return false;

        // Check đuôi video
        return AppConstants.VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}