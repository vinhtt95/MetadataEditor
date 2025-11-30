package com.vinhtt.metadataeditor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.File;

@Data
@AllArgsConstructor
public class VideoFile {
    private File file;

    @Override
    public String toString() {
        return file.getName(); // Để hiển thị tên trên TreeView
    }
}