package top.yumbo.ai.reviewer.application.port.output;

import top.yumbo.ai.reviewer.domain.model.SourceFile;

import java.nio.file.Path;
import java.util.List;

/**
 * 文件系统端口（输出端口）
 * 定义与文件系统交互的接口
 */
public interface FileSystemPort {

    /**
     * 扫描项目文件
     * @param projectRoot 项目根目录
     * @return 源文件列表
     */
    List<SourceFile> scanProjectFiles(Path projectRoot);

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容
     */
    String readFileContent(Path filePath);

    /**
     * 写入文件内容
     * @param filePath 文件路径
     * @param content 文件内容
     */
    void writeFileContent(Path filePath, String content);

    /**
     * 生成项目结构树
     * @param projectRoot 项目根目录
     * @return 结构树字符串
     */
    String generateProjectStructure(Path projectRoot);

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean fileExists(Path filePath);

    /**
     * 检查目录是否存在
     * @param dirPath 目录路径
     * @return 是否存在
     */
    boolean directoryExists(Path dirPath);

    /**
     * 创建目录
     * @param dirPath 目录路径
     */
    void createDirectory(Path dirPath);
}

