package top.yumbo.ai.reviewer.adapter.output.ast.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.stmt.*;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ProjectType;
import top.yumbo.ai.reviewer.domain.model.SourceFile;
import top.yumbo.ai.reviewer.domain.model.ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * JavaParser适配器
 * <p>
 * 使用JavaParser库解析Java源码，提取AST信息
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
public class JavaParserAdapter extends AbstractASTParser {

    private final JavaParser javaParser;

    public JavaParserAdapter() {
        this.javaParser = new JavaParser();
    }

    @Override
    protected CodeInsight doParse(Project project) {
        validateProject(project);

        // 初始化CodeInsight
        CodeInsight.CodeInsightBuilder builder = CodeInsight.builder()
            .projectName(project.getName());

        // 解析所有Java文件
        List<ClassStructure> classes = new ArrayList<>();
        List<InterfaceStructure> interfaces = new ArrayList<>();
        Map<String, Integer> packageClassCount = new HashMap<>();

        for (SourceFile sourceFile : project.getSourceFiles()) {
            if (!isJavaFile(sourceFile)) {
                continue;
            }

            try {
                parseSourceFile(sourceFile, classes, interfaces, packageClassCount);
            } catch (Exception e) {
                log.warn("解析文件失败: {} - {}", sourceFile.getRelativePath(), e.getMessage());
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

        // 构建CodeInsight
        CodeInsight insight = builder.build();

        // 检测设计模式（简单实现）
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
     * 解析单个源文件
     */
    private void parseSourceFile(SourceFile sourceFile,
                                  List<ClassStructure> classes,
                                  List<InterfaceStructure> interfaces,
                                  Map<String, Integer> packageClassCount) throws FileNotFoundException {

        File file = sourceFile.getPath().toFile();
        ParseResult<CompilationUnit> parseResult = javaParser.parse(file);

        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            log.warn("解析失败: {}", sourceFile.getRelativePath());
            return;
        }

        CompilationUnit cu = parseResult.getResult().get();

        // 获取包名
        String packageName = cu.getPackageDeclaration()
            .map(NodeWithName::getNameAsString)
            .orElse("default");

        // 统计包中的类数量
        packageClassCount.merge(packageName, 1, Integer::sum);

        // 解析类
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            if (classDecl.isInterface()) {
                InterfaceStructure interfaceStructure = parseInterface(classDecl, packageName);
                interfaces.add(interfaceStructure);
            } else {
                ClassStructure classStructure = parseClass(classDecl, packageName);
                classes.add(classStructure);
            }
        });

        // 解析枚举
        cu.findAll(EnumDeclaration.class).forEach(enumDecl -> {
            ClassStructure enumStructure = parseEnum(enumDecl, packageName);
            classes.add(enumStructure);
        });
    }

    /**
     * 解析类
     */
    private ClassStructure parseClass(ClassOrInterfaceDeclaration classDecl, String packageName) {
        String className = classDecl.getNameAsString();
        String fullName = packageName + "." + className;

        ClassStructure.ClassStructureBuilder builder = ClassStructure.builder()
            .className(className)
            .packageName(packageName)
            .fullQualifiedName(fullName)
            .isAbstract(classDecl.isAbstract())
            .isFinal(classDecl.isFinal())
            .isStatic(classDecl.isStatic());

        // 访问修饰符
        if (classDecl.isPublic()) {
            builder.accessModifier(ClassStructure.AccessModifier.PUBLIC);
        } else if (classDecl.isPrivate()) {
            builder.accessModifier(ClassStructure.AccessModifier.PRIVATE);
        } else if (classDecl.isProtected()) {
            builder.accessModifier(ClassStructure.AccessModifier.PROTECTED);
        } else {
            builder.accessModifier(ClassStructure.AccessModifier.PACKAGE_PRIVATE);
        }

        // 继承关系
        classDecl.getExtendedTypes().forEach(type -> builder.superClass(type.getNameAsString()));

        // 实现的接口
        List<String> implementedInterfaces = new ArrayList<>();
        classDecl.getImplementedTypes().forEach(type -> implementedInterfaces.add(type.getNameAsString()));
        builder.interfaces(implementedInterfaces);

        // 注解
        List<String> annotations = new ArrayList<>();
        classDecl.getAnnotations().forEach(ann -> annotations.add(ann.getNameAsString()));
        builder.annotations(annotations);

        ClassStructure classStructure = builder.build();

        // 解析字段
        classDecl.getFields().forEach(field -> field.getVariables()
            .forEach(variable -> classStructure.addField(parseField(variable, field))));

        // 解析方法
        classDecl.getMethods().forEach(method -> classStructure.addMethod(parseMethod(method)));

        // 计算代码行数
        classStructure.setLinesOfCode(
            classDecl.getEnd().map(pos -> pos.line).orElse(0) -
            classDecl.getBegin().map(pos -> pos.line).orElse(0) + 1
        );

        return classStructure;
    }

