package top.yumbo.ai.reviewer.adapter.output.ast.parser;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ProjectType;
import top.yumbo.ai.reviewer.domain.model.SourceFile;

import top.yumbo.ai.reviewer.domain.model.ast.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Python解析器适配器
 * <p>
 * 使用正则表达式和文本解析实现Python代码分析
 * 注：这是一个简化实现，对于生产环境建议使用ANTLR4或Jython
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
@SuppressWarnings("unused")
public class PythonParserAdapter extends AbstractASTParser {

    // Python语法正则表达式
    private static final Pattern CLASS_PATTERN = Pattern.compile("^class\\s+(\\w+)(?:\\(([^)]+)\\))?:");
    private static final Pattern DEF_PATTERN = Pattern.compile("^\\s*(def|async def)\\s+(\\w+)\\s*\\(([^)]*)\\):");
    private static final Pattern DECORATOR_PATTERN = Pattern.compile("^\\s*@(\\w+)");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^(?:from\\s+([\\w.]+)\\s+)?import\\s+(.+)");
    private static final Pattern IF_PATTERN = Pattern.compile("\\bif\\b");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bfor\\b");
    private static final Pattern WHILE_PATTERN = Pattern.compile("\\bwhile\\b");
    private static final Pattern ELIF_PATTERN = Pattern.compile("\\belif\\b");
    private static final Pattern EXCEPT_PATTERN = Pattern.compile("\\bexcept\\b");
    private static final Pattern WITH_PATTERN = Pattern.compile("\\bwith\\b");

    @Override
    protected CodeInsight doParse(Project project) {
        validateProject(project);

        CodeInsight.CodeInsightBuilder builder = CodeInsight.builder()
            .projectName(project.getName());

        List<ClassStructure> classes = new ArrayList<>();
        Map<String, Integer> packageClassCount = new HashMap<>();

        for (SourceFile sourceFile : project.getSourceFiles()) {
            if (!isPythonFile(sourceFile)) {
                continue;
            }

            try {
                parseSourceFile(sourceFile, classes, packageClassCount);
            } catch (Exception e) {
                log.warn("解析Python文件失败: {} - {}", sourceFile.getRelativePath(), e.getMessage());
            }
        }

        builder.classes(classes);

        // 构建项目结构
        ProjectStructure structure = buildProjectStructure(classes, packageClassCount);
        builder.structure(structure);

        // 构建依赖图
        DependencyGraph dependencyGraph = buildDependencyGraph(classes);
        builder.dependencyGraph(dependencyGraph);

        // 计算统计信息（Python 没有接口概念，传 null）
        CodeStatistics statistics = calculateStatistics(project, classes, null);
        builder.statistics(statistics);

        CodeInsight insight = builder.build();

        // 检测设计模式
        DesignPatterns patterns = detectDesignPatterns(classes);
        insight.setDesignPatterns(patterns);

        // 计算复杂度指标
        ComplexityMetrics metrics = calculateComplexityMetrics(classes);
        insight.setComplexityMetrics(metrics);

        // 检测代码坏味道
        detectCodeSmells(insight);

        return insight;
    }

