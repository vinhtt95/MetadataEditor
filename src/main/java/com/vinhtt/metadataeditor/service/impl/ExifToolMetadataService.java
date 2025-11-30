package com.vinhtt.metadataeditor.service.impl;

import com.vinhtt.metadataeditor.service.IMetadataService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExifToolMetadataService implements IMetadataService {

    private static final String EXIFTOOL_CMD = "exiftool"; // Hoặc đường dẫn tuyệt đối
    private static final String FFMPEG_CMD = "ffmpeg";     // Hoặc đường dẫn tuyệt đối

    // Danh sách các định dạng ExifTool KHÔNG hỗ trợ ghi -> Phải dùng FFmpeg
    private static final Set<String> USE_FFMPEG_EXTENSIONS = Set.of(
            ".wmv", ".avi", ".flv", ".webm", ".mpg", ".mpeg", ".vob", ".3gp"
    );

    @Override
    public Map<String, String> getMetadata(File file) {
        Map<String, String> meta = new HashMap<>();
        try {
            // ExifTool đọc được hầu hết định dạng (kể cả WMV) nên vẫn dùng nó để lấy thông tin
            ProcessBuilder pb = new ProcessBuilder(EXIFTOOL_CMD, "-m", "-s", "-G", file.getAbsolutePath());
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        meta.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return meta;
    }

    @Override
    public void removeMetadata(File file) throws IOException, InterruptedException {
        String name = file.getName().toLowerCase();

        // Kiểm tra đuôi file để chọn công cụ phù hợp
        boolean useFfmpeg = USE_FFMPEG_EXTENSIONS.stream().anyMatch(name::endsWith);

        if (useFfmpeg) {
            removeByFfmpeg(file);
        } else {
            removeByExifTool(file);
        }
    }

    private void removeByExifTool(File file) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                EXIFTOOL_CMD,
                "-all=",
                "-m",
                "-overwrite_original",
                file.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Nếu ExifTool thất bại (ví dụ file MP4 bị lỗi cấu trúc),
            // có thể fallback sang FFmpeg luôn ở đây nếu muốn chắc ăn.
            // System.err.println("ExifTool failed, trying FFmpeg fallback...");
            // removeByFfmpeg(file);
            throw new IOException("ExifTool failed (Code " + exitCode + "). Output:\n" + output);
        }
    }

    private void removeByFfmpeg(File inputFile) throws IOException, InterruptedException {
        String inputPath = inputFile.getAbsolutePath();

        // Logic tạo file tạm giống bài trước
        File parent = inputFile.getParentFile();
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf(".");
        String nameNoExt = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        String ext = (dotIndex == -1) ? "" : name.substring(dotIndex);

        File outputFile = new File(parent, nameNoExt + "_clean" + ext);

        // Lệnh FFmpeg để copy stream và xóa metadata
        ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_CMD, "-y", "-i", inputPath,
                "-map_metadata", "-1",
                "-c", "copy", // Quan trọng: Copy stream để giữ nguyên chất lượng
                outputFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Đọc log để tránh buffer đầy gây treo process
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {}
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // Xóa file gốc, đổi tên file mới
            if (inputFile.delete()) {
                outputFile.renameTo(inputFile);
            } else {
                throw new IOException("Cannot delete original file: " + inputPath);
            }
        } else {
            // Nếu lỗi thì xóa file tạm đi cho sạch
            outputFile.delete();
            throw new IOException("FFmpeg failed with code " + exitCode);
        }
    }
}