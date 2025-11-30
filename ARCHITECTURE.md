# VIDEO METADATA CLEANER - ARCHITECTURE DOCUMENT

## 1. Project Overview
Ứng dụng Java Desktop để quản lý và xóa hàng loạt metadata (EXIF, properties) của các video clip.
Dự án được xây dựng dựa trên các tiêu chuẩn hiện đại: giao diện Dark Mode, kiến trúc tách biệt (Decoupled), và dễ dàng mở rộng.

## 2. Technology Stack

### Core
* **Language:** Java 17+ (LTS)
* **Build Tool:** Apache Maven
* **UI Framework:** JavaFX 17+
* **Theme:** AtlantaFX (Dracula/Nord Theme) - Modern Dark Mode support.

### Libraries & Utilities
* **Dependency Injection:** Google Guice (hoặc CDI) - Để đảm bảo SOLID (DIP).
* **Boilerplate Reduction:** Project Lombok.
* **Metadata Engine:** FFmpeg (via ProcessBuilder hoặc JAVE2 Wrapper) / ExifTool.
* **Reactive/Binding:** JavaFX Properties & Bindings.
* **Logging:** SLF4J + Logback.

## 3. High-Level Architecture (MVVM Pattern)

Dự án áp dụng mô hình **Model - View - ViewModel**.

### 3.1. The Layers
1.  **View (UI Layer):**
    * Chịu trách nhiệm hiển thị dữ liệu và nhận tương tác người dùng.
    * Thành phần: FXML files, CSS, và các class `View` (Controllers).
    * **Nguyên tắc:** "Stupid View" - View không chứa business logic, chỉ binding dữ liệu từ ViewModel.

2.  **ViewModel (Presentation Logic Layer):**
    * Là cầu nối giữa View và Model.
    * Chứa trạng thái của UI (State) dưới dạng `Property` (ví dụ: `selectedFile`, `isLoading`).
    * Chứa các lệnh (Commands) để xử lý sự kiện (ví dụ: `onDeleteMetadata`).
    * Không tham chiếu trực tiếp đến các node của JavaFX (Buttons, TextFields).

3.  **Model (Data Layer):**
    * Các POJO/Record đại diện cho dữ liệu thực tế (`VideoFile`, `MetadataInfo`).
    * Không chứa logic xử lý.

4.  **Service Layer (Business Logic):**
    * Thực hiện các tác vụ nặng: Đọc file hệ thống, gọi FFmpeg, xử lý chuỗi.
    * Được tiêm (Inject) vào ViewModel thông qua Interfaces.

---

## 4. Project Structure (Maven Standard)

```text
src
├── main
│   ├── java
│   │   └── com.yourname.videocleaner
│   │       ├── App.java                   # Entry point (DI setup, load CSS)
│   │       ├── Launcher.java              # Fix JavaFX module path issue
│   │       │
│   │       ├── model                      # Data classes
│   │       │   ├── VideoFile.java
│   │       │   └── MetadataInfo.java
│   │       │
│   │       ├── view                       # UI Controllers
│   │       │   ├── MainView.java          # Code-behind for FXML
│   │       │   └── components             # Custom UI components (nếu có)
│   │       │
│   │       ├── viewmodel                  # Logic cho UI
│   │       │   ├── MainViewModel.java
│   │       │   └── TreeItemViewModel.java
│   │       │
│   │       ├── service                    # Business Logic Interfaces & Impl
│   │       │   ├── IFileService.java
│   │       │   ├── IMetadataService.java  # <--- Core Interface
│   │       │   ├── impl
│   │       │   │   ├── LocalFileService.java
│   │       │   │   └── FfmpegMetadataService.java
│   │       │
│   │       └── util                       # Helper classes
│   │           ├── DialogUtils.java
│   │           └── ConcurrencyUtils.java
│   │
│   └── resources
│       ├── fxml                           # Layout definitions
│       │   └── MainView.fxml
│       ├── css
│       │   └── styles.css                 # Custom overrides cho AtlantaFX
│       └── assets                         # Icons, Images
│
└── test                                   # Unit Tests
```

## 5. SOLID Principles Implementation

### Single Responsibility Principle (SRP)
* **MainView:** Chỉ lo việc layout và binding.
* **MainViewModel:** Chỉ lo quản lý trạng thái UI và điều phối lệnh.
* **FfmpegMetadataService:** Chỉ lo việc giao tiếp với process FFmpeg.

### Open/Closed Principle (OCP)
* Hệ thống được thiết kế để hỗ trợ nhiều engine xóa metadata khác nhau.
* Nếu muốn chuyển từ FFmpeg sang ExifTool, chỉ cần tạo class `ExifToolService` implement `IMetadataService` mà không cần sửa code trong ViewModel.

### Liskov Substitution Principle (LSP)
* Mọi implementation của `IMetadataService` đều phải đảm bảo đúng contract (input file -> output file sạch metadata) mà không gây lỗi runtime bất ngờ cho ViewModel.

### Interface Segregation Principle (ISP)
* Chia nhỏ interface nếu cần thiết. Ví dụ tách `IFileReader` và `IFileWriter` nếu có module chỉ cần đọc mà không cần ghi.

### Dependency Inversion Principle (DIP)
* ViewModel **không khởi tạo** `new FfmpegMetadataService()`.
* ViewModel nhận dependency qua Constructor:
    ```java
    @Inject
    public MainViewModel(IMetadataService metadataService, IFileService fileService) { ... }
    ```
* Google Guice sẽ lo việc cung cấp instance cụ thể lúc runtime.

---

## 6. Key Components & Data Flow

### 6.1. TreeView (File Explorer)
* **Logic:** Sử dụng `Lazy Loading` (chỉ load danh sách file con khi user mở folder cha) để tối ưu hiệu năng nếu folder có hàng nghìn file.
* **Binding:** `TreeView` bind với `rootFolder` property trong ViewModel.

### 6.2. Metadata Processing (FFmpeg)
* **Extract:** Chạy lệnh `ffprobe` để lấy JSON metadata và parse vào object `MetadataInfo`.
* **Remove:**
    * Input: `video.mp4`
    * Command: `ffmpeg -i video.mp4 -map_metadata -1 -c:v copy -c:a copy video_clean.mp4`
    * Output: File mới không còn metadata.
    * Action: Xóa file cũ, đổi tên file mới thành file cũ (hoặc giữ file backup tùy setting).

### 6.3. Concurrency (Multithreading)
* Việc đọc metadata và xóa metadata là tác vụ I/O blocking.
* **Bắt buộc:** Không chạy trên JavaFX Application Thread.
* **Giải pháp:** Sử dụng `javafx.concurrent.Task` hoặc `CompletableFuture`.
* Trong lúc Task chạy, ViewModel set `isProcessing = true` => UI hiện Loading Spinner và disable các nút bấm.

---

## 7. Maven Dependencies (Dự kiến)

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>

    <dependency>
        <groupId>io.github.mkpaz</groupId>
        <artifactId>atlantafx-base</artifactId>
        <version>2.0.1</version>
    </dependency>

    <dependency>
        <groupId>com.google.inject</groupId>
        <artifactId>guice</artifactId>
        <version>7.0.0</version>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
</dependencies>
```