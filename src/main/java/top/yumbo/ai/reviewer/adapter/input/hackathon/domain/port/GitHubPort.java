package top.yumbo.ai.reviewer.adapter.input.hackathon.domain.port;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GitHub 服务端口
 *
 * 定义 GitHub 操作的接口，用于代码克隆、仓库信息获取等
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
public interface GitHubPort {

    /**
     * 克隆 GitHub 仓库
     *
     * @param githubUrl GitHub 仓库 URL
     * @param branch 分支名称，null 则使用默认分支
     * @return 克隆到本地的路径
     * @throws GitHubException 如果克隆失败
     */
    Path cloneRepository(String githubUrl, String branch) throws GitHubException;

    /**
     * 克隆指定 commit 的代码
     *
     * @param githubUrl GitHub 仓库 URL
     * @param commitHash commit 哈希值
     * @return 克隆到本地的路径
     * @throws GitHubException 如果克隆失败
     */
    Path cloneRepositoryAtCommit(String githubUrl, String commitHash) throws GitHubException;

    /**
     * 获取仓库指标
     *
     * @param githubUrl GitHub 仓库 URL
     * @return GitHub 指标
     * @throws GitHubException 如果获取失败
     */
    GitHubMetrics getRepositoryMetrics(String githubUrl) throws GitHubException;

    /**
     * 验证 GitHub URL 是否有效
     *
     * @param githubUrl GitHub 仓库 URL
     * @return true 如果仓库存在且可访问
     */
    boolean isRepositoryAccessible(String githubUrl);

    /**
     * 获取仓库大小（字节）
     *
     * @param githubUrl GitHub 仓库 URL
     * @return 仓库大小
     * @throws GitHubException 如果获取失败
     */
    long getRepositorySize(String githubUrl) throws GitHubException;

    /**
     * 检查仓库是否包含指定文件
     *
     * @param githubUrl GitHub 仓库 URL
     * @param fileName 文件名
     * @return true 如果文件存在
     */
    boolean hasFile(String githubUrl, String fileName);

    /**
     * 获取仓库的默认分支名
     *
     * @param githubUrl GitHub 仓库 URL
     * @return 默认分支名（如 main, master）
     * @throws GitHubException 如果获取失败
     */
    String getDefaultBranch(String githubUrl) throws GitHubException;

    /**
     * GitHub 指标数据
     */
    class GitHubMetrics {
        private final String repositoryName;
        private final String ownerName;
        private final int commitCount;
        private final int contributorCount;
        private final LocalDateTime firstCommitDate;
        private final LocalDateTime lastCommitDate;
        private final List<String> branches;
        private final boolean hasReadme;
        private final boolean hasLicense;
        private final boolean hasGitHubActions;
        private final boolean hasIssues;
        private final boolean hasPullRequests;
        private final int starsCount;
        private final int forksCount;

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        private GitHubMetrics(Builder builder) {
            this.repositoryName = builder.repositoryName;
            this.ownerName = builder.ownerName;
            this.commitCount = builder.commitCount;
            this.contributorCount = builder.contributorCount;
            this.firstCommitDate = builder.firstCommitDate;
            this.lastCommitDate = builder.lastCommitDate;
            this.branches = builder.branches;
            this.hasReadme = builder.hasReadme;
            this.hasLicense = builder.hasLicense;
            this.hasGitHubActions = builder.hasGitHubActions;
            this.hasIssues = builder.hasIssues;
            this.hasPullRequests = builder.hasPullRequests;
            this.starsCount = builder.starsCount;
            this.forksCount = builder.forksCount;
        }

        // Getters
        public String getRepositoryName() { return repositoryName; }
        public String getOwnerName() { return ownerName; }
        public int getCommitCount() { return commitCount; }
        public int getContributorCount() { return contributorCount; }
        public LocalDateTime getFirstCommitDate() { return firstCommitDate; }
        public LocalDateTime getLastCommitDate() { return lastCommitDate; }
        public List<String> getBranches() { return branches; }
        public boolean isHasReadme() { return hasReadme; }
        public boolean isHasLicense() { return hasLicense; }
        public boolean isHasGitHubActions() { return hasGitHubActions; }
        public boolean isHasIssues() { return hasIssues; }
        public boolean isHasPullRequests() { return hasPullRequests; }
        public int getStarsCount() { return starsCount; }
        public int getForksCount() { return forksCount; }

        public static class Builder {
            private String repositoryName;
            private String ownerName;
            private int commitCount;
            private int contributorCount;
            private LocalDateTime firstCommitDate;
            private LocalDateTime lastCommitDate;
            private List<String> branches;
            private boolean hasReadme;
            private boolean hasLicense;
            private boolean hasGitHubActions;
            private boolean hasIssues;
            private boolean hasPullRequests;
            private int starsCount;
            private int forksCount;

            public Builder repositoryName(String repositoryName) {
                this.repositoryName = repositoryName;
                return this;
            }

            public Builder ownerName(String ownerName) {
                this.ownerName = ownerName;
                return this;
            }

            public Builder commitCount(int commitCount) {
                this.commitCount = commitCount;
                return this;
            }

            public Builder contributorCount(int contributorCount) {
                this.contributorCount = contributorCount;
                return this;
            }

            public Builder firstCommitDate(LocalDateTime firstCommitDate) {
                this.firstCommitDate = firstCommitDate;
                return this;
            }

            public Builder lastCommitDate(LocalDateTime lastCommitDate) {
                this.lastCommitDate = lastCommitDate;
                return this;
            }

            public Builder branches(List<String> branches) {
                this.branches = branches;
                return this;
            }

            public Builder hasReadme(boolean hasReadme) {
                this.hasReadme = hasReadme;
                return this;
            }

            public Builder hasLicense(boolean hasLicense) {
                this.hasLicense = hasLicense;
                return this;
            }

            public Builder hasGitHubActions(boolean hasGitHubActions) {
                this.hasGitHubActions = hasGitHubActions;
                return this;
            }

            public Builder hasIssues(boolean hasIssues) {
                this.hasIssues = hasIssues;
                return this;
            }

            public Builder hasPullRequests(boolean hasPullRequests) {
                this.hasPullRequests = hasPullRequests;
                return this;
            }

            public Builder starsCount(int starsCount) {
                this.starsCount = starsCount;
                return this;
            }

            public Builder forksCount(int forksCount) {
                this.forksCount = forksCount;
                return this;
            }

            public GitHubMetrics build() {
                return new GitHubMetrics(this);
            }
        }

        @Override
        public String toString() {
            return String.format(
                "GitHubMetrics{repo=%s/%s, commits=%d, contributors=%d, stars=%d}",
                ownerName, repositoryName, commitCount, contributorCount, starsCount
            );
        }
    }

    /**
     * GitHub 异常
     */
    class GitHubException extends Exception {
        public GitHubException(String message) {
            super(message);
        }

        public GitHubException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

