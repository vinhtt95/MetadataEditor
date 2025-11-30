package com.vinhtt.metadataeditor.service.impl;

import com.vinhtt.metadataeditor.service.IMetadataService;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FfmpegMetadataService implements IMetadataService {

    @Override
    public Map<String, String> getMetadata(File file) {
        // TODO: Dùng ProcessBuilder chạy "ffprobe" để lấy JSON parse ra Map
        // Code mẫu giả lập trả về data:
        Map<String, String> meta = new HashMap<>();
        meta.put("File Name", file.getName());
        meta.put("Size", String.valueOf(file.length() / 1024) + " KB");
        meta.put("Path", file.getAbsolutePath());
        return meta;
    }

    @Override
    public void removeMetadata(File inputFile) throws IOException, InterruptedException {
        String inputPath = inputFile.getAbsolutePath();

        // Tạo file tạm output
        File outputFile = new File(inputFile.getParent(), "clean_" + inputFile.getName());

        // Lệnh FFmpeg xóa toàn bộ metadata: -map_metadata -1
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", inputPath,
                "-map_metadata", "-1",
                "-c", "copy", // Copy stream, không encode lại (nhanh)
                outputFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // Xóa file cũ, đổi tên file mới thành cũ (Logic thay thế)
            // Lưu ý: Trên Windows file đang mở có thể không xóa được ngay
            if (inputFile.delete()) {
                outputFile.renameTo(inputFile);
            }
        } else {
            throw new IOException("FFmpeg exited with code " + exitCode);
        }
    }
}