package top.yumbo.ai.reviewer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Project领域模型单元测试
 */
@DisplayName("Project领域模型测试")
class ProjectTest {

    private Project project;
    private Path projectRoot;

    @BeforeEach
    void setUp() {
        projectRoot = Paths.get("/test/project");
        project = Project.builder()
                .name("TestProject")
                .rootPath(projectRoot)
                .type(ProjectType.JAVA)
                .structureTree("test-tree")
                .build();
    }

    @Nested
    @DisplayName("基本属性测试")
    class BasicPropertiesTest {

        @Test
        @DisplayName("应该正确创建Project对象")
        void shouldCreateProjectWithBasicProperties() {
            assertThat(project).isNotNull();
            assertThat(project.getName()).isEqualTo("TestProject");
            assertThat(project.getRootPath()).isEqualTo(projectRoot);
            assertThat(project.getType()).isEqualTo(ProjectType.JAVA);
            assertThat(project.getStructureTree()).isEqualTo("test-tree");
        }

        @Test
        @DisplayName("应该正确初始化空的源文件列表")
        void shouldInitializeEmptySourceFilesList() {
            assertThat(project.getSourceFiles()).isNotNull();
            assertThat(project.getSourceFiles()).isEmpty();
        }

        @Test
        @DisplayName("应该正确初始化元数据")
        void shouldInitializeMetadata() {
            assertThat(project.getMetadata()).isNotNull();
            assertThat(project.getMetadata().getFileCount()).isZero();
        }
    }

    @Nested
    @DisplayName("addSourceFile()方法测试")
    class AddSourceFileTest {

