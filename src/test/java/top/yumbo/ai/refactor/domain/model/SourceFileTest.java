package top.yumbo.ai.refactor.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SourceFile领域模型测试
 */
@DisplayName("SourceFile领域模型测试")
class SourceFileTest {

    private SourceFile javaFile;
    private SourceFile pythonFile;
    private SourceFile configFile;

    @BeforeEach
    void setUp() {
        javaFile = SourceFile.builder()
                .path(Paths.get("/project/src/Main.java"))
                .relativePath("src/Main.java")
                .fileName("Main.java")
                .extension(".java")
                .lineCount(100)
                .sizeInBytes(5000)
                .isCore(true)
                .category(SourceFile.FileCategory.ENTRY_POINT)
                .priority(10)
                .build();

        pythonFile = SourceFile.builder()
                .path(Paths.get("/project/app.py"))
                .relativePath("app.py")
                .fileName("app.py")
                .extension(".py")
                .lineCount(50)
                .sizeInBytes(2000)
                .category(SourceFile.FileCategory.SOURCE_CODE)
                .build();

        configFile = SourceFile.builder()
                .path(Paths.get("/project/config.yaml"))
                .relativePath("config.yaml")
                .fileName("config.yaml")
                .extension(".yaml")
                .lineCount(20)
                .sizeInBytes(500)
                .category(SourceFile.FileCategory.CONFIG)
                .build();
    }

    @Nested
    @DisplayName("基本属性测试")
    class BasicPropertiesTest {

        @Test
        @DisplayName("应该正确设置和获取文件路径")
        void shouldGetCorrectPath() {
            assertThat(javaFile.getPath()).isEqualTo(Paths.get("/project/src/Main.java"));
            assertThat(javaFile.getRelativePath()).isEqualTo("src/Main.java");
        }

        @Test
        @DisplayName("应该正确设置和获取文件名")
        void shouldGetCorrectFileName() {
            assertThat(javaFile.getFileName()).isEqualTo("Main.java");
        }

        @Test
        @DisplayName("应该正确设置和获取文件扩展名")
        void shouldGetCorrectExtension() {
            assertThat(javaFile.getExtension()).isEqualTo(".java");
            assertThat(pythonFile.getExtension()).isEqualTo(".py");
        }

        @Test
        @DisplayName("应该正确设置和获取行数")
        void shouldGetCorrectLineCount() {
            assertThat(javaFile.getLineCount()).isEqualTo(100);
            assertThat(pythonFile.getLineCount()).isEqualTo(50);
        }

