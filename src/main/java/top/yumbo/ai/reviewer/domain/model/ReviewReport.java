package top.yumbo.ai.reviewer.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评审报告领域模型
 * 包含完整的分析结果和评分信息
 */
@Data
@Builder
public class ReviewReport {

    private final String reportId;
    private final String projectName;
    private final String projectPath;

    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    // 总体评分
    private int overallScore;

    // 各维度评分
    @Builder.Default
    private Map<String, Integer> dimensionScores = new HashMap<>();

    // 分析结果
    private String projectOverview;

    @Builder.Default
    private List<ModuleAnalysis> moduleAnalyses = new ArrayList<>();

    private ArchitectureAnalysis architectureAnalysis;

    // 发现的问题
    @Builder.Default
    private List<Issue> issues = new ArrayList<>();

    // 改进建议
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();

    // 关键发现
    @Builder.Default
    private List<String> keyFindings = new ArrayList<>();

    /**
     * 添加维度评分
     */
    public void addDimensionScore(String dimension, int score) {
        dimensionScores.put(dimension, score);
    }

    /**
     * 获取维度评分
     */
    public int getDimensionScore(String dimension) {
        return dimensionScores.getOrDefault(dimension, 0);
    }

    /**
     * 添加问题
     */
    public void addIssue(Issue issue) {
        if (issue != null) {
            issues.add(issue);
        }
    }

    /**
     * 添加建议
     */
    public void addRecommendation(Recommendation recommendation) {
        if (recommendation != null) {
            recommendations.add(recommendation);
        }
    }

    /**
     * 添加关键发现
     */
    public void addKeyFinding(String finding) {
        if (finding != null && !finding.isBlank()) {
            keyFindings.add(finding);
        }
    }

    /**
     * 计算总体评分（基于维度权重）
     */
    public void calculateOverallScore(Map<String, Double> weights) {
        if (dimensionScores.isEmpty()) {
            this.overallScore = 0;
            return;
        }

        // 如果没有提供权重，使用平均值
        if (weights == null || weights.isEmpty()) {
            double sum = 0;
            for (int score : dimensionScores.values()) {
                sum += score;
            }
            this.overallScore = (int) Math.round(sum / dimensionScores.size());
            return;
        }

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<String, Integer> entry : dimensionScores.entrySet()) {
            String dimension = entry.getKey();
            int score = entry.getValue();
            double weight = weights.getOrDefault(dimension, 0.0);

            totalWeightedScore += score * weight;
            totalWeight += weight;
        }

        this.overallScore = totalWeight > 0
                ? (int) Math.round(totalWeightedScore / totalWeight)
                : 0;
    }

    /**
     * 是否达到及格标准
     */
    public boolean isPassThreshold(int threshold) {
        return overallScore >= threshold;
    }

    /**
     * 获取评级
     */
    public String getGrade() {
        if (overallScore >= 90) return "A";
        if (overallScore >= 80) return "B";
        if (overallScore >= 70) return "C";
        if (overallScore >= 60) return "D";
        return "F";
    }

    /**
     * 问题
     */
    @Data
    @Builder
    public static class Issue {
        private String type;
        private String severity;
        private String title;
        private String description;
        private String filePath;
        private Integer lineNumber;
        private String suggestion;
    }

    /**
     * 改进建议
     */
    @Data
    @Builder
    public static class Recommendation {
        private String category;
        private String priority;
        private String title;
        private String description;
        private String rationale;
        private int estimatedEffort; // 人天
    }

    /**
     * 模块分析
     */
    @Data
    @Builder
    public static class ModuleAnalysis {
        private String moduleName;
        private String responsibilities;
        private List<String> dependencies;
        private String analysisResult;
    }

    /**
     * 架构分析
     */
    @Data
    @Builder
    public static class ArchitectureAnalysis {
        private String architectureStyle;
        private String coreFlow;
        private List<String> technicalComponents;
        private List<String> bottlenecks;
        private String analysisResult;
    }
}