        @Test
        @DisplayName("应该成功添加源文件")
        void shouldAddSourceFileSuccessfully() {
            SourceFile file = createTestSourceFile("Test.java");

            project.addSourceFile(file);

            assertThat(project.getSourceFiles()).hasSize(1);
            assertThat(project.getSourceFiles()).contains(file);
            assertThat(project.getMetadata().getFileCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("应该忽略null源文件")
        void shouldIgnoreNullSourceFile() {
            project.addSourceFile(null);

            assertThat(project.getSourceFiles()).isEmpty();
            assertThat(project.getMetadata().getFileCount()).isZero();
        }

        @Test
        @DisplayName("应该避免添加重复的源文件")
        void shouldAvoidDuplicateSourceFiles() {
            SourceFile file = createTestSourceFile("Test.java");

            project.addSourceFile(file);
            project.addSourceFile(file); // 重复添加

            assertThat(project.getSourceFiles()).hasSize(1);
            assertThat(project.getMetadata().getFileCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("应该成功添加多个不同的源文件")
        void shouldAddMultipleDifferentSourceFiles() {
            SourceFile file1 = createTestSourceFile("Test1.java");
            SourceFile file2 = createTestSourceFile("Test2.java");
            SourceFile file3 = createTestSourceFile("Test3.java");

            project.addSourceFile(file1);
            project.addSourceFile(file2);
            project.addSourceFile(file3);

            assertThat(project.getSourceFiles()).hasSize(3);
            assertThat(project.getMetadata().getFileCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getCoreFiles()方法测试")
    class GetCoreFilesTest {

        @Test
        @DisplayName("应该返回所有核心文件")
        void shouldReturnAllCoreFiles() {
            SourceFile coreFile1 = createCoreSourceFile("Main.java");
            SourceFile coreFile2 = createCoreSourceFile("Config.xml");
            SourceFile normalFile = createTestSourceFile("Util.java");

            project.addSourceFile(coreFile1);
            project.addSourceFile(coreFile2);
            project.addSourceFile(normalFile);

            List<SourceFile> coreFiles = project.getCoreFiles();

            assertThat(coreFiles).hasSize(2);
            assertThat(coreFiles).contains(coreFile1, coreFile2);
            assertThat(coreFiles).doesNotContain(normalFile);
        }

        @Test
        @DisplayName("当没有核心文件时应该返回空列表")
        void shouldReturnEmptyListWhenNoCoreFiles() {
            SourceFile normalFile = createTestSourceFile("Util.java");
            project.addSourceFile(normalFile);

            List<SourceFile> coreFiles = project.getCoreFiles();

            assertThat(coreFiles).isEmpty();
        }

        @Test
        @DisplayName("当项目为空时应该返回空列表")
        void shouldReturnEmptyListWhenProjectIsEmpty() {
            List<SourceFile> coreFiles = project.getCoreFiles();

            assertThat(coreFiles).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTotalLines()方法测试")
    class GetTotalLinesTest {

        @Test
        @DisplayName("应该正确计算总行数")
        void shouldCalculateTotalLinesCorrectly() {
            SourceFile file1 = createSourceFileWithLines("Test1.java", 100);
            SourceFile file2 = createSourceFileWithLines("Test2.java", 200);
            SourceFile file3 = createSourceFileWithLines("Test3.java", 50);

            project.addSourceFile(file1);
            project.addSourceFile(file2);
            project.addSourceFile(file3);

            assertThat(project.getTotalLines()).isEqualTo(350);
        }

        @Test
        @DisplayName("当项目为空时应该返回0")
        void shouldReturnZeroWhenProjectIsEmpty() {
            assertThat(project.getTotalLines()).isZero();
        }

        @Test
        @DisplayName("应该忽略行数为0的文件")
        void shouldIgnoreFilesWithZeroLines() {
            SourceFile file1 = createSourceFileWithLines("Test1.java", 100);
            SourceFile file2 = createSourceFileWithLines("Empty.java", 0);

            project.addSourceFile(file1);
            project.addSourceFile(file2);

            assertThat(project.getTotalLines()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("isValid()方法测试")
    class IsValidTest {

        @Test
        @DisplayName("有效的项目应该返回true")
        void shouldReturnTrueForValidProject() {
            Project validProject = Project.builder()
                    .name("ValidProject")
                    .rootPath(Paths.get("/valid/path"))
                    .type(ProjectType.JAVA)
                    .build();

            assertThat(validProject.isValid()).isTrue();
        }

        @Test
        @DisplayName("name为null时应该返回false")
        void shouldReturnFalseWhenNameIsNull() {
            Project invalidProject = Project.builder()
                    .name(null)
                    .rootPath(Paths.get("/path"))
                    .type(ProjectType.JAVA)
                    .build();

            assertThat(invalidProject.isValid()).isFalse();
        }

        @Test
        @DisplayName("name为空字符串时应该返回false")
        void shouldReturnFalseWhenNameIsBlank() {
            Project invalidProject = Project.builder()
                    .name("   ")
                    .rootPath(Paths.get("/path"))
                    .type(ProjectType.JAVA)
                    .build();

            assertThat(invalidProject.isValid()).isFalse();
        }

        @Test
        @DisplayName("rootPath为null时应该返回false")
        void shouldReturnFalseWhenRootPathIsNull() {
            Project invalidProject = Project.builder()
                    .name("TestProject")
                    .rootPath(null)
                    .type(ProjectType.JAVA)
                    .build();

            assertThat(invalidProject.isValid()).isFalse();
        }

        @Test
        @DisplayName("type为null时应该返回false")
        void shouldReturnFalseWhenTypeIsNull() {
            Project invalidProject = Project.builder()
                    .name("TestProject")
                    .rootPath(Paths.get("/path"))
                    .type(null)
                    .build();

            assertThat(invalidProject.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("getLanguage()方法测试")
    class GetLanguageTest {

        @Test
        @DisplayName("应该返回Java语言")
        void shouldReturnJavaLanguage() {
            Project javaProject = Project.builder()
                    .name("JavaProject")
                    .rootPath(projectRoot)
                    .type(ProjectType.JAVA)
                    .build();

            assertThat(javaProject.getLanguage()).isEqualTo("java");
        }

        @Test
        @DisplayName("应该返回Python语言")
        void shouldReturnPythonLanguage() {
            Project pythonProject = Project.builder()
                    .name("PythonProject")
                    .rootPath(projectRoot)
                    .type(ProjectType.PYTHON)
                    .build();

            assertThat(pythonProject.getLanguage()).isEqualTo("python");
        }

        @Test
        @DisplayName("应该返回JavaScript语言")
        void shouldReturnJavaScriptLanguage() {
            Project jsProject = Project.builder()
                    .name("JSProject")
                    .rootPath(projectRoot)
                    .type(ProjectType.JAVASCRIPT)
                    .build();

            assertThat(jsProject.getLanguage()).isEqualTo("javascript");
        }
    }

    // ========== 辅助方法 ==========

    private SourceFile createTestSourceFile(String fileName) {
        return SourceFile.builder()
                .path(Paths.get("/test/" + fileName))
                .fileName(fileName)
                .relativePath(fileName)
                .extension(getExtension(fileName))
                .lineCount(100)
                .sizeInBytes(1024L)
                .isCore(false)
                .build();
    }

    private SourceFile createCoreSourceFile(String fileName) {
        return SourceFile.builder()
                .path(Paths.get("/test/" + fileName))
                .fileName(fileName)
                .relativePath(fileName)
                .extension(getExtension(fileName))
                .lineCount(100)
                .sizeInBytes(1024L)
                .isCore(true) // 标记为核心文件
                .build();
    }

    private SourceFile createSourceFileWithLines(String fileName, int lineCount) {
        return SourceFile.builder()
                .path(Paths.get("/test/" + fileName))
                .fileName(fileName)
                .relativePath(fileName)
                .extension(getExtension(fileName))
                .lineCount(lineCount)
                .sizeInBytes(1024L)
                .build();
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}

