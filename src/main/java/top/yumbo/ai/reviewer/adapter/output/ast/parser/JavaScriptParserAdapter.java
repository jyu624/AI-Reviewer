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
 * JavaScript/TypeScript解析器适配器
 * <p>
 * 支持ES6+语法，包括class、arrow function、async/await等
 * 注：这是一个简化实现，生产环境建议使用Babel Parser或ANTLR4
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
@SuppressWarnings("unused")
public class JavaScriptParserAdapter extends AbstractASTParser {

    // JavaScript/TypeScript语法正则表达式
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?(?:default\\s+)?class\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?(?:async\\s+)?function\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^\\s*(?:async\\s+)?(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
    @SuppressWarnings("unused")
    private static final Pattern ARROW_FUNCTION_PATTERN = Pattern.compile("(const|let|var)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?\\(([^)]*)\\)\\s*=>");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?interface\\s+(\\w+)");
    @SuppressWarnings("unused")
    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?type\\s+(\\w+)");
    private static final Pattern DECORATOR_PATTERN = Pattern.compile("^\\s*@(\\w+)");

    // 控制流关键字
    private static final Pattern IF_PATTERN = Pattern.compile("\\bif\\s*\\(");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bfor\\s*\\(");
    private static final Pattern WHILE_PATTERN = Pattern.compile("\\bwhile\\s*\\(");
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\bswitch\\s*\\(");
    private static final Pattern CASE_PATTERN = Pattern.compile("\\bcase\\s+");
    private static final Pattern CATCH_PATTERN = Pattern.compile("\\bcatch\\s*\\(");
    private static final Pattern TERNARY_PATTERN = Pattern.compile("\\?[^:]+:");

    @Override
    protected CodeInsight doParse(Project project) {
        validateProject(project);

        CodeInsight.CodeInsightBuilder builder = CodeInsight.builder()
            .projectName(project.getName());

        List<ClassStructure> classes = new ArrayList<>();
        List<InterfaceStructure> interfaces = new ArrayList<>();
        Map<String, Integer> packageClassCount = new HashMap<>();

        for (SourceFile sourceFile : project.getSourceFiles()) {
            if (!isJavaScriptFile(sourceFile)) {
                continue;
            }

            try {
                parseSourceFile(sourceFile, classes, interfaces, packageClassCount);
            } catch (Exception e) {
                log.warn("解析JavaScript文件失败: {} - {}", sourceFile.getRelativePath(), e.getMessage());
            }
        }

        builder.classes(classes);
        builder.interfaces(interfaces);

        // 构建项目结构
        ProjectStructure structure = buildProjectStructure(classes, packageClassCount);
        builder.structure(structure);

        // 构建依赖图
        DependencyGraph dependencyGraph = buildDependencyGraph(classes);
        builder.dependencyGraph(dependencyGraph);

        // 计算统计信息
        CodeStatistics statistics = calculateStatistics(project, classes, interfaces);
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
     * 解析JavaScript源文件
     */
    private void parseSourceFile(SourceFile sourceFile,
                                  List<ClassStructure> classes,
                                  List<InterfaceStructure> interfaces,
                                  Map<String, Integer> packageClassCount) throws IOException {

        List<String> lines = readFileLines(sourceFile);
        String packageName = extractPackageName(sourceFile);

        packageClassCount.merge(packageName, 0, Integer::sum);

        int i = 0;
        List<String> currentDecorators = new ArrayList<>();

        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 检查装饰器（TypeScript）
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
                if (classStructure != null) {
                    classes.add(classStructure);
                    packageClassCount.merge(packageName, 1, Integer::sum);
                }
                currentDecorators.clear();
            }

            // 检查接口定义（TypeScript）
            Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(trimmed);
            if (interfaceMatcher.find()) {
                InterfaceStructure interfaceStructure = parseInterface(lines, i, packageName);
                if (interfaceStructure != null) {
                    interfaces.add(interfaceStructure);
                }
            }

            // 检查独立函数
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(trimmed);
            if (functionMatcher.find() && !trimmed.contains("class")) {
                // 将独立函数作为一个"类"处理
                ClassStructure functionClass = parseFunctionAsClass(lines, i, packageName);
                if (functionClass != null) {
                    classes.add(functionClass);
                }
            }

            currentDecorators.clear();
            i++;
        }
    }

    /**
     * 解析JavaScript类
     */
    private ClassStructure parseClass(List<String> lines, int startLine, String packageName,
                                      List<String> decorators) {
        String line = lines.get(startLine).trim();
        Matcher matcher = CLASS_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String className = matcher.group(1);
        String superClass = matcher.group(2);

        ClassStructure.ClassStructureBuilder builder = ClassStructure.builder()
            .className(className)
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + className)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC)
            .annotations(new ArrayList<>(decorators));

        if (superClass != null) {
            builder.superClass(superClass);
        }

        ClassStructure classStructure = builder.build();

        // 解析类体
        int braceLevel = 0;
        int i = startLine;
        boolean foundOpenBrace = false;
        List<String> methodDecorators = new ArrayList<>();

        while (i < lines.size()) {
            String currentLine = lines.get(i);
            String trimmed = currentLine.trim();

            // 计算大括号层级
            for (char c : currentLine.toCharArray()) {
                if (c == '{') {
                    braceLevel++;
                    foundOpenBrace = true;
                } else if (c == '}') {
                    braceLevel--;
                }
            }

            // 类结束
            if (foundOpenBrace && braceLevel == 0) {
                break;
            }

            // 检查装饰器
            Matcher decoratorMatcher = DECORATOR_PATTERN.matcher(trimmed);
            if (decoratorMatcher.find()) {
                methodDecorators.add(decoratorMatcher.group(1));
                i++;
                continue;
            }

            // 检查方法定义
            Matcher methodMatcher = METHOD_PATTERN.matcher(trimmed);
            if (methodMatcher.find() && braceLevel > 0) {
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
        classStructure.setLinesOfCode(i - startLine + 1);

        return classStructure;
    }

    /**
     * 解析方法
     */
    private MethodInfo parseMethod(List<String> lines, int methodBodyStart, List<String> decorators) {
        String line = lines.get(methodBodyStart).trim();
        Matcher matcher = METHOD_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String methodName = matcher.group(1);
        String params = matcher.group(2);

        boolean isAsync = line.contains("async");
        boolean isConstructor = methodName.equals("constructor");

        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(methodName)
            .returnType(isAsync ? "Promise<any>" : "any")
            .annotations(new ArrayList<>(decorators))
            .isConstructor(isConstructor);

        // 解析参数
        List<MethodInfo.Parameter> parameters = parseParameters(params);
        builder.parameters(parameters);

        // 找到方法结束位置
        int braceLevel = 0;
        int i = methodBodyStart;

        while (i < lines.size()) {
            String currentLine = lines.get(i);

            for (char c : currentLine.toCharArray()) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
            }

            if (braceLevel == 0 && i > methodBodyStart) {
                break;
            }
            i++;
        }

        int linesOfCode = i - methodBodyStart + 1;
        builder.linesOfCode(linesOfCode);

        // 计算复杂度
        int complexity = calculateComplexity(lines, methodBodyStart, i + 1);
        builder.cyclomaticComplexity(complexity);

        MethodInfo methodInfo = builder.build();
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 解析接口（TypeScript）
     */
    private InterfaceStructure parseInterface(List<String> lines, int startLine, String packageName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = INTERFACE_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String interfaceName = matcher.group(1);

        return InterfaceStructure.builder()
            .interfaceName(interfaceName)
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + interfaceName)
            .build();
    }

    /**
     * 将独立函数解析为类
     */
    private ClassStructure parseFunctionAsClass(List<String> lines, int startLine, String packageName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = FUNCTION_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String functionName = matcher.group(1);

        ClassStructure classStructure = ClassStructure.builder()
            .className(functionName + "_Function")
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + functionName)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC)
            .build();

