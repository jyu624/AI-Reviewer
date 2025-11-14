package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 类结构分析
 * <p>
 * 表示一个Java类的完整结构信息，包括字段、方法、继承关系等
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class ClassStructure {

    private String className;
    private String packageName;
    private String fullQualifiedName;

    // 访问控制
    private AccessModifier accessModifier;

    @Builder.Default
    private boolean isAbstract = false;

    @Builder.Default
    private boolean isFinal = false;

    @Builder.Default
    private boolean isStatic = false;

    // 继承关系
    private String superClass;

    @Builder.Default
    private List<String> interfaces = new ArrayList<>();

    // 组成部分
    @Builder.Default
    private List<FieldInfo> fields = new ArrayList<>();

    @Builder.Default
    private List<MethodInfo> methods = new ArrayList<>();

    @Builder.Default
    private List<String> annotations = new ArrayList<>();

    // 依赖关系
    @Builder.Default
    private Set<String> importedClasses = new HashSet<>();

    @Builder.Default
    private Set<String> dependentClasses = new HashSet<>();

    // 质量指标
    @Builder.Default
    private int linesOfCode = 0;

    @Builder.Default
    private int methodCount = 0;

    @Builder.Default
    private int fieldCount = 0;

    @Builder.Default
    private double cohesion = 0.0;            // 内聚度 LCOM

    @Builder.Default
    private int couplingLevel = 0;          // 耦合度 CBO

    // 设计模式识别
    @Builder.Default
    private List<DesignPattern> detectedPatterns = new ArrayList<>();

    // 职责描述（可选，通过AI生成）
    private String responsibility;

    /**
     * 访问修饰符枚举
     */
    public enum AccessModifier {
        PUBLIC, PROTECTED, PRIVATE, PACKAGE_PRIVATE
    }

    /**
     * 获取方法数量
     */
    public int getMethodCount() {
        return methods.size();
    }

    /**
     * 获取字段数量
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * 添加方法
     */
    public void addMethod(MethodInfo method) {
        this.methods.add(method);
        this.methodCount = this.methods.size();
    }

    /**
     * 添加字段
     */
    public void addField(FieldInfo field) {
        this.fields.add(field);
        this.fieldCount = this.fields.size();
    }

    /**
     * 添加注解
     */
    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    /**
     * 添加导入的类
     */
    public void addImportedClass(String className) {
        this.importedClasses.add(className);
    }

    /**
     * 添加依赖的类
     */
    public void addDependentClass(String className) {
        this.dependentClasses.add(className);
        this.couplingLevel = this.dependentClasses.size();
    }

    /**
     * 添加检测到的设计模式
     */
    public void addDetectedPattern(DesignPattern pattern) {
        this.detectedPatterns.add(pattern);
    }

    /**
     * 检查是否是接口
     */
    public boolean isInterface() {
        return false; // 实际应该在解析时设置
    }

    /**
     * 计算类的复杂度（基于方法复杂度总和）
     */
    public int getTotalComplexity() {
        return methods.stream()
            .mapToInt(MethodInfo::getCyclomaticComplexity)
            .sum();
    }

    /**
     * 获取高复杂度方法列表（复杂度>10）
     */
    public List<MethodInfo> getHighComplexityMethods() {
        return methods.stream()
            .filter(m -> m.getCyclomaticComplexity() > 10)
            .toList();
    }
}

