package top.yumbo.ai.api.source;

import java.io.InputStream;
import java.util.List;

/**
 * File source interface - unified abstraction for different file sources
 *
 * This interface allows the AI Reviewer to obtain files from various sources:
 * - Local file system
 * - SFTP/FTP servers
 * - Git repositories (GitHub/GitLab/Gitee)
 * - Cloud storage (AWS S3, Azure Blob, Alibaba OSS)
 * - Archives (ZIP, TAR, JAR)
 *
 * @author AI-Reviewer Team
 * @since 1.1.0
 */
public interface IFileSource extends AutoCloseable {

    /**
     * Get the name of this file source
     *
     * @return source identifier (e.g., "local", "sftp", "git", "s3")
     */
    String getSourceName();

    /**
     * Check if this file source supports the given configuration
     *
     * @param config file source configuration
     * @return true if this source can handle the configuration, false otherwise
     */
    boolean support(FileSourceConfig config);

    /**
     * Initialize the file source with the given configuration
     * This method establishes connections, authenticates, and prepares the source for use
     *
     * @param config file source configuration
     * @throws Exception if initialization fails
     */
    void initialize(FileSourceConfig config) throws Exception;

    /**
     * List all files from the specified base path
     *
     * @param basePath base path to list files from (relative to source root)
     * @return list of source files
     * @throws Exception if listing fails
     */
    List<SourceFile> listFiles(String basePath) throws Exception;

    /**
     * Read the content of a specific file
     *
     * @param file the source file to read
     * @return input stream of file content
     * @throws Exception if reading fails
     */
    InputStream readFile(SourceFile file) throws Exception;

    /**
     * Close the file source and release resources
     * This method should close connections, clean up temporary files, etc.
     *
     * @throws Exception if cleanup fails
     */
    @Override
    void close() throws Exception;

    /**
     * Get the priority of this file source
     * Higher values indicate higher priority when multiple sources support the same config
     *
     * @return priority value, default is 0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Check if the file source is initialized and ready to use
     *
     * @return true if initialized, false otherwise
     */
    default boolean isInitialized() {
        return true;
    }
}

