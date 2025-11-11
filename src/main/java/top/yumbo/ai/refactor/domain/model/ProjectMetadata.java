package top.yumbo.ai.refactor.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目元数据
 */
@Data
@NoArgsConstructor
public class ProjectMetadata {

    private int fileCount = 0;
    private int totalLines = 0;
    private long sizeInBytes = 0;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime analyzedAt;

    private Map<String, String> customProperties = new HashMap<>();

    /**
     * 增加文件计数
     */
    public void incrementFileCount() {
        this.fileCount++;
    }

    /**
     * 添加代码行数
     */
    public void addLines(int lines) {
        this.totalLines += lines;
    }

    /**
     * 添加文件大小
     */
    public void addSize(long bytes) {
        this.sizeInBytes += bytes;
    }

    /**
     * 添加自定义属性
     */
    public void putProperty(String key, String value) {
        customProperties.put(key, value);
    }

    /**
     * 获取自定义属性
     */
    public String getProperty(String key) {
        return customProperties.get(key);
    }
}

