package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码洞察 - 项目级别的代码分析结果
 *
 * 这是AST分析的核心领域模型，包含了项目的结构化代码信息
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class CodeInsight {

    private String projectName;

    // 结构分析
    private ProjectStructure structure;          // 项目结构

    @Builder.Default
    private List<ClassStructure> classes = new ArrayList<>();        // 所有类

    @Builder.Default
    private List<InterfaceStructure> interfaces = new ArrayList<>(); // 所有接口

    // 关系分析
    private DependencyGraph dependencyGraph;     // 依赖图

    // 质量指标
    private ComplexityMetrics complexityMetrics; // 复杂度指标

    @Builder.Default
    private List<CodeSmell> codeSmells = new ArrayList<>();          // 代码坏味道

    private DesignPatterns designPatterns;       // 设计模式

    // 统计信息
    private CodeStatistics statistics;           // 代码统计

    // 热点分析
    @Builder.Default
    private List<HotSpot> hotSpots = new ArrayList<>();             // 代码热点（高复杂度区域）

    /**
     * 生成结构化的AI提示词
     * 将CodeInsight转换为适合AI分析的文本格式
     */
    public String toAIPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("## 代码结构分析\n\n");

        // 项目结构
        if (structure != null) {
            sb.append("### 包/模块结构\n");
            sb.append(structure.toTreeString()).append("\n\n");
        }

        // 核心类列表
        if (!classes.isEmpty()) {
            sb.append("### 核心类列表（Top 10）\n");
            classes.stream()
                .sorted((a, b) -> Integer.compare(b.getMethodCount(), a.getMethodCount()))
                .limit(10)
                .forEach(cls -> {
                    sb.append(String.format("- %s: %d个方法, %d个字段, 耦合度=%d\n",
                        cls.getClassName(),
                        cls.getMethodCount(),
                        cls.getFieldCount(),
                        cls.getCouplingLevel()
                    ));
                });
            sb.append("\n");
        }

        // 架构设计
        sb.append("## 架构设计\n\n");
        if (structure != null) {
            sb.append("### 架构分层\n");
            sb.append(structure.detectArchitectureStyle()).append("\n\n");
        }

        // 设计模式
        if (designPatterns != null && !designPatterns.getPatterns().isEmpty()) {
            sb.append("### 设计模式使用\n");
            designPatterns.getPatterns().forEach(pattern -> {
                sb.append(String.format("- %s: %d处\n",
                    pattern.getName(),
                    pattern.getInstances().size()
                ));
            });
            sb.append("\n");
        }

        // 质量指标
        if (complexityMetrics != null) {
            sb.append("## 代码质量指标\n\n");
            sb.append(String.format("- 平均圈复杂度: %.2f\n", complexityMetrics.getAvgCyclomaticComplexity()));
            sb.append(String.format("- 最高圈复杂度: %d (方法: %s)\n",
                complexityMetrics.getMaxCyclomaticComplexity(),
                complexityMetrics.getMostComplexMethod()));
            sb.append(String.format("- 复杂度>10的方法数: %d\n", complexityMetrics.getHighComplexityMethodCount()));
            sb.append(String.format("- 平均方法长度: %.1f 行\n", complexityMetrics.getAvgMethodLength()));
            sb.append(String.format("- 长方法(>50行)数量: %d\n", complexityMetrics.getLongMethodCount()));
            sb.append(String.format("- 检测到的代码坏味道: %d 个\n\n", codeSmells.size()));
        }

        // 关键发现
        if (!codeSmells.isEmpty()) {
            sb.append("## 关键发现\n\n");
            sb.append("### 需要改进\n");
            codeSmells.stream()
                .limit(5)
                .forEach(smell -> {
                    sb.append(String.format("⚠️ %s: %s\n", smell.getLocation(), smell.getMessage()));
                });
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 添加类结构
     */
    public void addClass(ClassStructure classStructure) {
        this.classes.add(classStructure);
    }

    /**
     * 添加接口结构
     */
    public void addInterface(InterfaceStructure interfaceStructure) {
        this.interfaces.add(interfaceStructure);
    }

    /**
     * 添加代码坏味道
     */
    public void addCodeSmell(CodeSmell smell) {
        this.codeSmells.add(smell);
    }

    /**
     * 添加热点
     */
    public void addHotSpot(HotSpot hotSpot) {
        this.hotSpots.add(hotSpot);
    }
}

