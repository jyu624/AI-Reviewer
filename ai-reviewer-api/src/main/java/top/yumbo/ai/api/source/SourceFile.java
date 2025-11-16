package top.yumbo.ai.api.source;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract representation of a file from any source
 *
 * This class represents a file that can come from various sources
 * (local filesystem, SFTP, Git, S3, etc.) in a unified way.
 *
 * @author AI-Reviewer Team
 * @since 1.1.0
 */
@Data
@Builder
public class SourceFile {

    /**
     * Unique identifier for this file within its source
     * For local files: absolute path
     * For SFTP: remote path
     * For S3: object key
     * For Git: relative path in repository
     */
    private String fileId;

    /**
     * Relative path from the base path of the source
     */
    private String relativePath;

    /**
     * File name (without path)
     */
    private String fileName;

    /**
     * File size in bytes
     */
    private long fileSize;

    /**
     * Last modified timestamp
     */
    private LocalDateTime lastModified;

    /**
     * File metadata (custom attributes specific to the source)
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Reference to the file source (used for reading content)
     */
    private IFileSource source;

    /**
     * Get an input stream to read the file content
     *
     * @return input stream of file content
     * @throws Exception if reading fails
     */
    public InputStream getInputStream() throws Exception {
        if (source == null) {
            throw new IllegalStateException("File source is not set");
        }
        return source.readFile(this);
    }

    /**
     * Convert this source file to a temporary local file
     * This is useful when the parser requires a java.io.File object
     *
     * @return temporary local file
     * @throws Exception if conversion fails
     */
    public File toTempFile() throws Exception {
        // Create temporary file with original extension
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }

        File tempFile = File.createTempFile("ai-reviewer-", extension);
        tempFile.deleteOnExit(); // Automatically delete on JVM exit

        // Copy content from source to temp file
        try (InputStream in = getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            in.transferTo(out);
        }

        return tempFile;
    }

    /**
     * Put metadata
     *
     * @param key metadata key
     * @param value metadata value
     */
    public void putMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get metadata
     *
     * @param key metadata key
     * @return metadata value, or null if not found
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Get file extension
     *
     * @return file extension (without dot), or empty string if no extension
     */
    public String getExtension() {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Check if this is a specific file type
     *
     * @param extension file extension to check (without dot)
     * @return true if file has the specified extension
     */
    public boolean isFileType(String extension) {
        return getExtension().equalsIgnoreCase(extension);
    }
}

