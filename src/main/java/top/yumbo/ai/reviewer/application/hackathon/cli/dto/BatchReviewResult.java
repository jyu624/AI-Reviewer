package top.yumbo.ai.reviewer.application.hackathon.cli.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量评审结果
 */
@Data
@Builder
public class BatchReviewResult {
    private int totalCount;
    private int successCount;
    private int failureCount;
    private long duration;
    private List<ReviewResult> results;
}

