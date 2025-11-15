package top.yumbo.ai.api.model;
import lombok.Builder;
import lombok.Data;
import java.nio.file.Path;
import java.util.Map;
/**
 * Processor configuration
 */
@Data
@Builder
public class ProcessorConfig {
    /**
     * Processor type
     */
    private String processorType;
    /**
     * Output format
     */
    @Builder.Default
    private String outputFormat = "markdown";
    /**
     * Output path
     */
    private Path outputPath;
    /**
     * Template path (if using templates)
     */
    private Path templatePath;
    /**
     * Custom parameters
     */
    private Map<String, Object> customParams;
}
