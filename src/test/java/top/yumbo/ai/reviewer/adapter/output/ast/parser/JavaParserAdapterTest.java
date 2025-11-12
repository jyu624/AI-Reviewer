package top.yumbo.ai.reviewer.adapter.output.ast.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ProjectType;
import top.yumbo.ai.reviewer.domain.model.SourceFile;
import top.yumbo.ai.reviewer.domain.model.ast.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * JavaParserAdapter测试
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
class JavaParserAdapterTest {

    private JavaParserAdapter parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new JavaParserAdapter();
    }

    @Test
    void testSupportsJavaProject() {
        assertThat(parser.supports("JAVA")).isTrue();
        assertThat(parser.supports("PYTHON")).isFalse();
    }

    @Test
    void testGetParserName() {
        assertThat(parser.getParserName()).isEqualTo("JavaParser");
    }

    @Test
    void testParseSimpleJavaClass() throws IOException {
        // 创建一个简单的Java类文件
        String javaCode = """
            package com.example;
            
            public class HelloWorld {
                private String message;
                
                public HelloWorld(String message) {
                    this.message = message;
                }
                
                public void sayHello() {
                    System.out.println(message);
                }
                
                public String getMessage() {
                    return message;
                }
            }
            """;

        Path javaFile = tempDir.resolve("HelloWorld.java");
        Files.writeString(javaFile, javaCode);

        // 创建项目
        Project project = Project.builder()
            .name("test-project")
            .rootPath(tempDir)
            .type(ProjectType.JAVA)
            .build();

        SourceFile sourceFile = SourceFile.builder()
            .path(javaFile)
            .relativePath("HelloWorld.java")
            .fileName("HelloWorld.java")
            .extension("java")
            .content(javaCode)
            .lineCount(16)
            .build();

        project.addSourceFile(sourceFile);

        // 解析项目
        CodeInsight insight = parser.parseProject(project);

        // 验证结果
        assertThat(insight).isNotNull();
        assertThat(insight.getProjectName()).isEqualTo("test-project");
        assertThat(insight.getClasses()).isNotEmpty();

        // 验证类结构
        ClassStructure helloWorldClass = insight.getClasses().get(0);
        assertThat(helloWorldClass.getClassName()).isEqualTo("HelloWorld");
        assertThat(helloWorldClass.getPackageName()).isEqualTo("com.example");
        assertThat(helloWorldClass.getAccessModifier()).isEqualTo(ClassStructure.AccessModifier.PUBLIC);

        // 验证字段
        assertThat(helloWorldClass.getFields()).hasSize(1);
        FieldInfo field = helloWorldClass.getFields().get(0);
        assertThat(field.getFieldName()).isEqualTo("message");
        assertThat(field.getType()).isEqualTo("String");

        // 验证方法
        assertThat(helloWorldClass.getMethods()).hasSize(3); // 构造器 + 2个方法
        assertThat(helloWorldClass.getMethods())
            .extracting(MethodInfo::getMethodName)
            .contains("HelloWorld", "sayHello", "getMessage");
    }

    @Test
    void testCalculateComplexity() throws IOException {
        // 创建一个有复杂度的方法
        String javaCode = """
            package com.example;
            
            public class ComplexMethod {
                public int calculate(int x) {
                    int result = 0;
                    
                    if (x > 0) {
                        for (int i = 0; i < x; i++) {
                            if (i % 2 == 0) {
                                result += i;
                            } else {
                                result -= i;
                            }
                        }
                    } else if (x < 0) {
                        result = -1;
                    } else {
                        result = 0;
                    }
                    
                    return result;
                }
            }
            """;

        Path javaFile = tempDir.resolve("ComplexMethod.java");
        Files.writeString(javaFile, javaCode);

        Project project = Project.builder()
            .name("test-project")
            .rootPath(tempDir)
            .type(ProjectType.JAVA)
            .build();

        SourceFile sourceFile = SourceFile.builder()
            .path(javaFile)
            .relativePath("ComplexMethod.java")
            .fileName("ComplexMethod.java")
            .extension("java")
            .content(javaCode)
            .lineCount(25)
            .build();

        project.addSourceFile(sourceFile);

        CodeInsight insight = parser.parseProject(project);

        // 验证复杂度计算
        ClassStructure clazz = insight.getClasses().get(0);
        MethodInfo method = clazz.getMethods().get(0);

        assertThat(method.getCyclomaticComplexity()).isGreaterThan(1);
        assertThat(method.getCyclomaticComplexity()).isLessThanOrEqualTo(6); // 3个if + 1个for + 1个else if + 基础1

        // 验证复杂度指标
        assertThat(insight.getComplexityMetrics()).isNotNull();
        assertThat(insight.getComplexityMetrics().getTotalMethods()).isEqualTo(1);
    }

    @Test
    void testDetectCodeSmells() throws IOException {
        // 创建一个有代码坏味道的类
        String javaCode = """
            package com.example;
            
            public class BadSmell {
                // 长方法
                public void veryLongMethod() {
                    System.out.println("Line 1");
                    System.out.println("Line 2");
                    System.out.println("Line 3");
                    System.out.println("Line 4");
                    System.out.println("Line 5");
                    System.out.println("Line 6");
                    System.out.println("Line 7");
                    System.out.println("Line 8");
                    System.out.println("Line 9");
                    System.out.println("Line 10");
                    System.out.println("Line 11");
                    System.out.println("Line 12");
                    System.out.println("Line 13");
                    System.out.println("Line 14");
                    System.out.println("Line 15");
                    System.out.println("Line 16");
                    System.out.println("Line 17");
                    System.out.println("Line 18");
                    System.out.println("Line 19");
                    System.out.println("Line 20");
                    System.out.println("Line 21");
                    System.out.println("Line 22");
                    System.out.println("Line 23");
                    System.out.println("Line 24");
                    System.out.println("Line 25");
                    System.out.println("Line 26");
                    System.out.println("Line 27");
                    System.out.println("Line 28");
                    System.out.println("Line 29");
                    System.out.println("Line 30");
                    System.out.println("Line 31");
                    System.out.println("Line 32");
                    System.out.println("Line 33");
                    System.out.println("Line 34");
                    System.out.println("Line 35");
                    System.out.println("Line 36");
                    System.out.println("Line 37");
                    System.out.println("Line 38");
                    System.out.println("Line 39");
                    System.out.println("Line 40");
                    System.out.println("Line 41");
                    System.out.println("Line 42");
                    System.out.println("Line 43");
                    System.out.println("Line 44");
                    System.out.println("Line 45");
                    System.out.println("Line 46");
                    System.out.println("Line 47");
                    System.out.println("Line 48");
                    System.out.println("Line 49");
                    System.out.println("Line 50");
                    System.out.println("Line 51");
                }
                
                // 参数过多
                public void tooManyParameters(int a, int b, int c, int d, int e, int f) {
                    System.out.println(a + b + c + d + e + f);
                }
            }
            """;

        Path javaFile = tempDir.resolve("BadSmell.java");
        Files.writeString(javaFile, javaCode);

        Project project = Project.builder()
            .name("test-project")
            .rootPath(tempDir)
            .type(ProjectType.JAVA)
            .build();

        SourceFile sourceFile = SourceFile.builder()
            .path(javaFile)
            .relativePath("BadSmell.java")
            .fileName("BadSmell.java")
            .extension("java")
            .content(javaCode)
            .lineCount(62)
            .build();

        project.addSourceFile(sourceFile);

        CodeInsight insight = parser.parseProject(project);

        // 验证代码坏味道检测
        assertThat(insight.getCodeSmells()).isNotEmpty();

        // 应该检测到长方法
        assertThat(insight.getCodeSmells())
            .extracting(CodeSmell::getType)
            .contains(CodeSmell.SmellType.LONG_METHOD);

        // 应该检测到参数过多
        assertThat(insight.getCodeSmells())
            .extracting(CodeSmell::getType)
            .contains(CodeSmell.SmellType.TOO_MANY_PARAMETERS);
    }

    @Test
    void testDetectArchitectureStyle() throws IOException {
        // 创建分层架构的项目结构
        createFile("com/example/controller/UserController.java", """
            package com.example.controller;
            public class UserController {}
            """);

        createFile("com/example/service/UserService.java", """
            package com.example.service;
            public class UserService {}
            """);

        createFile("com/example/repository/UserRepository.java", """
            package com.example.repository;
            public class UserRepository {}
            """);

        createFile("com/example/model/User.java", """
            package com.example.model;
            public class User {}
            """);

        Project project = createProjectFromFiles();

        CodeInsight insight = parser.parseProject(project);

        // 验证架构风格检测
        assertThat(insight.getStructure()).isNotNull();
        assertThat(insight.getStructure().getArchitectureStyle())
            .contains("分层架构");
    }

    private void createFile(String relativePath, String content) throws IOException {
        Path file = tempDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private Project createProjectFromFiles() throws IOException {
        Project project = Project.builder()
            .name("test-project")
            .rootPath(tempDir)
            .type(ProjectType.JAVA)
            .build();

        Files.walk(tempDir)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    SourceFile sourceFile = SourceFile.builder()
                        .path(path)
                        .relativePath(tempDir.relativize(path).toString())
                        .fileName(path.getFileName().toString())
                        .extension("java")
                        .content(content)
                        .lineCount((int) content.lines().count())
                        .build();
                    project.addSourceFile(sourceFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        return project;
    }
}

