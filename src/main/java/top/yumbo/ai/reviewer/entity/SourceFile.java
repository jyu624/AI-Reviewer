package top.yumbo.ai.reviewer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.nio.file.Path;
import java.util.List;

/**
 * 源文件实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceFile {

    private Path filePath;
    private FileType fileType;
    private String content;
    private long size; // 文件大小（字节）
    private String language; // 编程语言
    private List<String> imports; // 导入的包/模块
    private List<String> classes; // 定义的类
    private List<String> functions; // 定义的函数/方法
    private int linesOfCode; // 代码行数
    private boolean isTestFile; // 是否为测试文件
    private boolean isConfigFile; // 是否为配置文件
    private String relativePath; // 相对项目根目录的路径

    /**
     * 文件类型枚举
     */
    public enum FileType {
        SOURCE_CODE,      // 源代码文件
        CONFIGURATION,    // 配置文件
        DOCUMENTATION,    // 文档文件
        BUILD_SCRIPT,     // 构建脚本
        TEST,            // 测试��件
        RESOURCE,        // 资源文件
        DEPENDENCY,      // 依赖文件
        OTHER            // 其他
    }

    /**
     * 判断是否为核心文件
     */
    public boolean isCoreFile() {
        return fileType == FileType.SOURCE_CODE ||
               fileType == FileType.CONFIGURATION ||
               fileType == FileType.BUILD_SCRIPT;
    }

    /**
     * 获取文件扩展名
     */
    public String getExtension() {
        String fileName = filePath.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * 源文件构建器
     */
    public static class SourceFileBuilder {
        public SourceFileBuilder content(String content) {
            this.content = content;
            this.linesOfCode = content != null ? content.split("\n").length : 0;
            return this;
        }
    }
}
