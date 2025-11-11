package top.yumbo.ai.refactor.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分析配置
 */
@Data
@Builder
public class AnalysisConfiguration {

    @Builder.Default
    private List<String> analysisDimensions = List.of(
            "architecture",
            "code_quality",
            "technical_debt",
            "functionality",
            "business_value",
            "test_coverage"
    );

    @Builder.Default
    private Map<String, Double> dimensionWeights = Map.of(
            "architecture", 0.20,
            "code_quality", 0.20,
            "technical_debt", 0.15,
            "functionality", 0.20,
            "business_value", 0.15,
            "test_coverage", 0.10
    );

    @Builder.Default
    private int batchSize = 5;

    @Builder.Default
    private int maxConcurrency = 3;

    @Builder.Default
    private boolean enableCaching = true;

    @Builder.Default
    private boolean enableParallelAnalysis = true;

    @Builder.Default
    private int maxFileSize = 1024; // KB

    @Builder.Default
    private int maxFilesCount = 100;

    /**
     * 验证配置有效性
     */
    public boolean isValid() {
        return analysisDimensions != null && !analysisDimensions.isEmpty()
                && dimensionWeights != null
                && batchSize > 0
                && maxConcurrency > 0
                && maxFileSize > 0
                && maxFilesCount > 0;
    }

    /**
     * 获取维度权重
     */
    public double getDimensionWeight(String dimension) {
        return dimensionWeights.getOrDefault(dimension, 0.0);
    }
}

