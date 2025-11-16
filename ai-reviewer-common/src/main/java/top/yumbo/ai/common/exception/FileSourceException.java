package top.yumbo.ai.common.exception;

/**
 * Exception thrown when file source operations fail
 *
 * @author AI-Reviewer Team
 * @since 1.1.0
 */
public class FileSourceException extends AIReviewerException {

    public FileSourceException(String message) {
        super(message);
    }

    public FileSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSourceException(Throwable cause) {
        super(cause);
    }
}

