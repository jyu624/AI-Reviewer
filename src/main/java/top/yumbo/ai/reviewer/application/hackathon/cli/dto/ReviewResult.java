package top.yumbo.ai.reviewer.application.hackathon.cli.dto;

import lombok.Builder;
import lombok.Data;
import top.yumbo.ai.reviewer.domain.model.ReviewReport;

/**
 * 评审结果
 */
@Data
@Builder
public class ReviewResult {
    private TeamSubmission submission;
    private ReviewReport report;
    private boolean success;
    private String errorMessage;

    public static ReviewResult success(TeamSubmission submission, ReviewReport report) {
        return ReviewResult.builder()
            .submission(submission)
            .report(report)
            .success(true)
            .build();
    }

    public static ReviewResult failure(TeamSubmission submission, String errorMessage) {
        return ReviewResult.builder()
            .submission(submission)
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }
}

