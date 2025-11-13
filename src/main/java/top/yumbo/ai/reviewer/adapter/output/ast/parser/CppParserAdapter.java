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
 * C/C++解析器适配器
 *
 * 支持C/C++的类、函数、方法等解析
 * 注：这是一个简化实现，生产环境建议使用libclang或ANTLR4
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
public class CppParserAdapter extends AbstractASTParser {

    // C++语法正则表达式
    private static final Pattern CLASS_PATTERN = Pattern.compile("^(?:class|struct)\\s+(\\w+)(?:\\s*:\\s*(?:public|private|protected)\\s+(\\w+))?\\s*\\{");
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^namespace\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^\\s*(?:virtual\\s+)?(?:static\\s+)?([\\w:<>]+)\\s+(\\w+)\\s*\\(([^)]*)\\)(?:\\s*const)?(?:\\s*override)?\\s*[{;]");
    private static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*\\(([^)]*)\\)(?:\\s*:\\s*[^{]+)?\\s*\\{");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^\\s*(?:static\\s+)?(?:const\\s+)?([\\w:<>]+)(?:\\s*[*&])?\\s+(\\w+)\\s*[=;]");

    // 访问修饰符
    private static final Pattern ACCESS_MODIFIER_PATTERN = Pattern.compile("^\\s*(public|private|protected)\\s*:");

    // 控制流关键字
    private static final Pattern IF_PATTERN = Pattern.compile("\\bif\\s*\\(");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bfor\\s*\\(");
    private static final Pattern WHILE_PATTERN = Pattern.compile("\\bwhile\\s*\\(");
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\bswitch\\s*\\(");
    private static final Pattern CASE_PATTERN = Pattern.compile("\\bcase\\s+");
    private static final Pattern CATCH_PATTERN = Pattern.compile("\\bcatch\\s*\\(");

    @Override
    protected CodeInsight doParse(Project project) {
        validateProject(project);

        CodeInsight.CodeInsightBuilder builder = CodeInsight.builder()
            .projectName(project.getName());

        List<ClassStructure> classes = new ArrayList<>();
        Map<String, Integer> packageClassCount = new HashMap<>();

        for (SourceFile sourceFile : project.getSourceFiles()) {
            if (!isCppFile(sourceFile)) {
                continue;
            }

            try {
                parseSourceFile(sourceFile, classes, packageClassCount);
            } catch (Exception e) {
                log.warn("解析C++文件失败: {} - {}", sourceFile.getRelativePath(), e.getMessage());
            }
        }

        builder.classes(classes);

        // 构建项目结构
        ProjectStructure structure = buildProjectStructure(classes, packageClassCount);
        builder.structure(structure);

        // 构建依赖图
        DependencyGraph dependencyGraph = buildDependencyGraph(classes);
        builder.dependencyGraph(dependencyGraph);

        // 计算统计信息
        CodeStatistics statistics = calculateStatistics(project, classes);
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
     * 解析C++源文件
     */
    private void parseSourceFile(SourceFile sourceFile,
                                  List<ClassStructure> classes,
                                  Map<String, Integer> packageClassCount) throws IOException {

        List<String> lines = readFileLines(sourceFile);
        String namespaceName = extractNamespace(lines);

        packageClassCount.merge(namespaceName, 0, Integer::sum);

        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 跳过注释和预处理指令
            if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*")) {
                i++;
                continue;
            }

            // 检查类定义
            Matcher classMatcher = CLASS_PATTERN.matcher(trimmed);
            if (classMatcher.find()) {
                ClassStructure classStructure = parseClass(lines, i, namespaceName);
                if (classStructure != null) {
                    classes.add(classStructure);
                    packageClassCount.merge(namespaceName, 1, Integer::sum);
                }
            }

            i++;
        }
    }

    /**
     * 解析C++类
     */
    private ClassStructure parseClass(List<String> lines, int startLine, String namespaceName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = CLASS_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String className = matcher.group(1);
        String baseClass = matcher.group(2);

        ClassStructure.ClassStructureBuilder builder = ClassStructure.builder()
            .className(className)
            .packageName(namespaceName)
            .fullQualifiedName(namespaceName + "::" + className)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC);

        if (baseClass != null) {
            builder.superClass(baseClass);
        }

        ClassStructure classStructure = builder.build();

        // 解析类体
        int braceLevel = 0;
        int i = startLine;
        boolean foundOpenBrace = false;
        ClassStructure.AccessModifier currentAccessModifier = ClassStructure.AccessModifier.PRIVATE; // C++默认private

        while (i < lines.size()) {
            String currentLine = lines.get(i);
            String trimmed = currentLine.trim();

            // 跳过注释
            if (trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                i++;
                continue;
            }

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

            // 检查访问修饰符
            Matcher accessMatcher = ACCESS_MODIFIER_PATTERN.matcher(trimmed);
            if (accessMatcher.find()) {
                String modifier = accessMatcher.group(1);
                currentAccessModifier = switch (modifier) {
                    case "public" -> ClassStructure.AccessModifier.PUBLIC;
                    case "private" -> ClassStructure.AccessModifier.PRIVATE;
                    case "protected" -> ClassStructure.AccessModifier.PROTECTED;
                    default -> currentAccessModifier;
                };
                i++;
                continue;
            }

            // 检查构造函数
            Matcher constructorMatcher = CONSTRUCTOR_PATTERN.matcher(trimmed);
            if (constructorMatcher.find() && constructorMatcher.group(1).equals(className)) {
                MethodInfo methodInfo = parseConstructor(lines, i, currentAccessModifier);
                if (methodInfo != null) {
                    classStructure.addMethod(methodInfo);
                }
                i++;
                continue;
            }

            // 检查方法定义
            Matcher methodMatcher = METHOD_PATTERN.matcher(trimmed);
            if (methodMatcher.find() && braceLevel > 0) {
                MethodInfo methodInfo = parseMethod(lines, i, currentAccessModifier);
                if (methodInfo != null) {
                    classStructure.addMethod(methodInfo);
                }
            }

            // 检查字段定义
            Matcher fieldMatcher = FIELD_PATTERN.matcher(trimmed);
            if (fieldMatcher.find() && braceLevel > 0 && !trimmed.contains("(")) {
                FieldInfo fieldInfo = parseField(trimmed, currentAccessModifier);
                if (fieldInfo != null) {
                    classStructure.addField(fieldInfo);
                }
            }

            i++;
        }

        // 计算代码行数
        classStructure.setLinesOfCode(i - startLine + 1);

        return classStructure;
    }

    /**
     * 解析构造函数
     */
    private MethodInfo parseConstructor(List<String> lines, int startLine,
                                       ClassStructure.AccessModifier accessModifier) {
        String line = lines.get(startLine).trim();
        Matcher matcher = CONSTRUCTOR_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String constructorName = matcher.group(1);
        String paramsStr = matcher.group(2);

        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(constructorName)
            .returnType("void")
            .isConstructor(true)
            .accessModifier(accessModifier);

        // 解析参数
        List<MethodInfo.Parameter> parameters = parseParameters(paramsStr);
        builder.parameters(parameters);

        // 找到方法结束位置
        int braceLevel = 0;
        int i = startLine;

        while (i < lines.size()) {
            String currentLine = lines.get(i);

            for (char c : currentLine.toCharArray()) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
            }

            if (braceLevel == 0 && i > startLine) {
                break;
            }
            i++;
        }

        int linesOfCode = i - startLine + 1;
        builder.linesOfCode(linesOfCode);

        // 计算复杂度
        int complexity = calculateComplexity(lines, startLine, i + 1);
        builder.cyclomaticComplexity(complexity);

        MethodInfo methodInfo = builder.build();
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 解析方法
     */
    private MethodInfo parseMethod(List<String> lines, int startLine,
                                   ClassStructure.AccessModifier accessModifier) {
        String line = lines.get(startLine).trim();
        Matcher matcher = METHOD_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String returnType = matcher.group(1);
        String methodName = matcher.group(2);
        String paramsStr = matcher.group(3);

        boolean isStatic = line.contains("static");
        boolean isVirtual = line.contains("virtual");

        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(methodName)
            .returnType(returnType)
            .isStatic(isStatic)
            .accessModifier(accessModifier);

        // 解析参数
        List<MethodInfo.Parameter> parameters = parseParameters(paramsStr);
        builder.parameters(parameters);

        // 检查是否只是声明（以分号结尾）
        if (line.trim().endsWith(";")) {
            builder.linesOfCode(1);
            builder.cyclomaticComplexity(1);
            return builder.build();
        }

        // 找到方法结束位置
        int braceLevel = 0;
        int i = startLine;
        boolean foundOpenBrace = false;

        while (i < lines.size()) {
            String currentLine = lines.get(i);

            for (char c : currentLine.toCharArray()) {
                if (c == '{') {
                    braceLevel++;
                    foundOpenBrace = true;
                } else if (c == '}') {
                    braceLevel--;
                }
            }

            if (foundOpenBrace && braceLevel == 0) {
                break;
            }
            i++;
        }

        int linesOfCode = i - startLine + 1;
        builder.linesOfCode(linesOfCode);

        // 计算复杂度
        int complexity = calculateComplexity(lines, startLine, i + 1);
        builder.cyclomaticComplexity(complexity);

        MethodInfo methodInfo = builder.build();
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 解析字段
     */
    private FieldInfo parseField(String line, ClassStructure.AccessModifier accessModifier) {
        Matcher matcher = FIELD_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String type = matcher.group(1);
        String name = matcher.group(2);

        boolean isStatic = line.contains("static");
        boolean isConst = line.contains("const");

        return FieldInfo.builder()
            .fieldName(name)
            .type(type)
            .isStatic(isStatic)
            .isFinal(isConst)
            .accessModifier(accessModifier)
            .build();
    }

    /**
     * 解析参数列表
     */
    private List<MethodInfo.Parameter> parseParameters(String paramsStr) {
        List<MethodInfo.Parameter> parameters = new ArrayList<>();

        if (paramsStr == null || paramsStr.trim().isEmpty()) {
            return parameters;
        }

        // C++参数格式: type name, const type& name, type* name
        String[] params = paramsStr.split(",");
        for (String param : params) {
            param = param.trim();
            if (param.isEmpty()) {
                continue;
            }

            // 移除默认值
            if (param.contains("=")) {
                param = param.substring(0, param.indexOf("=")).trim();
            }

            // 简化处理：最后一个词是参数名
            String[] parts = param.trim().split("\\s+");
            if (parts.length >= 2) {
                String type = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
                String name = parts[parts.length - 1];
                // 移除指针和引用符号
                name = name.replace("*", "").replace("&", "").replace("[", "").replace("]", "");

                parameters.add(MethodInfo.Parameter.builder()
                    .name(name)
                    .type(type)
                    .build());
            } else if (parts.length == 1) {
                // 只有类型
                parameters.add(MethodInfo.Parameter.builder()
                    .name("arg" + parameters.size())
                    .type(parts[0])
                    .build());
            }
        }

        return parameters;
    }

    /**
     * 计算C++代码复杂度
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
            if (line.contains("&&")) complexity++;
            if (line.contains("||")) complexity++;
            if (line.contains("?") && line.contains(":")) complexity++; // 三元运算符
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
     * 提取命名空间
     */
    private String extractNamespace(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = NAMESPACE_PATTERN.matcher(line.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "global";
    }

    private ProjectStructure buildProjectStructure(List<ClassStructure> classes,
                                                   Map<String, Integer> packageClassCount) {
        if (classes.isEmpty()) {
            return ProjectStructure.builder().build();
        }

        String rootPackage = classes.get(0).getPackageName();

        ProjectStructure.ProjectStructureBuilder builder = ProjectStructure.builder()
            .rootPackage(rootPackage);

        Map<String, ProjectStructure.PackageInfo> packages = new HashMap<>();
        packageClassCount.forEach((pkg, count) -> {
            ProjectStructure.PackageInfo packageInfo = ProjectStructure.PackageInfo.builder()
                .name(pkg)
                .classCount(count)
                .build();
            packages.put(pkg, packageInfo);
        });

        builder.packages(packages);

        ProjectStructure structure = builder.build();
        structure.detectArchitectureStyle();

        return structure;
    }

    private DependencyGraph buildDependencyGraph(List<ClassStructure> classes) {
        DependencyGraph graph = DependencyGraph.builder().build();

        for (ClassStructure cls : classes) {
            String className = cls.getFullQualifiedName();

            if (cls.getSuperClass() != null) {
                graph.addDependency(className, cls.getSuperClass());
            }

            cls.getFields().forEach(field -> {
                graph.addDependency(className, field.getType());
            });
        }

        return graph;
    }

    private CodeStatistics calculateStatistics(Project project, List<ClassStructure> classes) {
        int totalMethods = classes.stream()
            .mapToInt(ClassStructure::getMethodCount)
            .sum();

        int totalFields = classes.stream()
            .mapToInt(ClassStructure::getFieldCount)
            .sum();

        return CodeStatistics.builder()
            .totalFiles(project.getSourceFiles().size())
            .totalClasses(classes.size())
            .totalMethods(totalMethods)
            .totalFields(totalFields)
            .totalLines(project.getTotalLines())
            .build();
    }

    private DesignPatterns detectDesignPatterns(List<ClassStructure> classes) {
        DesignPatterns patterns = DesignPatterns.builder().build();

        // 检测单例模式（getInstance方法 + private构造函数）
        detectSingletonPattern(classes, patterns);

        // 检测工厂模式（create方法）
        detectFactoryPattern(classes, patterns);

        return patterns;
    }

    private void detectSingletonPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern singletonPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.SINGLETON)
            .name("单例模式")
            .build();

        for (ClassStructure cls : classes) {
            boolean hasPrivateConstructor = cls.getMethods().stream()
                .anyMatch(m -> m.isConstructor() &&
                              m.getAccessModifier() == ClassStructure.AccessModifier.PRIVATE);

            boolean hasGetInstance = cls.getMethods().stream()
                .anyMatch(m -> m.getMethodName().toLowerCase().contains("getinstance") ||
                              m.getMethodName().toLowerCase().contains("instance"));

            if (hasPrivateConstructor && hasGetInstance) {
                singletonPattern.addInstance(cls.getClassName());
            }
        }

        if (singletonPattern.getInstanceCount() > 0) {
            singletonPattern.setConfidence(0.85);
            patterns.addPattern(singletonPattern);
        }
    }

    private void detectFactoryPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern factoryPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.FACTORY)
            .name("工厂模式")
            .build();

        for (ClassStructure cls : classes) {
            if (cls.getClassName().contains("Factory") || cls.getClassName().contains("Creator")) {
                factoryPattern.addInstance(cls.getClassName());
            }
        }

        if (factoryPattern.getInstanceCount() > 0) {
            factoryPattern.setConfidence(0.75);
            patterns.addPattern(factoryPattern);
        }
    }

    private ComplexityMetrics calculateComplexityMetrics(List<ClassStructure> classes) {
        if (classes.isEmpty()) {
            return ComplexityMetrics.builder().build();
        }

        List<MethodInfo> allMethods = classes.stream()
            .flatMap(cls -> cls.getMethods().stream())
            .toList();

        if (allMethods.isEmpty()) {
            return ComplexityMetrics.builder().build();
        }

        double avgComplexity = allMethods.stream()
            .mapToInt(MethodInfo::getCyclomaticComplexity)
            .average()
            .orElse(0.0);

        MethodInfo mostComplex = allMethods.stream()
            .max(Comparator.comparingInt(MethodInfo::getCyclomaticComplexity))
            .orElse(null);

        long highComplexityCount = allMethods.stream()
            .filter(MethodInfo::isComplexMethod)
            .count();

        double avgLength = allMethods.stream()
            .mapToInt(MethodInfo::getLinesOfCode)
            .average()
            .orElse(0.0);

        long longMethodCount = allMethods.stream()
            .filter(MethodInfo::isLongMethod)
            .count();

        long tooManyParamsCount = allMethods.stream()
            .filter(MethodInfo::hasTooManyParameters)
            .count();

        return ComplexityMetrics.builder()
            .avgCyclomaticComplexity(avgComplexity)
            .maxCyclomaticComplexity(mostComplex != null ? mostComplex.getCyclomaticComplexity() : 0)
            .mostComplexMethod(mostComplex != null ? mostComplex.getMethodName() : null)
            .highComplexityMethodCount((int) highComplexityCount)
            .avgMethodLength(avgLength)
            .longMethodCount((int) longMethodCount)
            .tooManyParametersCount((int) tooManyParamsCount)
            .totalMethods(allMethods.size())
            .totalClasses(classes.size())
            .build();
    }

    private void detectCodeSmells(CodeInsight insight) {
        insight.getClasses().forEach(cls -> {
            cls.getMethods().forEach(method -> {
                method.getSmells().forEach(insight::addCodeSmell);
            });

            if (cls.getMethodCount() > 20 || cls.getFieldCount() > 15) {
                insight.addCodeSmell(CodeSmell.builder()
                    .type(CodeSmell.SmellType.GOD_CLASS)
                    .severity(CodeSmell.Severity.HIGH)
                    .location(cls.getClassName())
                    .message(String.format("类过大(方法:%d, 字段:%d)，建议拆分",
                        cls.getMethodCount(), cls.getFieldCount()))
                    .build());
            }
        });
    }

    private boolean isCppFile(SourceFile file) {
        String ext = file.getExtension().toLowerCase();
        return "cpp".equals(ext) || "cc".equals(ext) || "cxx".equals(ext) ||
               "c".equals(ext) || "h".equals(ext) || "hpp".equals(ext) || "hxx".equals(ext);
    }

    @Override
    public boolean supports(String projectType) {
        return "CPP".equalsIgnoreCase(projectType) ||
               "C++".equalsIgnoreCase(projectType) ||
               "C".equalsIgnoreCase(projectType);
    }

    @Override
    public String getParserName() {
        return "CppParser";
    }
}

