package top.yumbo.ai.reviewer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 检查文件/目录是否存在
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * 读取文件内容
     */
    public static String readContent(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * 写入文件内容
     */
    public static void writeContent(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    /**
     * 获取文件大小（字节）
     */
    public static long getSize(Path path) throws IOException {
        return Files.size(path);
    }

    /**
     * 判断是否为目录
     */
    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * 判断是否为常规文件
     */
    public static boolean isRegularFile(Path path) {
        return Files.isRegularFile(path);
    }

    /**
     * 获取相对路径
     */
    public static String getRelativePath(Path base, Path target) {
        return base.relativize(target).toString();
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * 创建目录（如果不存在）
     */
    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }
}
