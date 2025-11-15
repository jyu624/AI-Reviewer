package top.yumbo.ai.api.model;
import lombok.Builder;
import lombok.Data;
import java.util.Map;
/**
 * Preprocessed data model containing parsed file information
 */
@Data
@Builder
public class PreProcessedData {
    /**
     * File metadata
     */
    private FileMetadata metadata;
    /**
     * Parsed structured content
     */
    private String content;
    /**
     * Additional context information
     */
    private Map<String, Object> context;
    /**
     * Parser used to create this data
     */
    private String parserName;
}
