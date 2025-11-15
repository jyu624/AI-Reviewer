package top.yumbo.ai.common.exception;
/**
 * Exception thrown during result processing
 */
public class ProcessorException extends AIReviewerException {
    public ProcessorException(String message) {
        super(message);
    }
    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
