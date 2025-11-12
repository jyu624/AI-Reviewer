package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

/**
 * 代码统计信息
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class CodeStatistics {

    @Builder.Default
    private int totalFiles = 0;

    @Builder.Default
    private int totalClasses = 0;

    @Builder.Default
    private int totalInterfaces = 0;

    @Builder.Default
    private int totalMethods = 0;

    @Builder.Default
    private int totalFields = 0;

    @Builder.Default
    private int totalLines = 0;

    @Builder.Default
    private int totalCodeLines = 0;  // 不包括空行和注释

    @Builder.Default
    private int totalCommentLines = 0;

    @Builder.Default
    private int totalBlankLines = 0;

    /**
     * 计算注释率
     */
    public double getCommentRatio() {
        if (totalCodeLines == 0) return 0.0;
        return (double) totalCommentLines / totalCodeLines;
    }

    /**
     * 计算平均每个类的方法数
     */
    public double getAvgMethodsPerClass() {
        if (totalClasses == 0) return 0.0;
        return (double) totalMethods / totalClasses;
    }

    /**
     * 计算平均每个方法的代码行数
     */
    public double getAvgLinesPerMethod() {
        if (totalMethods == 0) return 0.0;
        return (double) totalCodeLines / totalMethods;
    }
}

