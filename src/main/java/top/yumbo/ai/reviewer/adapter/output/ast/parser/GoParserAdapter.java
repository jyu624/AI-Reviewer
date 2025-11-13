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
 * Go语言解析器适配器
 *
 * 支持Go语言的类型(struct)、函数、方法等解析
 * 注：这是一个简化实现，生产环境建议使用go/parser或ANTLR4
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
public class GoParserAdapter extends AbstractASTParser {

    // Go语法正则表达式
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^package\\s+(\\w+)");
    private static final Pattern STRUCT_PATTERN = Pattern.compile("^type\\s+(\\w+)\\s+struct\\s*\\{");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("^type\\s+(\\w+)\\s+interface\\s*\\{");
    private static final Pattern FUNC_PATTERN = Pattern.compile("^func\\s+(?:\\(\\w+\\s+\\*?(\\w+)\\)\\s+)?(\\w+)\\s*\\(([^)]*)\\)(?:\\s*([^{]+))?\\s*\\{");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^func\\s+\\((\\w+)\\s+\\*?(\\w+)\\)\\s+(\\w+)\\s*\\(([^)]*)\\)(?:\\s*([^{]+))?\\s*\\{");

    // 控制流关键字
    private static final Pattern IF_PATTERN = Pattern.compile("\\bif\\b");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bfor\\b");
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\bswitch\\b");
    private static final Pattern CASE_PATTERN = Pattern.compile("\\bcase\\b");
    private static final Pattern SELECT_PATTERN = Pattern.compile("\\bselect\\b");

    @Override
    protected CodeInsight doParse(Project project) {
        validateProject(project);

        CodeInsight.CodeInsightBuilder builder = CodeInsight.builder()
            .projectName(project.getName());

        List<ClassStructure> classes = new ArrayList<>();
        List<InterfaceStructure> interfaces = new ArrayList<>();
        Map<String, Integer> packageClassCount = new HashMap<>();

        for (SourceFile sourceFile : project.getSourceFiles()) {
            if (!isGoFile(sourceFile)) {
                continue;
            }

            try {
                parseSourceFile(sourceFile, classes, interfaces, packageClassCount);
            } catch (Exception e) {
                log.warn("解析Go文件失败: {} - {}", sourceFile.getRelativePath(), e.getMessage());
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
     * 解析Go源文件
     */
    private void parseSourceFile(SourceFile sourceFile,
                                  List<ClassStructure> classes,
                                  List<InterfaceStructure> interfaces,
                                  Map<String, Integer> packageClassCount) throws IOException {

        List<String> lines = readFileLines(sourceFile);
        String packageName = extractPackageName(lines);

        packageClassCount.merge(packageName, 0, Integer::sum);

        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 检查struct定义
            Matcher structMatcher = STRUCT_PATTERN.matcher(trimmed);
            if (structMatcher.find()) {
                ClassStructure classStructure = parseStruct(lines, i, packageName);
                if (classStructure != null) {
                    classes.add(classStructure);
                    packageClassCount.merge(packageName, 1, Integer::sum);
                }
            }

            // 检查interface定义
            Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(trimmed);
            if (interfaceMatcher.find()) {
                InterfaceStructure interfaceStructure = parseInterface(lines, i, packageName);
                if (interfaceStructure != null) {
                    interfaces.add(interfaceStructure);
                }
            }

            // 检查独立函数
            Matcher funcMatcher = FUNC_PATTERN.matcher(trimmed);
            if (funcMatcher.find() && !trimmed.contains("(")) {
                // 将独立函数作为一个"类"处理
                ClassStructure functionClass = parseFunctionAsClass(lines, i, packageName);
                if (functionClass != null) {
                    classes.add(functionClass);
                }
            }

            i++;
        }
    }

    /**
     * 解析Go struct（类似类）
     */
    private ClassStructure parseStruct(List<String> lines, int startLine, String packageName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = STRUCT_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String structName = matcher.group(1);

        ClassStructure.ClassStructureBuilder builder = ClassStructure.builder()
            .className(structName)
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + structName)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC);

        ClassStructure classStructure = builder.build();

        // 解析struct字段
        int braceLevel = 0;
        int i = startLine;
        boolean foundOpenBrace = false;

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

            // struct结束
            if (foundOpenBrace && braceLevel == 0) {
                break;
            }

            // 解析字段（简化：字段名 类型）
            if (foundOpenBrace && braceLevel == 1 && !trimmed.isEmpty() && !trimmed.contains("type")) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 2) {
                    FieldInfo fieldInfo = FieldInfo.builder()
                        .fieldName(parts[0])
                        .type(parts[1])
                        .accessModifier(Character.isUpperCase(parts[0].charAt(0)) ?
                            ClassStructure.AccessModifier.PUBLIC :
                            ClassStructure.AccessModifier.PRIVATE)
                        .build();
                    classStructure.addField(fieldInfo);
                }
            }

            i++;
        }

        // 查找该struct的方法
        findMethodsForStruct(lines, structName, classStructure);

        // 计算代码行数
        classStructure.setLinesOfCode(i - startLine + 1);

        return classStructure;
    }

    /**
     * 查找struct的方法
     */
    private void findMethodsForStruct(List<String> lines, String structName, ClassStructure classStructure) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            Matcher methodMatcher = METHOD_PATTERN.matcher(line);

            if (methodMatcher.find()) {
                String receiverType = methodMatcher.group(2);
                // 检查方法是否属于该struct
                if (receiverType.equals(structName)) {
                    MethodInfo methodInfo = parseMethod(lines, i);
                    if (methodInfo != null) {
                        classStructure.addMethod(methodInfo);
                    }
                }
            }
        }
    }

    /**
     * 解析Go接口
     */
    private InterfaceStructure parseInterface(List<String> lines, int startLine, String packageName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = INTERFACE_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String interfaceName = matcher.group(1);

        InterfaceStructure.InterfaceStructureBuilder builder = InterfaceStructure.builder()
            .interfaceName(interfaceName)
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + interfaceName);

        InterfaceStructure interfaceStructure = builder.build();

        // 解析接口方法
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

            // 解析接口方法签名（简化）
            String trimmed = currentLine.trim();
            if (foundOpenBrace && braceLevel == 1 && !trimmed.isEmpty() && trimmed.contains("(")) {
                Pattern methodSigPattern = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)");
                Matcher sigMatcher = methodSigPattern.matcher(trimmed);
                if (sigMatcher.find()) {
                    String methodName = sigMatcher.group(1);
                    MethodInfo methodInfo = MethodInfo.builder()
                        .methodName(methodName)
                        .returnType("interface")
                        .build();
                    interfaceStructure.addMethod(methodInfo);
                }
            }

            i++;
        }

        return interfaceStructure;
    }

    /**
     * 解析方法
     */
    private MethodInfo parseMethod(List<String> lines, int startLine) {
        String line = lines.get(startLine).trim();
        Matcher methodMatcher = METHOD_PATTERN.matcher(line);
        Matcher funcMatcher = FUNC_PATTERN.matcher(line);

        String methodName = null;
        String paramsStr = null;
        String returnType = "void";

        if (methodMatcher.find()) {
            methodName = methodMatcher.group(3);
            paramsStr = methodMatcher.group(4);
            returnType = methodMatcher.group(5) != null ? methodMatcher.group(5).trim() : "void";
        } else if (funcMatcher.find()) {
            methodName = funcMatcher.group(2);
            paramsStr = funcMatcher.group(3);
            returnType = funcMatcher.group(4) != null ? funcMatcher.group(4).trim() : "void";
        }

        if (methodName == null) {
            return null;
        }

        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(methodName)
            .returnType(returnType)
            .accessModifier(Character.isUpperCase(methodName.charAt(0)) ?
                ClassStructure.AccessModifier.PUBLIC :
                ClassStructure.AccessModifier.PRIVATE);

        // 解析参数
        List<MethodInfo.Parameter> parameters = parseParameters(paramsStr);
        builder.parameters(parameters);

        // 找到方法结束位置
        int braceLevel = 0;
        int i = startLine;
        int methodBodyStart = startLine;

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
        int complexity = calculateComplexity(lines, methodBodyStart, i + 1);
        builder.cyclomaticComplexity(complexity);

        MethodInfo methodInfo = builder.build();
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 将独立函数解析为类
     */
    private ClassStructure parseFunctionAsClass(List<String> lines, int startLine, String packageName) {
        String line = lines.get(startLine).trim();
        Matcher matcher = FUNC_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String functionName = matcher.group(2);

        ClassStructure classStructure = ClassStructure.builder()
            .className(functionName + "_Function")
            .packageName(packageName)
            .fullQualifiedName(packageName + "." + functionName)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC)
            .build();

        MethodInfo methodInfo = parseMethod(lines, startLine);
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

        // Go参数格式: name type 或 name1, name2 type
        String[] params = paramsStr.split(",");
        for (String param : params) {
            param = param.trim();
            if (param.isEmpty()) {
                continue;
            }

            String[] parts = param.trim().split("\\s+");
            if (parts.length >= 2) {
                String name = parts[0];
                String type = parts[parts.length - 1];

                parameters.add(MethodInfo.Parameter.builder()
                    .name(name)
                    .type(type)
                    .build());
            } else if (parts.length == 1) {
                // 可能只有类型
                parameters.add(MethodInfo.Parameter.builder()
                    .name("arg" + parameters.size())
                    .type(parts[0])
                    .build());
            }
        }

        return parameters;
    }

    /**
     * 计算Go代码复杂度
     */
    private int calculateComplexity(List<String> lines, int start, int end) {
        int complexity = 1;

        for (int i = start; i < end && i < lines.size(); i++) {
            String line = lines.get(i);

            if (IF_PATTERN.matcher(line).find()) complexity++;
            if (FOR_PATTERN.matcher(line).find()) complexity++;
            if (SWITCH_PATTERN.matcher(line).find()) complexity++;
            if (CASE_PATTERN.matcher(line).find()) complexity++;
            if (SELECT_PATTERN.matcher(line).find()) complexity++;
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
     * 提取包名
     */
    private String extractPackageName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = PACKAGE_PATTERN.matcher(line.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "main";
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

            // Go通过字段类型建立依赖
            cls.getFields().forEach(field -> {
                graph.addDependency(className, field.getType());
            });
        }

        return graph;
    }

    private CodeStatistics calculateStatistics(Project project,
                                               List<ClassStructure> classes,
                                               List<InterfaceStructure> interfaces) {
        int totalMethods = classes.stream()
            .mapToInt(ClassStructure::getMethodCount)
            .sum();

        return CodeStatistics.builder()
            .totalFiles(project.getSourceFiles().size())
            .totalClasses(classes.size())
            .totalInterfaces(interfaces.size())
            .totalMethods(totalMethods)
            .totalLines(project.getTotalLines())
            .build();
    }

    private DesignPatterns detectDesignPatterns(List<ClassStructure> classes) {
        DesignPatterns patterns = DesignPatterns.builder().build();

        // 检测单例模式（通常使用sync.Once）
        detectSingletonPattern(classes, patterns);

        return patterns;
    }

    private void detectSingletonPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern singletonPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.SINGLETON)
            .name("单例模式")
            .build();

        for (ClassStructure cls : classes) {
            // Go单例通常有getInstance方法和sync.Once字段
            boolean hasGetInstance = cls.getMethods().stream()
                .anyMatch(m -> m.getMethodName().toLowerCase().contains("getinstance") ||
                              m.getMethodName().toLowerCase().contains("instance"));

            if (hasGetInstance) {
                singletonPattern.addInstance(cls.getClassName());
            }
        }

        if (singletonPattern.getInstanceCount() > 0) {
            singletonPattern.setConfidence(0.7);
            patterns.addPattern(singletonPattern);
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

            if (cls.getMethodCount() > 15) {
                insight.addCodeSmell(CodeSmell.builder()
                    .type(CodeSmell.SmellType.GOD_CLASS)
                    .severity(CodeSmell.Severity.HIGH)
                    .location(cls.getClassName())
                    .message(String.format("struct过大(%d个方法)，建议拆分", cls.getMethodCount()))
                    .build());
            }
        });
    }

    private boolean isGoFile(SourceFile file) {
        return "go".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public boolean supports(String projectType) {
        return "GO".equalsIgnoreCase(projectType) ||
               "GOLANG".equalsIgnoreCase(projectType);
    }

    @Override
    public String getParserName() {
        return "GoParser";
    }
}

