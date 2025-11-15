package top.yumbo.ai.api.model;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
/**
 * AI response model containing AI service output
 */
@Data
@Builder
public class AIResponse {
    /**
     * Response content from AI
     */
    private String content;
    /**
     * Confidence score (0.0 to 1.0)
     */
    private Double confidence;
    /**
     * Model used for generation
     */
    private String model;
    /**
     * Provider name
     */
    private String provider;
    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;
    /**
     * Timestamp of response
     */
    private LocalDateTime timestamp;
    /**
     * Token usage information
     */
    private TokenUsage tokenUsage;
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    @Data
    @Builder
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
