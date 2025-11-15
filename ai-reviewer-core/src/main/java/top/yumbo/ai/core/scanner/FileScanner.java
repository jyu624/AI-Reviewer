package top.yumbo.ai.core.scanner;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.common.util.FileUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
/**
 * File scanner for discovering files in directory
 */
@Slf4j
public class FileScanner {
    /**
     * Scan directory for files
     */
    public List<Path> scan(Path directory) throws IOException {
        log.info("Scanning directory: {}", directory);
        List<Path> files = FileUtil.listFilesRecursively(directory)
                .collect(Collectors.toList());
        log.info("Found {} files", files.size());
        return files;
    }
    /**
     * Scan directory with size limit
     */
    public List<Path> scanWithSizeLimit(Path directory, long maxFileSize) throws IOException {
        log.info("Scanning directory with size limit: {} bytes", maxFileSize);
        List<Path> files = FileUtil.listFilesRecursively(directory)
                .filter(path -> {
                    try {
                        return java.nio.file.Files.size(path) <= maxFileSize;
                    } catch (IOException e) {
                        log.warn("Could not check size of file: {}", path, e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
        log.info("Found {} files within size limit", files.size());
        return files;
    }
}