    /**
     * 解析Python源文件
     */
    private void parseSourceFile(SourceFile sourceFile,
                                  List<ClassStructure> classes,
                                  Map<String, Integer> packageClassCount) throws IOException {

        List<String> lines = readFileLines(sourceFile);
        String packageName = extractPackageName(sourceFile);

        packageClassCount.merge(packageName, 0, Integer::sum);

        int i = 0;
        List<String> currentDecorators = new ArrayList<>();

        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 检查装饰器
            Matcher decoratorMatcher = DECORATOR_PATTERN.matcher(trimmed);
            if (decoratorMatcher.find()) {
                currentDecorators.add(decoratorMatcher.group(1));
                i++;
                continue;
            }

            // 检查类定义
            Matcher classMatcher = CLASS_PATTERN.matcher(trimmed);
            if (classMatcher.find()) {
                ClassStructure classStructure = parseClass(lines, i, packageName, currentDecorators);
                classes.add(classStructure);
                packageClassCount.merge(packageName, 1, Integer::sum);
                currentDecorators.clear();
            }

            currentDecorators.clear();
            i++;
        }
    }

    /**
     * 解析Python类
     */
    private ClassStructure parseClass(List<String> lines, int startLine, String packageName,
                                      List<String> decorators) {
        String line = lines.get(startLine).trim();
        Matcher matcher = CLASS_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String className = matcher.group(1);
        String baseClasses = matcher.group(2);

        ClassStructure.ClassStructureBuilder builder = ClassStructure.builder()
            .className(className)
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + className)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC)
            .annotations(new ArrayList<>(decorators));

        // 解析基类
        if (baseClasses != null && !baseClasses.trim().isEmpty()) {
            String[] bases = baseClasses.split(",");
            if (bases.length > 0) {
                builder.superClass(bases[0].trim());
            }
        }

        ClassStructure classStructure = builder.build();

        // 解析类体
        int indentLevel = getIndentLevel(lines.get(startLine));
        int i = startLine + 1;
        List<String> methodDecorators = new ArrayList<>();

        while (i < lines.size()) {
            String currentLine = lines.get(i);
            int currentIndent = getIndentLevel(currentLine);

            // 如果缩进回到类级别，结束类解析
            if (currentIndent <= indentLevel && !currentLine.trim().isEmpty()) {
                break;
            }

            String trimmed = currentLine.trim();

            // 检查装饰器
            Matcher decoratorMatcher = DECORATOR_PATTERN.matcher(trimmed);
            if (decoratorMatcher.find()) {
                methodDecorators.add(decoratorMatcher.group(1));
                i++;
                continue;
            }

            // 检查方法定义
            Matcher defMatcher = DEF_PATTERN.matcher(trimmed);
            if (defMatcher.find()) {
                MethodInfo methodInfo = parseMethod(lines, i, methodDecorators);
                if (methodInfo != null) {
                    classStructure.addMethod(methodInfo);
                }
                methodDecorators.clear();
            }

            methodDecorators.clear();
            i++;
        }

        // 计算代码行数
        int endLine = i;
        classStructure.setLinesOfCode(endLine - startLine);

        return classStructure;
    }

    /**
     * 解析Python方法
     */
    private MethodInfo parseMethod(List<String> lines, int startLine, List<String> decorators) {
        String line = lines.get(startLine).trim();
        Matcher matcher = DEF_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String methodName = matcher.group(2);
        String params = matcher.group(3);

        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(methodName)
            .returnType("Any")  // Python是动态类型
            .isStatic(decorators.contains("staticmethod"))
            .annotations(new ArrayList<>(decorators))
            .isConstructor(methodName.equals("__init__"));

        // 解析参数
        List<MethodInfo.Parameter> parameters = parseParameters(params);
        builder.parameters(parameters);

        // 计算方法体信息
        int indentLevel = getIndentLevel(lines.get(startLine));
        int methodBodyStart = startLine + 1;
        int methodBodyEnd = methodBodyStart;

        while (methodBodyEnd < lines.size()) {
            String currentLine = lines.get(methodBodyEnd);
            int currentIndent = getIndentLevel(currentLine);

            if (currentIndent <= indentLevel && !currentLine.trim().isEmpty()) {
                break;
            }
            methodBodyEnd++;
        }

        int linesOfCode = methodBodyEnd - startLine;
        builder.linesOfCode(linesOfCode);

        // 计算复杂度
        int complexity = calculateComplexity(lines, methodBodyStart, methodBodyEnd);
        builder.cyclomaticComplexity(complexity);

        MethodInfo methodInfo = builder.build();
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 解析参数列表
     */
    private List<MethodInfo.Parameter> parseParameters(String paramsStr) {
        List<MethodInfo.Parameter> parameters = new ArrayList<>();

        if (paramsStr == null || paramsStr.trim().isEmpty()) {
            return parameters;
        }

        String[] params = paramsStr.split(",");
        for (String param : params) {
            param = param.trim();
            if (param.isEmpty() || param.equals("self") || param.equals("cls")) {
                continue;
            }

            // 处理类型注解和默认值
            String name = param.split("[=:]")[0].trim();
            String type = "Any";

            if (param.contains(":")) {
                String[] parts = param.split(":");
                if (parts.length > 1) {
                    type = parts[1].split("=")[0].trim();
                }
            }

            parameters.add(MethodInfo.Parameter.builder()
                .name(name)
                .type(type)
                .build());
        }

        return parameters;
    }

    /**
     * 计算Python代码复杂度
     */
    private int calculateComplexity(List<String> lines, int start, int end) {
        int complexity = 1;

        for (int i = start; i < end && i < lines.size(); i++) {
            String line = lines.get(i).toLowerCase();

            if (IF_PATTERN.matcher(line).find()) complexity++;
            if (ELIF_PATTERN.matcher(line).find()) complexity++;
            if (FOR_PATTERN.matcher(line).find()) complexity++;
            if (WHILE_PATTERN.matcher(line).find()) complexity++;
            if (EXCEPT_PATTERN.matcher(line).find()) complexity++;
            if (WITH_PATTERN.matcher(line).find()) complexity++;
            if (line.contains(" and ")) complexity++;
            if (line.contains(" or ")) complexity++;
        }

        return complexity;
    }

    /**
     * 读取文件所有行
     */
    private List<String> readFileLines(SourceFile sourceFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile.getPath().toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 提取包名（基于文件路径）
     */
    private String extractPackageName(SourceFile sourceFile) {
        String relativePath = sourceFile.getRelativePath();
        if (relativePath.contains("/")) {
            String dir = relativePath.substring(0, relativePath.lastIndexOf("/"));
            return dir.replace("/", ".");
        }
        return "default";
    }

    /**
     * 获取行的缩进级别
     */
    private int getIndentLevel(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') spaces++;
            else if (c == '\t') spaces += 4;
            else break;
        }
        return spaces / 4;
    }

    /**
     * Python特定的单例模式检测（覆盖基类方法）
     * Python使用 __new__ 方法实现单例
     */
    @Override
    protected void detectSingletonPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern singletonPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.SINGLETON)
            .name("单例模式")
            .build();

        for (ClassStructure cls : classes) {
            boolean hasNewMethod = cls.getMethods().stream()
                .anyMatch(m -> m.getMethodName().equals("__new__"));

            if (hasNewMethod) {
                singletonPattern.addInstance(cls.getClassName());
            }
        }

        if (singletonPattern.getInstanceCount() > 0) {
            singletonPattern.setConfidence(0.7);
            patterns.addPattern(singletonPattern);
        }
    }


    private boolean isPythonFile(SourceFile file) {
        return "py".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public boolean supports(String projectType) {
        return "PYTHON".equalsIgnoreCase(projectType) ||
               ProjectType.PYTHON.name().equalsIgnoreCase(projectType);
    }

    @Override
    public String getParserName() {
        return "PythonParser";
    }
}
