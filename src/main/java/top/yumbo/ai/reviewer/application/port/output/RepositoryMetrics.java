package top.yumbo.ai.reviewer.application.port.output;

import lombok.Builder;

/**
 * 仓库指标值对象
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
@Builder
public record RepositoryMetrics(
    String repositoryName,
    String owner,
    int commitCount,
    int contributorCount,
    int starCount,
    boolean hasReadme,
    boolean hasLicense,
    String primaryLanguage,
    long sizeInKB
) {
    /**
     * 创建空指标
     */
    public static RepositoryMetrics empty() {
        return RepositoryMetrics.builder()
            .repositoryName("")
            .owner("")
            .commitCount(0)
            .contributorCount(0)
            .starCount(0)
            .hasReadme(false)
            .hasLicense(false)
            .primaryLanguage("Unknown")
            .sizeInKB(0)
            .build();
    }
}

