package top.yumbo.ai.common.util;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.common.constants.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
/**
 * File utility class
 */
@Slf4j
public final class FileUtil {
    private FileUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    /**
     * Read file content as string
     */
    public static String readFileToString(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
    /**
     * Read file content as string
     */
    public static String readFileToString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    /**
     * Write string to file
     */
    public static void writeStringToFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
    /**
     * Get file extension
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot) : "";
    }
    /**
     * Check if file size is acceptable
     */
    public static boolean isFileSizeAcceptable(File file) {
        return file.length() <= Constants.MAX_FILE_SIZE;
    }
    /**
     * List all files in directory recursively
     */
    public static Stream<Path> listFilesRecursively(Path directory) throws IOException {
        return Files.walk(directory)
                .filter(Files::isRegularFile);
    }
}