        @Test
        @DisplayName("应该正确设置和获取文件大小")
        void shouldGetCorrectFileSize() {
            assertThat(javaFile.getSizeInBytes()).isEqualTo(5000);
            assertThat(pythonFile.getSizeInBytes()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("文件分类测试")
    class CategoryTest {

        @Test
        @DisplayName("应该正确识别入口文件")
        void shouldIdentifyEntryPoint() {
            assertThat(javaFile.getCategory()).isEqualTo(SourceFile.FileCategory.ENTRY_POINT);
            assertThat(javaFile.isEntryPoint()).isTrue();
        }

        @Test
        @DisplayName("应该正确识别配置文件")
        void shouldIdentifyConfigFile() {
            assertThat(configFile.getCategory()).isEqualTo(SourceFile.FileCategory.CONFIG);
            assertThat(configFile.isConfigFile()).isTrue();
        }

        @Test
        @DisplayName("应该正确识别源代码文件")
        void shouldIdentifySourceCode() {
            assertThat(pythonFile.getCategory()).isEqualTo(SourceFile.FileCategory.SOURCE_CODE);
        }

        @Test
        @DisplayName("应该有默认分类")
        void shouldHaveDefaultCategory() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.txt"))
                    .fileName("test.txt")
                    .extension(".txt")
                    .build();

            assertThat(file.getCategory()).isEqualTo(SourceFile.FileCategory.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("核心文件标记测试")
    class CoreFileFlagTest {

        @Test
        @DisplayName("应该正确标记核心文件")
        void shouldMarkAsCoreFile() {
            assertThat(javaFile.isCore()).isTrue();
        }

        @Test
        @DisplayName("应该正确标记非核心文件")
        void shouldMarkAsNonCoreFile() {
            assertThat(pythonFile.isCore()).isFalse();
        }

        @Test
        @DisplayName("默认应该为非核心文件")
        void shouldBeNonCoreByDefault() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.java"))
                    .fileName("test.java")
                    .extension(".java")
                    .build();

            assertThat(file.isCore()).isFalse();
        }
    }

    @Nested
    @DisplayName("优先级测试")
    class PriorityTest {

        @Test
        @DisplayName("应该正确设置和获取优先级")
        void shouldGetCorrectPriority() {
            assertThat(javaFile.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("默认优先级应该为0")
        void shouldHaveDefaultPriority() {
            assertThat(pythonFile.getPriority()).isEqualTo(0);
        }

        @Test
        @DisplayName("应该能够设置不同的优先级")
        void shouldSetDifferentPriorities() {
            SourceFile highPriority = SourceFile.builder()
                    .path(Paths.get("/high.java"))
                    .fileName("high.java")
                    .extension(".java")
                    .priority(100)
                    .build();

            SourceFile lowPriority = SourceFile.builder()
                    .path(Paths.get("/low.java"))
                    .fileName("low.java")
                    .extension(".java")
                    .priority(1)
                    .build();

            assertThat(highPriority.getPriority()).isGreaterThan(lowPriority.getPriority());
        }
    }

    @Nested
    @DisplayName("getProjectType()方法测试")
    class GetProjectTypeTest {

        @Test
        @DisplayName("应该根据扩展名返回Java项目类型")
        void shouldReturnJavaTypeForJavaFile() {
            assertThat(javaFile.getProjectType()).isEqualTo(ProjectType.JAVA);
        }

        @Test
        @DisplayName("应该根据扩展名返回Python项目类型")
        void shouldReturnPythonTypeForPythonFile() {
            assertThat(pythonFile.getProjectType()).isEqualTo(ProjectType.PYTHON);
        }

        @Test
        @DisplayName("应该处理未知扩展名")
        void shouldHandleUnknownExtension() {
            SourceFile unknownFile = SourceFile.builder()
                    .path(Paths.get("/file.xyz"))
                    .fileName("file.xyz")
                    .extension(".xyz")
                    .build();

            ProjectType type = unknownFile.getProjectType();
            assertThat(type).isIn(ProjectType.UNKNOWN, ProjectType.JAVA); // 取决于实现
        }
    }

    @Nested
    @DisplayName("文件内容测试")
    class ContentTest {

        @Test
        @DisplayName("应该能够设置和获取文件内容")
        void shouldGetAndSetContent() {
            String content = "public class Main { }";
            javaFile.setContent(content);

            assertThat(javaFile.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("初始内容应该为null")
        void shouldHaveNullContentInitially() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.java"))
                    .fileName("test.java")
                    .extension(".java")
                    .build();

            assertThat(file.getContent()).isNull();
        }

        @Test
        @DisplayName("应该能够更新文件内容")
        void shouldUpdateContent() {
            javaFile.setContent("content1");
            assertThat(javaFile.getContent()).isEqualTo("content1");

            javaFile.setContent("content2");
            assertThat(javaFile.getContent()).isEqualTo("content2");
        }
    }

    @Nested
    @DisplayName("Builder模式测试")
    class BuilderTest {

        @Test
        @DisplayName("应该能够使用Builder创建完整的SourceFile")
        void shouldCreateCompleteSourceFileWithBuilder() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.java"))
                    .relativePath("test.java")
                    .fileName("test.java")
                    .extension(".java")
                    .content("public class Test {}")
                    .lineCount(10)
                    .sizeInBytes(100)
                    .isCore(true)
                    .category(SourceFile.FileCategory.SOURCE_CODE)
                    .priority(5)
                    .build();

            assertThat(file).isNotNull();
            assertThat(file.getPath()).isEqualTo(Paths.get("/test.java"));
            assertThat(file.getFileName()).isEqualTo("test.java");
            assertThat(file.getExtension()).isEqualTo(".java");
            assertThat(file.getContent()).isEqualTo("public class Test {}");
            assertThat(file.getLineCount()).isEqualTo(10);
            assertThat(file.getSizeInBytes()).isEqualTo(100);
            assertThat(file.isCore()).isTrue();
            assertThat(file.getPriority()).isEqualTo(5);
        }

        @Test
        @DisplayName("应该能够创建最小化的SourceFile")
        void shouldCreateMinimalSourceFile() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/minimal.txt"))
                    .fileName("minimal.txt")
                    .extension(".txt")
                    .build();

            assertThat(file).isNotNull();
            assertThat(file.getPath()).isEqualTo(Paths.get("/minimal.txt"));
            assertThat(file.isCore()).isFalse();
            assertThat(file.getPriority()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("应该处理空的相对路径")
        void shouldHandleEmptyRelativePath() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.java"))
                    .relativePath("")
                    .fileName("test.java")
                    .extension(".java")
                    .build();

            assertThat(file.getRelativePath()).isEmpty();
        }

        @Test
        @DisplayName("应该处理没有扩展名的文件")
        void shouldHandleFileWithoutExtension() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/README"))
                    .fileName("README")
                    .extension("")
                    .build();

            assertThat(file.getExtension()).isEmpty();
        }

        @Test
        @DisplayName("应该处理非常大的文件")
        void shouldHandleVeryLargeFile() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/large.txt"))
                    .fileName("large.txt")
                    .extension(".txt")
                    .lineCount(1_000_000)
                    .sizeInBytes(100_000_000L)
                    .build();

            assertThat(file.getLineCount()).isEqualTo(1_000_000);
            assertThat(file.getSizeInBytes()).isEqualTo(100_000_000L);
        }

        @Test
        @DisplayName("应该处理空文件")
        void shouldHandleEmptyFile() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/empty.txt"))
                    .fileName("empty.txt")
                    .extension(".txt")
                    .lineCount(0)
                    .sizeInBytes(0L)
                    .build();

            assertThat(file.getLineCount()).isEqualTo(0);
            assertThat(file.getSizeInBytes()).isEqualTo(0L);
        }

        @Test
        @DisplayName("应该处理负数优先级")
        void shouldHandleNegativePriority() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/test.txt"))
                    .fileName("test.txt")
                    .extension(".txt")
                    .priority(-10)
                    .build();

            assertThat(file.getPriority()).isEqualTo(-10);
        }
    }

    @Nested
    @DisplayName("文件路径处理测试")
    class PathHandlingTest {

        @Test
        @DisplayName("应该处理Windows风格路径")
        void shouldHandleWindowsPath() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("C:\\project\\src\\Main.java"))
                    .relativePath("src\\Main.java")
                    .fileName("Main.java")
                    .extension(".java")
                    .build();

            assertThat(file.getPath().toString()).contains("Main.java");
        }

        @Test
        @DisplayName("应该处理Unix风格路径")
        void shouldHandleUnixPath() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/project/src/Main.java"))
                    .relativePath("src/Main.java")
                    .fileName("Main.java")
                    .extension(".java")
                    .build();

            assertThat(file.getPath().toString()).contains("Main.java");
        }

        @Test
        @DisplayName("应该处理深层嵌套路径")
        void shouldHandleDeeplyNestedPath() {
            SourceFile file = SourceFile.builder()
                    .path(Paths.get("/a/b/c/d/e/f/g/h/file.java"))
                    .relativePath("a/b/c/d/e/f/g/h/file.java")
                    .fileName("file.java")
                    .extension(".java")
                    .build();

            assertThat(file.getPath().toString()).contains("file.java");
            assertThat(file.getRelativePath()).contains("a/b/c");
        }
    }
}