    /**
     * 解析接口
     */
    private InterfaceStructure parseInterface(ClassOrInterfaceDeclaration interfaceDecl, String packageName) {
        String interfaceName = interfaceDecl.getNameAsString();
        String fullName = packageName + "." + interfaceName;

        InterfaceStructure.InterfaceStructureBuilder builder = InterfaceStructure.builder()
            .interfaceName(interfaceName)
            .packageName(packageName)
            .fullQualifiedName(fullName);

        // 扩展的接口
        List<String> extendedInterfaces = new ArrayList<>();
        interfaceDecl.getExtendedTypes().forEach(type -> extendedInterfaces.add(type.getNameAsString()));
        builder.extendedInterfaces(extendedInterfaces);

        InterfaceStructure interfaceStructure = builder.build();

        // 解析方法
        interfaceDecl.getMethods().forEach(method -> interfaceStructure.addMethod(parseMethod(method)));

        return interfaceStructure;
    }

    /**
     * 解析枚举（作为特殊的类处理）
     */
    private ClassStructure parseEnum(EnumDeclaration enumDecl, String packageName) {
        String enumName = enumDecl.getNameAsString();
        String fullName = packageName + "." + enumName;

        return ClassStructure.builder()
            .className(enumName)
            .packageName(packageName)
            .fullQualifiedName(fullName)
            .isFinal(true)
            .accessModifier(ClassStructure.AccessModifier.PUBLIC)
            .linesOfCode(enumDecl.getEnd().map(pos -> pos.line).orElse(0) -
                        enumDecl.getBegin().map(pos -> pos.line).orElse(0) + 1)
            .build();
    }

    /**
     * 解析字段
     */
    private FieldInfo parseField(VariableDeclarator variable, FieldDeclaration field) {
        return FieldInfo.builder()
            .fieldName(variable.getNameAsString())
            .type(variable.getTypeAsString())
            .isStatic(field.isStatic())
            .isFinal(field.isFinal())
            .accessModifier(getAccessModifier(field))
            .initializer(variable.getInitializer().map(Object::toString).orElse(null))
            .build();
    }

    /**
     * 解析方法
     */
    private MethodInfo parseMethod(MethodDeclaration method) {
        MethodInfo.MethodInfoBuilder builder = MethodInfo.builder()
            .methodName(method.getNameAsString())
            .returnType(method.getTypeAsString())
            .isStatic(method.isStatic())
            .isFinal(method.isFinal())
            .isAbstract(method.isAbstract())
            .accessModifier(getAccessModifier(method));

        // 解析参数
        List<MethodInfo.Parameter> parameters = new ArrayList<>();
        method.getParameters().forEach(param -> parameters.add(MethodInfo.Parameter.builder()
                .name(param.getNameAsString())
                .type(param.getTypeAsString())
                .isFinal(param.isFinal())
                .build()));
        builder.parameters(parameters);

        // 抛出的异常
        List<String> exceptions = new ArrayList<>();
        method.getThrownExceptions().forEach(ex -> exceptions.add(ex.asString()));
        builder.throwsExceptions(exceptions);

        // 注解
        List<String> annotations = new ArrayList<>();
        method.getAnnotations().forEach(ann -> annotations.add(ann.getNameAsString()));
        builder.annotations(annotations);

        // 计算代码行数
        int linesOfCode = method.getEnd().map(pos -> pos.line).orElse(0) -
                         method.getBegin().map(pos -> pos.line).orElse(0) + 1;
        builder.linesOfCode(linesOfCode);

        // 计算圈复杂度
        int complexity = calculateCyclomaticComplexity(method);
        builder.cyclomaticComplexity(complexity);

        // 检查是否有try-catch
        builder.hasTryCatch(!method.findAll(TryStmt.class).isEmpty());

        MethodInfo methodInfo = builder.build();

        // 自动检测代码坏味道
        methodInfo.detectSmells();

        return methodInfo;
    }

