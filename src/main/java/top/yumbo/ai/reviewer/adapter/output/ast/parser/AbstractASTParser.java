package top.yumbo.ai.reviewer.adapter.output.ast.parser;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.ASTParserPort;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ast.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AST解析器抽象基类
 *
 * 提供通用的解析逻辑和模板方法，消除子类中的重复代码
 *
 * @author AI-Reviewer Team
 * @version 2.0
 * @since 2025-11-13
 */
@Slf4j
public abstract class AbstractASTParser implements ASTParserPort {

    @Override
    public CodeInsight parseProject(Project project) {
        log.info("开始使用 {} 解析项目: {}", getParserName(), project.getName());

        long startTime = System.currentTimeMillis();

        try {
            // 模板方法：由子类实现具体的解析逻辑
            CodeInsight insight = doParse(project);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("项目解析完成: {} (耗时: {}ms, 类数: {}, 方法数: {})",
                project.getName(),
                elapsed,
                insight.getClasses().size(),
                insight.getStatistics() != null ? insight.getStatistics().getTotalMethods() : 0
            );

            return insight;

        } catch (Exception e) {
            log.error("项目解析失败: {}", project.getName(), e);
            throw new RuntimeException("AST解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 具体的解析逻辑，由子类实现
     */
    protected abstract CodeInsight doParse(Project project);

    /**
     * 验证项目是否可以解析
     */
    protected void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }

        if (!project.isValid()) {
            throw new IllegalArgumentException("项目信息无效");
        }

        if (project.getSourceFiles().isEmpty()) {
            throw new IllegalArgumentException("项目没有源文件");
        }
    }

    // ==================== 公共构建方法 ====================

    /**
     * 构建项目结构（所有解析器通用）
     */
    protected ProjectStructure buildProjectStructure(List<ClassStructure> classes,
                                                    Map<String, Integer> packageClassCount) {
        if (classes.isEmpty()) {
            return ProjectStructure.builder().build();
        }

        String rootPackage = findRootPackage(classes);

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

    /**
     * 查找根包名
     */
    protected String findRootPackage(List<ClassStructure> classes) {
        if (classes.isEmpty()) {
            return "default";
        }
        String firstPackage = classes.get(0).getPackageName();
        if (firstPackage != null && firstPackage.contains(".")) {
            return firstPackage.split("\\.")[0];
        }
        return firstPackage != null ? firstPackage : "default";
    }

    /**
     * 构建依赖图（所有解析器通用）
     */
    protected DependencyGraph buildDependencyGraph(List<ClassStructure> classes) {
        DependencyGraph graph = DependencyGraph.builder().build();

        for (ClassStructure cls : classes) {
            String className = cls.getFullQualifiedName();

            // 添加父类依赖
            if (cls.getSuperClass() != null) {
                graph.addDependency(className, cls.getSuperClass());
            }

            // 添加接口依赖
            cls.getInterfaces().forEach(intf -> {
                graph.addDependency(className, intf);
            });

            // 添加字段类型依赖（如果存在）
            if (cls.getFields() != null) {
                cls.getFields().forEach(field -> {
                    if (field.getType() != null) {
                        graph.addDependency(className, field.getType());
                    }
                });
            }
        }

        return graph;
    }

    /**
     * 计算统计信息（所有解析器通用）
     *
     * @param project 项目
     * @param classes 类列表
     * @param interfaces 接口列表（可选，Go/Python等语言可传null或空列表）
     */
    protected CodeStatistics calculateStatistics(Project project, List<ClassStructure> classes,
                                                List<InterfaceStructure> interfaces) {
        int totalMethods = classes.stream()
            .mapToInt(ClassStructure::getMethodCount)
            .sum();

        int totalFields = classes.stream()
            .mapToInt(cls -> cls.getFields() != null ? cls.getFields().size() : 0)
            .sum();

        int interfaceCount = interfaces != null ? interfaces.size() : 0;

        return CodeStatistics.builder()
            .totalFiles(project.getSourceFiles().size())
            .totalClasses(classes.size())
            .totalInterfaces(interfaceCount)
            .totalMethods(totalMethods)
            .totalLines(project.getTotalLines())
            .totalFields(totalFields)
            .build();
    }

    /**
     * 计算复杂度指标（所有解析器通用）
     */
    protected ComplexityMetrics calculateComplexityMetrics(List<ClassStructure> classes) {
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
            .mostComplexMethod(mostComplex != null ? mostComplex.getMethodName() : "N/A")
            .highComplexityMethodCount((int) highComplexityCount)
            .avgMethodLength(avgLength)
            .longMethodCount((int) longMethodCount)
            .tooManyParametersCount((int) tooManyParamsCount)
            .totalMethods(allMethods.size())
            .totalClasses(classes.size())
            .build();
    }

    /**
     * 检测代码坏味道（所有解析器通用）
     */
    protected void detectCodeSmells(CodeInsight insight) {
        insight.getClasses().forEach(cls -> {
            // 检测方法级别的坏味道
            cls.getMethods().forEach(method -> {
                method.getSmells().forEach(insight::addCodeSmell);
            });

            // 检测类级别的坏味道 - God Class（上帝类）
            if (cls.getMethodCount() > 20) {
                insight.addCodeSmell(CodeSmell.builder()
                    .type(CodeSmell.SmellType.GOD_CLASS)
                    .severity(CodeSmell.Severity.HIGH)
                    .location(cls.getClassName())
                    .message(String.format("类过大(%d个方法)，建议拆分", cls.getMethodCount()))
                    .build());
            }
        });
    }

    /**
     * 检测单例模式（可被子类覆盖以适配语言特性）
     */
    protected void detectSingletonPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern singletonPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.SINGLETON)
            .name("单例模式")
            .build();

        for (ClassStructure cls : classes) {
            // 检查是否有 getInstance 等典型的单例方法
            boolean hasSingletonMethod = cls.getMethods().stream()
                .anyMatch(m ->
                    m.getMethodName().toLowerCase().contains("getinstance") ||
                    m.getMethodName().toLowerCase().contains("instance")
                );

            if (hasSingletonMethod && cls.getMethods().stream().anyMatch(m -> m.isStatic())) {
                singletonPattern.addInstance(cls.getClassName());
            }
        }

        if (singletonPattern.getInstanceCount() > 0) {
            singletonPattern.setConfidence(0.7);
            patterns.addPattern(singletonPattern);
        }
    }

