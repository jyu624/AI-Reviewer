package top.yumbo.ai.reviewer.application.port.output;

import lombok.Builder;

import java.nio.file.Path;

/**
 * 克隆请求值对象
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
@Builder
public record CloneRequest(
    String url,
    String branch,
    Path targetDirectory,
    int timeoutSeconds
) {
    /**
     * 创建默认克隆请求
     */
    public static CloneRequest of(String url) {
        return CloneRequest.builder()
            .url(url)
            .branch(null)  // 自动检测默认分支
            .targetDirectory(null)  // 使用默认目录
            .timeoutSeconds(60)
            .build();
    }

    /**
     * 创建指定分支的克隆请求
     */
    public static CloneRequest of(String url, String branch) {
        return CloneRequest.builder()
            .url(url)
            .branch(branch)
            .targetDirectory(null)
            .timeoutSeconds(60)
            .build();
    }
}

