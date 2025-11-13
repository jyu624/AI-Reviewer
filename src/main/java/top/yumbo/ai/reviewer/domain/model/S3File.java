package top.yumbo.ai.reviewer.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * S3 文件领域模型
 * 表示存储在 AWS S3 中的文件
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@Builder
public class S3File {

    /**
     * 文件的 S3 键（Key）
     */
    private final String key;

    /**
     * 文件名（不含路径）
     */
    private final String fileName;

    /**
     * 文件所在的 S3 路径前缀
     */
    private final String prefix;

    /**
     * 文件大小（字节）
     */
    private final long sizeInBytes;

    /**
     * 文件最后修改时间
     */
    private final Instant lastModified;

    /**
     * 文件的 ETag（用于版本控制和验证）
     */
    private final String etag;

    /**
     * 文件内容类型（MIME type）
     */
    private String contentType;

    /**
     * 文件内容（按需加载）
     */
    private byte[] content;

    /**
     * 文件的存储类别
     */
    private String storageClass;

    /**
     * 是否为目录（以 / 结尾）
     */
    public boolean isDirectory() {
        return key.endsWith("/");
    }

    /**
     * 获取文件扩展名
     */
    public String getExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取文件大小（KB）
     */
    public double getSizeInKB() {
        return sizeInBytes / 1024.0;
    }

    /**
     * 获取文件大小（MB）
     */
    public double getSizeInMB() {
        return sizeInBytes / (1024.0 * 1024.0);
    }

    /**
     * 获取相对路径（去除前缀）
     */
    public String getRelativePath(String basePrefix) {
        if (basePrefix == null || basePrefix.isEmpty()) {
            return key;
        }
        if (key.startsWith(basePrefix)) {
            return key.substring(basePrefix.length());
        }
        return key;
    }

    /**
     * 判断是否为源代码文件
     */
    public boolean isSourceCode() {
        String ext = getExtension();
        return ext.matches("java|py|js|ts|go|rs|cpp|c|h|scala|kt|php|rb|cs|swift|dart");
    }

    /**
     * 判断是否为配置文件
     */
    public boolean isConfigFile() {
        String ext = getExtension();
        return ext.matches("yaml|yml|json|xml|properties|toml|ini|conf");
    }

    @Override
    public String toString() {
        return String.format("S3File{key='%s', size=%d bytes, lastModified=%s}",
                key, sizeInBytes, lastModified);
    }
}