    /**
     * 检测工厂模式
     */
    protected void detectFactoryPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern factoryPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.FACTORY)
            .name("工厂模式")
            .build();

        for (ClassStructure cls : classes) {
            boolean isFactory = cls.getClassName().toLowerCase().contains("factory");
            boolean hasCreateMethod = cls.getMethods().stream()
                .anyMatch(m -> m.getMethodName().toLowerCase().contains("create"));

            if (isFactory && hasCreateMethod) {
                factoryPattern.addInstance(cls.getClassName());
            }
        }

        if (factoryPattern.getInstanceCount() > 0) {
            factoryPattern.setConfidence(0.8);
            patterns.addPattern(factoryPattern);
        }
    }

    /**
     * 检测观察者模式
     */
    protected void detectObserverPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern observerPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.OBSERVER)
            .name("观察者模式")
            .build();

        for (ClassStructure cls : classes) {
            boolean hasListenerMethods = cls.getMethods().stream()
                .anyMatch(m ->
                    m.getMethodName().toLowerCase().contains("addlistener") ||
                    m.getMethodName().toLowerCase().contains("removelistener") ||
                    m.getMethodName().toLowerCase().contains("notify")
                );

            if (hasListenerMethods) {
                observerPattern.addInstance(cls.getClassName());
            }
        }

        if (observerPattern.getInstanceCount() > 0) {
            observerPattern.setConfidence(0.75);
            patterns.addPattern(observerPattern);
        }
    }

    /**
     * 检测装饰器模式（可被子类覆盖）
     */
    protected void detectDecoratorPattern(List<ClassStructure> classes, DesignPatterns patterns) {
        DesignPattern decoratorPattern = DesignPattern.builder()
            .type(DesignPattern.PatternType.DECORATOR)
            .name("装饰器模式")
            .build();

        for (ClassStructure cls : classes) {
            // 检查是否使用了大量注解/装饰器
            long decoratedMethodsCount = cls.getMethods().stream()
                .filter(m -> m.getAnnotations() != null && !m.getAnnotations().isEmpty())
                .count();

            if (decoratedMethodsCount > 3) {
                decoratorPattern.addInstance(cls.getClassName());
            }
        }

        if (decoratorPattern.getInstanceCount() > 0) {
            decoratorPattern.setConfidence(0.6);
            patterns.addPattern(decoratorPattern);
        }
    }

    /**
     * 检测设计模式（通用实现，子类可以添加语言特定的检测）
     */
    protected DesignPatterns detectDesignPatterns(List<ClassStructure> classes) {
        DesignPatterns patterns = DesignPatterns.builder().build();

        // 检测常见设计模式
        detectSingletonPattern(classes, patterns);
        detectFactoryPattern(classes, patterns);
        detectObserverPattern(classes, patterns);
        detectDecoratorPattern(classes, patterns);

        return patterns;
    }
}

