package com.vinhtt.metadataeditor.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface IMetadataService {

    /**
     * Trích xuất thông tin metadata từ file video.
     * @param file File video đầu vào.
     * @return Map chứa key-value của metadata (ví dụ: "Duration" -> "00:05:20").
     */
    Map<String, String> getMetadata(File file);

    /**
     * Xóa toàn bộ metadata của file video.
     * Phương thức này sẽ tạo file tạm sạch metadata, sau đó ghi đè lên file gốc.
     * @param file File video cần xử lý.
     * @throws IOException Lỗi đọc/ghi file.
     * @throws InterruptedException Lỗi khi process bị ngắt.
     */
    void removeMetadata(File file) throws IOException, InterruptedException;
}