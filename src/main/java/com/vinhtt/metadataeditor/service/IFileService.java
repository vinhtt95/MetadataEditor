package com.vinhtt.metadataeditor.service;

import com.vinhtt.metadataeditor.model.VideoFile;
import java.io.File;
import java.util.List;

public interface IFileService {
    /**
     * Lấy danh sách các file video trong một thư mục.
     * @param directory Thư mục cần quét.
     * @return Danh sách các đối tượng VideoFile.
     */
    List<VideoFile> getVideoFiles(File directory);
}