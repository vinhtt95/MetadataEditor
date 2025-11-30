package com.vinhtt.metadataeditor.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinhtt.metadataeditor.service.IMetadataService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FfmpegMetadataService implements IMetadataService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, String> getMetadata(File file) {
        Map<String, String> metadata = new HashMap<>();
        if (file == null || !file.exists()) return metadata;

        try {
            // Lệnh FFprobe để lấy toàn bộ thông tin dưới dạng JSON
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "quiet",            // Tắt log thừa
                    "-print_format", "json",  // Xuất ra JSON
                    "-show_format",           // Lấy thông tin container (bao gồm tags)
                    // "-show_streams",       // Bỏ comment nếu muốn lấy thêm bitrate, codec video/audio
                    file.getAbsolutePath()
            );

            Process process = pb.start();

            // Parse JSON đầu ra bằng Jackson
            JsonNode root = objectMapper.readTree(process.getInputStream());

            if (root.has("format")) {
                JsonNode format = root.get("format");

                // 1. Lấy thông tin cơ bản
                if (format.has("duration")) metadata.put("Duration", format.get("duration").asText() + " s");
                if (format.has("size")) metadata.put("Size", format.get("size").asText() + " bytes");
                if (format.has("bit_rate")) metadata.put("Bitrate", format.get("bit_rate").asText() + " bps");

                // 2. Lấy Tags (Genres, Studio, Title...)
                if (format.has("tags")) {
                    JsonNode tags = format.get("tags");
                    Iterator<String> fieldNames = tags.fieldNames();

                    while (fieldNames.hasNext()) {
                        String key = fieldNames.next();
                        String value = tags.get(key).asText();

                        // Chuẩn hóa key cho đẹp (Tùy chọn)
                        String displayKey = capitalize(key);
                        metadata.put(displayKey, value);
                    }
                }
            }

            // Đợi process kết thúc để không bị zombie process
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            metadata.put("Error", "Could not read metadata: " + e.getMessage());
        }

        return metadata;
    }

    // Hàm phụ viết hoa chữ cái đầu: "genre" -> "Genre"
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public void removeMetadata(File inputFile) throws IOException, InterruptedException {
        String inputPath = inputFile.getAbsolutePath();

        // Tạo file tạm: video.mp4 -> video_clean.mp4
        // Logic đổi tên để tránh trùng lặp
        File parent = inputFile.getParentFile();
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf(".");
        String nameNoExt = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        String ext = (dotIndex == -1) ? "" : name.substring(dotIndex);

        File outputFile = new File(parent, nameNoExt + "_clean" + ext);

        // Lệnh: ffmpeg -i input -map_metadata -1 -c copy output
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", inputPath,
                "-map_metadata", "-1",
                "-c", "copy",
                outputFile.getAbsolutePath()
        );

        // Redirect error stream để debug nếu cần, nhưng không block main thread
        // pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // Xóa file gốc, đổi tên file clean thành file gốc
            if (inputFile.delete()) {
                outputFile.renameTo(inputFile);
            }
        } else {
            throw new IOException("FFmpeg exited with code " + exitCode);
        }
    }
}