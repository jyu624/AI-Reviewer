package top.yumbo.ai.common.exception;
/**
 * Exception thrown during file parsing
 */
public class ParseException extends AIReviewerException {
    public ParseException(String message) {
        super(message);
    }
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