        MethodInfo methodInfo = parseMethod(lines, startLine, new ArrayList<>());
        if (methodInfo != null) {
            classStructure.addMethod(methodInfo);
        }

        return classStructure;
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
            if (param.isEmpty()) {
                continue;
            }

            // 处理TypeScript类型注解
            String name = param;
            String type = "any";

            if (param.contains(":")) {
                String[] parts = param.split(":");
                name = parts[0].trim();
                if (parts.length > 1) {
                    type = parts[1].split("=")[0].trim();
                }
            } else if (param.contains("=")) {
                name = param.split("=")[0].trim();
            }

            // 移除解构和剩余参数符号
            name = name.replace("...", "").replace("{", "").replace("}", "").trim();

            parameters.add(MethodInfo.Parameter.builder()
                .name(name)
                .type(type)
                .build());
        }

        return parameters;
    }

    /**
     * 计算JavaScript代码复杂度
     */
    private int calculateComplexity(List<String> lines, int start, int end) {
        int complexity = 1;

        for (int i = start; i < end && i < lines.size(); i++) {
            String line = lines.get(i);

            if (IF_PATTERN.matcher(line).find()) complexity++;
            if (FOR_PATTERN.matcher(line).find()) complexity++;
            if (WHILE_PATTERN.matcher(line).find()) complexity++;
            if (SWITCH_PATTERN.matcher(line).find()) complexity++;
            if (CASE_PATTERN.matcher(line).find()) complexity++;
            if (CATCH_PATTERN.matcher(line).find()) complexity++;
            if (TERNARY_PATTERN.matcher(line).find()) complexity++;
            if (line.contains("&&")) complexity++;
            if (line.contains("||")) complexity++;
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



    private boolean isJavaScriptFile(SourceFile file) {
        String ext = file.getExtension().toLowerCase();
        return "js".equals(ext) || "jsx".equals(ext) ||
               "ts".equals(ext) || "tsx".equals(ext);
    }

    @Override
    public boolean supports(String projectType) {
        return "JAVASCRIPT".equalsIgnoreCase(projectType) ||
               "TYPESCRIPT".equalsIgnoreCase(projectType) ||
               "NODE".equalsIgnoreCase(projectType) ||
               ProjectType.NODE.name().equalsIgnoreCase(projectType);
    }

    @Override
    public String getParserName() {
        return "JavaScriptParser";
    }
}
