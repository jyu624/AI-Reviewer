package top.yumbo.ai.common.exception;
/**
 * Base exception for AI Reviewer
 */
public class AIReviewerException extends RuntimeException {
    public AIReviewerException(String message) {
        super(message);
    }
    public AIReviewerException(String message, Throwable cause) {
        super(message, cause);
    }
    public AIReviewerException(Throwable cause) {
        super(cause);
    }
}
