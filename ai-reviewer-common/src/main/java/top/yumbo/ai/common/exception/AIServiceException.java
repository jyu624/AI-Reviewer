package top.yumbo.ai.common.exception;
/**
 * Exception thrown during AI service invocation
 */
public class AIServiceException extends AIReviewerException {
    public AIServiceException(String message) {
        super(message);
    }
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
