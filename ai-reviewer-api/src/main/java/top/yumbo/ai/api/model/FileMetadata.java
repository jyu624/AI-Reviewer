package top.yumbo.ai.api.model;
import lombok.Builder;
import lombok.Data;
import java.nio.file.Path;
/**
 * File metadata model
 */
@Data
@Builder
public class FileMetadata {
    /**
     * File path
     */
    private Path filePath;
    /**
     * File name
     */
    private String fileName;
    /**
     * File type/extension
     */
    private String fileType;
    /**
     * File size in bytes
     */
    private long fileSize;
    /**
     * MIME type
     */
    private String mimeType;
    /**
     * File encoding
     */
    private String encoding;
}
