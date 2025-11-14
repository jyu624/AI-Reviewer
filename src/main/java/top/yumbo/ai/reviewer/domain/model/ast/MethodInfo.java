package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法信息
 * <p>
 * 表示一个方法的完整信息，包括签名、复杂度、调用关系等
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class MethodInfo {

    private String methodName;
    private String returnType;

    @Builder.Default
    private List<Parameter> parameters = new ArrayList<>();

    private ClassStructure.AccessModifier accessModifier;

    @Builder.Default
    private boolean isStatic = false;

    @Builder.Default
    private boolean isFinal = false;

    @Builder.Default
    private boolean isAbstract = false;

    @Builder.Default
    private boolean isConstructor = false;

    // 复杂度指标
    @Builder.Default
    private int cyclomaticComplexity = 1;   // 圈复杂度

    @Builder.Default
    private int cognitiveComplexity = 0;    // 认知复杂度

    @Builder.Default
    private int linesOfCode = 0;

    @Builder.Default
    private int nestingDepth = 0;           // 嵌套深度

    // 调用关系
    @Builder.Default
    private List<String> calledMethods = new ArrayList<>();

    @Builder.Default
    private List<String> calledBy = new ArrayList<>();

    // 异常处理
    @Builder.Default
    private List<String> throwsExceptions = new ArrayList<>();

    @Builder.Default
    private boolean hasTryCatch = false;

    // 注解
    @Builder.Default
    private List<String> annotations = new ArrayList<>();

    // 代码坏味道
    @Builder.Default
    private List<CodeSmell> smells = new ArrayList<>();     // 如：方法过长、参数过多等

    /**
     * 方法参数
     */
    @Data
    @Builder
    public static class Parameter {
        private String name;
        private String type;

        @Builder.Default
        private boolean isFinal = false;

        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    /**
     * 获取方法签名
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();

        if (accessModifier != null) {
            sb.append(accessModifier.name().toLowerCase()).append(" ");
        }

        if (isStatic) {
            sb.append("static ");
        }

        if (isFinal) {
            sb.append("final ");
        }

        if (isAbstract) {
            sb.append("abstract ");
        }

        if (!isConstructor && returnType != null) {
            sb.append(returnType).append(" ");
        }

        sb.append(methodName).append("(");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).toString());
        }

        sb.append(")");

        if (!throwsExceptions.isEmpty()) {
            sb.append(" throws ").append(String.join(", ", throwsExceptions));
        }

        return sb.toString();
    }

    /**
     * 检查是否是长方法（>50行）
     */
    public boolean isLongMethod() {
        return linesOfCode > 50;
    }

    /**
     * 检查是否是复杂方法（圈复杂度>10）
     */
    public boolean isComplexMethod() {
        return cyclomaticComplexity > 10;
    }

    /**
     * 检查是否有太多参数（>5个）
     */
    public boolean hasTooManyParameters() {
        return parameters.size() > 5;
    }

    /**
     * 添加调用的方法
     */
    public void addCalledMethod(String methodName) {
        this.calledMethods.add(methodName);
    }

    /**
     * 添加调用者
     */
    public void addCaller(String methodName) {
        this.calledBy.add(methodName);
    }

    /**
     * 添加注解
     */
    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    /**
     * 添加代码坏味道
     */
    public void addSmell(CodeSmell smell) {
        this.smells.add(smell);
    }

    /**
     * 检测并添加代码坏味道
     */
    public void detectSmells() {
        if (isLongMethod()) {
            addSmell(CodeSmell.builder()
                .type(CodeSmell.SmellType.LONG_METHOD)
                .severity(CodeSmell.Severity.MEDIUM)
                .location(methodName)
                .message(String.format("方法过长(%d行)，建议拆分", linesOfCode))
                .build());
        }

        if (isComplexMethod()) {
            addSmell(CodeSmell.builder()
                .type(CodeSmell.SmellType.HIGH_COMPLEXITY)
                .severity(CodeSmell.Severity.HIGH)
                .location(methodName)
                .message(String.format("圈复杂度过高(%d)，建议重构", cyclomaticComplexity))
                .build());
        }

        if (hasTooManyParameters()) {
            addSmell(CodeSmell.builder()
                .type(CodeSmell.SmellType.TOO_MANY_PARAMETERS)
                .severity(CodeSmell.Severity.MEDIUM)
                .location(methodName)
                .message(String.format("参数过多(%d个)，建议使用对象封装", parameters.size()))
                .build());
        }
    }
}

