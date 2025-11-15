package top.yumbo.ai.api.model;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
/**
 * Process result model containing final output
 */
@Data
@Builder
public class ProcessResult {
    /**
     * Result type
     */
    private String resultType;
    /**
     * Result content
     */
    private String content;
    /**
     * Output format
     */
    private String format;
    /**
     * Processing timestamp
     */
    private LocalDateTime timestamp;
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    /**
     * Success flag
     */
    private boolean success;
    /**
     * Error message if failed
     */
    private String errorMessage;
}
