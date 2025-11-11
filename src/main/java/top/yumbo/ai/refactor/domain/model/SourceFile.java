package top.yumbo.ai.refactor.domain.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

/**
 * 源文件领域模型
 */
@Data
@Builder
public class SourceFile {

    private final Path path;
    private final String relativePath;
    private final String fileName;
    private final String extension;

    private String content;
    private int lineCount;
    private long sizeInBytes;

    @Builder.Default
    private boolean isCore = false;

    @Builder.Default
    private FileCategory category = FileCategory.UNKNOWN;

    @Builder.Default
    private int priority = 0;

    /**
     * 获取文件类型
     */
    public ProjectType getProjectType() {
        return ProjectType.fromExtension(extension);
    }

    /**
     * 判断是否为配置文件
     */
    public boolean isConfigFile() {
        return category == FileCategory.CONFIG;
    }

    /**
     * 判断是否为入口文件
     */
    public boolean isEntryPoint() {
        return category == FileCategory.ENTRY_POINT;
    }

    /**
     * 判断是否为测试文件
     */
    public boolean isTestFile() {
        return category == FileCategory.TEST;
    }

    /**
     * 文件类别
     */
    public enum FileCategory {
        ENTRY_POINT,    // 入口文件
        CONFIG,         // 配置文件
        CORE_BUSINESS,  // 核心业务
        UTIL,           // 工具类
        TEST,           // 测试文件
        DOCUMENTATION,  // 文档文件
        SOURCE_CODE,    // 源码文件
        UNKNOWN         // 未知类型
    }
}


