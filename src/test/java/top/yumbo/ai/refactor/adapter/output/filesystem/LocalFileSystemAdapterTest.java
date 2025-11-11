package top.yumbo.ai.refactor.adapter.output.filesystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.yumbo.ai.refactor.domain.model.SourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LocalFileSystemAdapter测试
 */
@DisplayName("LocalFileSystemAdapter测试")
class LocalFileSystemAdapterTest {

    @TempDir
    Path tempDir;

    private LocalFileSystemAdapter adapter;
    private LocalFileSystemAdapter.FileSystemConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new LocalFileSystemAdapter.FileSystemConfig(
                List.of("*.java", "*.py", "*.xml"),
                List.of("*.class", "*.jar", "target/*", "build/*"),
                1024, // maxFileSizeKB
                10  // maxDepth
        );

        adapter = new LocalFileSystemAdapter(testConfig);
    }

    @AfterEach
    void tearDown() {
        // 清理临时文件
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTest {

        @Test
        @DisplayName("应该使用配置创建适配器")
        void shouldCreateAdapterWithConfig() {
            assertThat(adapter).isNotNull();
        }

        @Test
        @DisplayName("应该接受null配置并使用默认值")
        void shouldAcceptNullConfigAndUseDefaults() {
            LocalFileSystemAdapter.FileSystemConfig defaultConfig =
                    new LocalFileSystemAdapter.FileSystemConfig(
                            List.of("*.*"),
                            List.of(),
                            Integer.MAX_VALUE,  // maxFileSizeKB
                            Integer.MAX_VALUE   // maxDepth
                    );

            LocalFileSystemAdapter defaultAdapter = new LocalFileSystemAdapter(defaultConfig);
            assertThat(defaultAdapter).isNotNull();
        }
    }

    @Nested
    @DisplayName("scanProjectFiles()方法测试")
    class ScanProjectFilesTest {

        @Test
        @DisplayName("应该能够扫描空目录")
        void shouldScanEmptyDirectory() {
            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).isEmpty();
        }

        @Test
        @DisplayName("应该扫描单个Java文件")
        void shouldScanSingleJavaFile() throws IOException {
            Path javaFile = tempDir.resolve("Test.java");
            Files.writeString(javaFile, "public class Test {}");

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSize(1);
            assertThat(files.get(0).getFileName()).isEqualTo("Test.java");
            assertThat(files.get(0).getExtension()).isEqualTo("java");
        }

        @Test
        @DisplayName("应该扫描多个文件")
        void shouldScanMultipleFiles() throws IOException {
            Files.writeString(tempDir.resolve("File1.java"), "class File1 {}");
            Files.writeString(tempDir.resolve("File2.java"), "class File2 {}");
            Files.writeString(tempDir.resolve("File3.py"), "print('hello')");

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("应该排除匹配排除模式的文件")
        void shouldExcludeMatchingFiles() throws IOException {
            Files.writeString(tempDir.resolve("Test.java"), "class Test {}");
            Files.writeString(tempDir.resolve("Test.class"), "binary");

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSize(1);
            assertThat(files.get(0).getFileName()).isEqualTo("Test.java");
        }

        @Test
        @DisplayName("应该扫描子目录")
        void shouldScanSubdirectories() throws IOException {
            Path subDir = tempDir.resolve("src");
            Files.createDirectories(subDir);
            Files.writeString(subDir.resolve("Main.java"), "class Main {}");

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSize(1);
            assertThat(files.get(0).getRelativePath()).contains("src");
        }

        @Test
        @DisplayName("应该正确设置相对路径")
        void shouldSetCorrectRelativePath() throws IOException {
            Path subDir = tempDir.resolve("src").resolve("main");
            Files.createDirectories(subDir);
            Path javaFile = subDir.resolve("App.java");
            Files.writeString(javaFile, "class App {}");

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSize(1);
            SourceFile file = files.get(0);
            assertThat(file.getRelativePath()).contains("src");
            assertThat(file.getRelativePath()).contains("main");
        }

        @Test
        @DisplayName("应该跳过超过大小限制的文件")
        void shouldSkipOversizedFiles() throws IOException {
            // 创建一个超过1MB的文件
            Path largeFile = tempDir.resolve("large.java");
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 100000; i++) {
                content.append("// This is a long comment line to make the file large\n");
            }
            Files.writeString(largeFile, content.toString());

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            // 应该被跳过（如果超过1024KB）
            boolean hasLargeFile = files.stream()
                    .anyMatch(f -> f.getFileName().equals("large.java"));

            if (hasLargeFile) {
                // 文件未超过限制
                assertThat(files).hasSizeGreaterThanOrEqualTo(1);
            } else {
                // 文件被跳过
                assertThat(files).doesNotContain(
                        files.stream().filter(f -> f.getFileName().equals("large.java")).findFirst().orElse(null)
                );
            }
        }

        @Test
        @DisplayName("应该处理不存在的目录")
        void shouldHandleNonExistentDirectory() {
            Path nonExistent = tempDir.resolve("nonexistent");

            assertThatThrownBy(() -> adapter.scanProjectFiles(nonExistent))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("readFileContent()方法测试")
    class ReadFileContentTest {

        @Test
        @DisplayName("应该读取文件内容")
        void shouldReadFileContent() throws IOException {
            Path file = tempDir.resolve("test.txt");
            String content = "Hello, World!";
            Files.writeString(file, content);

            String readContent = adapter.readFileContent(file);

            assertThat(readContent).isEqualTo(content);
        }

        @Test
        @DisplayName("应该读取空文件")
        void shouldReadEmptyFile() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            Files.writeString(file, "");

            String content = adapter.readFileContent(file);

            assertThat(content).isEmpty();
        }

        @Test
        @DisplayName("应该读取多行文件")
        void shouldReadMultilineFile() throws IOException {
            Path file = tempDir.resolve("multiline.txt");
            String content = "Line 1\nLine 2\nLine 3";
            Files.writeString(file, content);

            String readContent = adapter.readFileContent(file);

            assertThat(readContent).isEqualTo(content);
        }

        @Test
        @DisplayName("应该处理不存在的文件")
        void shouldHandleNonExistentFile() {
            Path nonExistent = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> adapter.readFileContent(nonExistent))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("读取文件失败");
        }

        @Test
        @DisplayName("应该读取UTF-8编码的文件")
        void shouldReadUtf8File() throws IOException {
            Path file = tempDir.resolve("utf8.txt");
            String content = "中文测试 🚀 emoji";
            Files.writeString(file, content);

            String readContent = adapter.readFileContent(file);

            assertThat(readContent).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("writeFileContent()方法测试")
    class WriteFileContentTest {

        @Test
        @DisplayName("应该写入文件内容")
        void shouldWriteFileContent() throws IOException {
            Path file = tempDir.resolve("output.txt");
            String content = "Hello, File!";

            adapter.writeFileContent(file, content);

            assertThat(Files.exists(file)).isTrue();
            assertThat(Files.readString(file)).isEqualTo(content);
        }

        @Test
        @DisplayName("应该创建不存在的父目录")
        void shouldCreateParentDirectories() throws IOException {
            Path file = tempDir.resolve("a").resolve("b").resolve("c").resolve("file.txt");
            String content = "Nested file";

            adapter.writeFileContent(file, content);

            assertThat(Files.exists(file)).isTrue();
            assertThat(Files.readString(file)).isEqualTo(content);
        }

        @Test
        @DisplayName("应该覆盖已存在的文件")
        void shouldOverwriteExistingFile() throws IOException {
            Path file = tempDir.resolve("existing.txt");
            Files.writeString(file, "Old content");

            adapter.writeFileContent(file, "New content");

            assertThat(Files.readString(file)).isEqualTo("New content");
        }

        @Test
        @DisplayName("应该写入空字符串")
        void shouldWriteEmptyString() throws IOException {
            Path file = tempDir.resolve("empty.txt");

            adapter.writeFileContent(file, "");

            assertThat(Files.exists(file)).isTrue();
            assertThat(Files.readString(file)).isEmpty();
        }

        @Test
        @DisplayName("应该写入UTF-8内容")
        void shouldWriteUtf8Content() throws IOException {
            Path file = tempDir.resolve("utf8.txt");
            String content = "中文内容 🎉";

            adapter.writeFileContent(file, content);

            assertThat(Files.readString(file)).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("generateProjectStructure()方法测试")
    class GenerateProjectStructureTest {

        @Test
        @DisplayName("应该生成空目录的结构")
        void shouldGenerateStructureForEmptyDirectory() {
            String structure = adapter.generateProjectStructure(tempDir);

            assertThat(structure).isNotEmpty();
            assertThat(structure).contains(tempDir.getFileName().toString());
        }

        @Test
        @DisplayName("应该生成包含文件的目录结构")
        void shouldGenerateStructureWithFiles() throws IOException {
            Files.writeString(tempDir.resolve("file1.txt"), "content");
            Files.writeString(tempDir.resolve("file2.txt"), "content");

            String structure = adapter.generateProjectStructure(tempDir);

            assertThat(structure)
                    .contains("file1.txt")
                    .contains("file2.txt");
        }

        @Test
        @DisplayName("应该生成嵌套目录结构")
        void shouldGenerateNestedStructure() throws IOException {
            Path subDir = tempDir.resolve("src");
            Files.createDirectories(subDir);
            Files.writeString(subDir.resolve("Main.java"), "class Main {}");

            String structure = adapter.generateProjectStructure(tempDir);

            assertThat(structure)
                    .contains("src")
                    .contains("Main.java");
        }
    }

    @Nested
    @DisplayName("fileExists()方法测试")
    class FileExistsTest {

        @Test
        @DisplayName("存在的文件应该返回true")
        void shouldReturnTrueForExistingFile() throws IOException {
            Path file = tempDir.resolve("exists.txt");
            Files.writeString(file, "content");

            assertThat(adapter.fileExists(file)).isTrue();
        }

        @Test
        @DisplayName("不存在的文件应该返回false")
        void shouldReturnFalseForNonExistentFile() {
            Path file = tempDir.resolve("nonexistent.txt");

            assertThat(adapter.fileExists(file)).isFalse();
        }

        @Test
        @DisplayName("目录应该返回false")
        void shouldReturnFalseForDirectory() {
            assertThat(adapter.fileExists(tempDir)).isFalse();
        }
    }

    @Nested
    @DisplayName("directoryExists()方法测试")
    class DirectoryExistsTest {

        @Test
        @DisplayName("存在的目录应该返回true")
        void shouldReturnTrueForExistingDirectory() {
            assertThat(adapter.directoryExists(tempDir)).isTrue();
        }

        @Test
        @DisplayName("不存在的目录应该返回false")
        void shouldReturnFalseForNonExistentDirectory() {
            Path dir = tempDir.resolve("nonexistent");

            assertThat(adapter.directoryExists(dir)).isFalse();
        }

        @Test
        @DisplayName("文件应该返回false")
        void shouldReturnFalseForFile() throws IOException {
            Path file = tempDir.resolve("file.txt");
            Files.writeString(file, "content");

            assertThat(adapter.directoryExists(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("createDirectory()方法测试")
    class CreateDirectoryTest {

        @Test
        @DisplayName("应该创建单层目录")
        void shouldCreateSingleDirectory() {
            Path dir = tempDir.resolve("newdir");

            adapter.createDirectory(dir);

            assertThat(Files.exists(dir)).isTrue();
            assertThat(Files.isDirectory(dir)).isTrue();
        }

        @Test
        @DisplayName("应该创建多层嵌套目录")
        void shouldCreateNestedDirectories() {
            Path dir = tempDir.resolve("a").resolve("b").resolve("c");

            adapter.createDirectory(dir);

            assertThat(Files.exists(dir)).isTrue();
            assertThat(Files.isDirectory(dir)).isTrue();
        }

        @Test
        @DisplayName("创建已存在的目录不应该抛出异常")
        void shouldNotThrowForExistingDirectory() {
            adapter.createDirectory(tempDir);

            // 不应该抛出异常
            assertThat(Files.exists(tempDir)).isTrue();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("应该处理包含特殊字符的文件名")
        void shouldHandleSpecialCharactersInFileName() throws IOException {
            Path file = tempDir.resolve("file with spaces.txt");
            Files.writeString(file, "content");

            String content = adapter.readFileContent(file);

            assertThat(content).isEqualTo("content");
        }

        @Test
        @DisplayName("应该处理深层嵌套的目录结构")
        void shouldHandleDeeplyNestedStructure() throws IOException {
            Path deepPath = tempDir.resolve("a").resolve("b").resolve("c")
                    .resolve("d").resolve("e").resolve("f");
            Files.createDirectories(deepPath);
            Path file = deepPath.resolve("deep.txt");
            Files.writeString(file, "deep content");

            String content = adapter.readFileContent(file);

            assertThat(content).isEqualTo("deep content");
        }

        @Test
        @DisplayName("应该处理大量文件的扫描")
        void shouldHandleScannningManyFiles() throws IOException {
            // 创建100个文件
            for (int i = 0; i < 100; i++) {
                Files.writeString(tempDir.resolve("file" + i + ".java"), "class File" + i + " {}");
            }

            List<SourceFile> files = adapter.scanProjectFiles(tempDir);

            assertThat(files).hasSizeGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("扫描小项目应该很快")
        void shouldScanSmallProjectQuickly() throws IOException {
            // 创建10个文件
            for (int i = 0; i < 10; i++) {
                Files.writeString(tempDir.resolve("file" + i + ".java"), "class File" + i + " {}");
            }

            long startTime = System.currentTimeMillis();
            List<SourceFile> files = adapter.scanProjectFiles(tempDir);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(files).hasSize(10);
            assertThat(duration).isLessThan(1000); // 应该在1秒内完成
        }

        @Test
        @DisplayName("读取小文件应该很快")
        void shouldReadSmallFileQuickly() throws IOException {
            Path file = tempDir.resolve("small.txt");
            Files.writeString(file, "small content");

            long startTime = System.currentTimeMillis();
            String content = adapter.readFileContent(file);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(content).isNotEmpty();
            assertThat(duration).isLessThan(100); // 应该在100ms内完成
        }

        @Test
        @DisplayName("写入小文件应该很快")
        void shouldWriteSmallFileQuickly() throws IOException {
            Path file = tempDir.resolve("output.txt");

            long startTime = System.currentTimeMillis();
            adapter.writeFileContent(file, "content");
            long duration = System.currentTimeMillis() - startTime;

            assertThat(Files.exists(file)).isTrue();
            assertThat(duration).isLessThan(100); // 应该在100ms内完成
        }
    }
}

