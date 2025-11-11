package top.yumbo.ai.reviewer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 摘要报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryReport {

    private String title;
    private String content;
    private List<String> keyFindings;
    private List<String> recommendations;
    private String analysisTime;

    // 评分分布
    private ScoreDistribution scoreDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreDistribution {
        private int excellent; // 90-100
        private int good;      // 70-89
        private int fair;      // 50-69
        private int poor;      // 0-49
    }
}