    /**
     * 计算圈复杂度
     */
    private int calculateCyclomaticComplexity(MethodDeclaration method) {
        int complexity = 1;  // 基础复杂度

        // 统计决策点
        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(ForEachStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(DoStmt.class).size();
        complexity += method.findAll(CatchClause.class).size();
        complexity += method.findAll(SwitchEntry.class).size();
        complexity += method.findAll(ConditionalExpr.class).size();  // 三元运算符

        return complexity;
    }

    /**
     * 获取访问修饰符
     */
    private ClassStructure.AccessModifier getAccessModifier(BodyDeclaration<?> declaration) {
        // 使用JavaParser原生方法判断访问修饰符
        if (declaration instanceof FieldDeclaration field) {
            if (field.isPublic()) return ClassStructure.AccessModifier.PUBLIC;
            if (field.isPrivate()) return ClassStructure.AccessModifier.PRIVATE;
            if (field.isProtected()) return ClassStructure.AccessModifier.PROTECTED;
        } else if (declaration instanceof MethodDeclaration method) {
            if (method.isPublic()) return ClassStructure.AccessModifier.PUBLIC;
            if (method.isPrivate()) return ClassStructure.AccessModifier.PRIVATE;
            if (method.isProtected()) return ClassStructure.AccessModifier.PROTECTED;
        } else if (declaration instanceof ConstructorDeclaration constructor) {
            if (constructor.isPublic()) return ClassStructure.AccessModifier.PUBLIC;
            if (constructor.isPrivate()) return ClassStructure.AccessModifier.PRIVATE;
            if (constructor.isProtected()) return ClassStructure.AccessModifier.PROTECTED;
        }
        return ClassStructure.AccessModifier.PACKAGE_PRIVATE;
    }


    /**
     * 检测建造者模式（Java特有：检测Lombok @Builder注解）
     */
    private void detectBuilderPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern builderPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.BUILDER)
            .name("建造者模式")
            .build();

        for (ClassStructure cls : classes) {
            // 检测是否有Builder注解（Lombok）
            boolean hasBuilderAnnotation = cls.getAnnotations().stream()
                .anyMatch(ann -> ann.contains("Builder"));

            // 或者包含内部Builder类
            boolean hasBuilderInnerClass = cls.getClassName().endsWith("Builder") ||
                                          classes.stream().anyMatch(c ->
                                              c.getClassName().equals(cls.getClassName() + "Builder"));

            if (hasBuilderAnnotation || hasBuilderInnerClass) {
                builderPattern.addInstance(cls.getClassName());
            }
        }

        if (builderPattern.getInstanceCount() > 0) {
            builderPattern.setConfidence(0.9);
            patterns.addPattern(builderPattern);
        }
    }

    /**
     * Java特定的设计模式检测（覆盖基类方法以添加Builder模式检测）
     */
    @Override
    protected DesignPatterns detectDesignPatterns(List<ClassStructure> classes) {
        DesignPatterns patterns = super.detectDesignPatterns(classes);

        // 添加Java特有的Builder模式检测
        detectBuilderPattern(classes, patterns);

        return patterns;
    }

    private boolean isJavaFile(SourceFile file) {
        return "java".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public boolean supports(String projectType) {
        return "JAVA".equalsIgnoreCase(projectType) ||
               ProjectType.JAVA.name().equalsIgnoreCase(projectType);
    }

    @Override
    public String getParserName() {
        return "JavaParser";
    }
}
