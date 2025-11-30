package com.vinhtt.metadataeditor.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AppConstants {
    // Danh sách các định dạng video được hỗ trợ
    public static final Set<String> VIDEO_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv",
            ".webm", ".m4v", ".mpg", ".mpeg", ".3gp", ".ts", ".vob", ".ogv"
    )));

    // Các file/folder hệ thống cần bỏ qua
    public static final Set<String> IGNORED_FILES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ".ds_store", "thumbs.db", "desktop.ini"
    )));
}