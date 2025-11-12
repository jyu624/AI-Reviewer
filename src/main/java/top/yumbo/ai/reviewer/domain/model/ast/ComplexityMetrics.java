package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

/**
 * 复杂度指标
 *
 * 汇总项目级别的复杂度统计信息
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class ComplexityMetrics {

    // 圈复杂度统计
    @Builder.Default
    private double avgCyclomaticComplexity = 0.0;

    @Builder.Default
    private int maxCyclomaticComplexity = 0;

    private String mostComplexMethod;  // 最复杂的方法

    @Builder.Default
    private int highComplexityMethodCount = 0;  // 复杂度>10的方法数

    // 认知复杂度统计
    @Builder.Default
    private double avgCognitiveComplexity = 0.0;

    @Builder.Default
    private int maxCognitiveComplexity = 0;

    // 方法长度统计
    @Builder.Default
    private double avgMethodLength = 0.0;

    @Builder.Default
    private int maxMethodLength = 0;

    @Builder.Default
    private int longMethodCount = 0;  // 长方法(>50行)数量

    // 类复杂度统计
    @Builder.Default
    private double avgClassComplexity = 0.0;

    @Builder.Default
    private int maxClassComplexity = 0;

    private String mostComplexClass;  // 最复杂的类

    // 嵌套深度统计
    @Builder.Default
    private double avgNestingDepth = 0.0;

    @Builder.Default
    private int maxNestingDepth = 0;

    // 参数统计
    @Builder.Default
    private double avgParameterCount = 0.0;

    @Builder.Default
    private int maxParameterCount = 0;

    @Builder.Default
    private int tooManyParametersCount = 0;  // 参数>5的方法数

    // 总体统计
    @Builder.Default
    private int totalMethods = 0;

    @Builder.Default
    private int totalClasses = 0;

    /**
     * 计算复杂度等级
     */
    public String getComplexityGrade() {
        if (avgCyclomaticComplexity < 5) {
            return "优秀";
        } else if (avgCyclomaticComplexity < 10) {
            return "良好";
        } else if (avgCyclomaticComplexity < 15) {
            return "中等";
        } else if (avgCyclomaticComplexity < 20) {
            return "较差";
        } else {
            return "很差";
        }
    }

    /**
     * 生成复杂度报告摘要
     */
    public String toSummary() {
        return String.format("""
            复杂度分析摘要:
            - 平均圈复杂度: %.2f (%s)
            - 最高圈复杂度: %d (方法: %s)
            - 高复杂度方法数: %d (占比: %.1f%%)
            - 平均方法长度: %.1f 行
            - 长方法数量: %d
            - 参数过多的方法: %d
            """,
            avgCyclomaticComplexity,
            getComplexityGrade(),
            maxCyclomaticComplexity,
            mostComplexMethod != null ? mostComplexMethod : "N/A",
            highComplexityMethodCount,
            totalMethods > 0 ? (highComplexityMethodCount * 100.0 / totalMethods) : 0.0,
            avgMethodLength,
            longMethodCount,
            tooManyParametersCount
        );
    }
}

