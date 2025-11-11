package top.yumbo.ai.reviewer.adapter.input.hackathon;

import org.junit.jupiter.api.*;
import top.yumbo.ai.reviewer.adapter.input.hackathon.adapter.output.ai.DeepSeekAIAdapter;
import top.yumbo.ai.reviewer.adapter.input.hackathon.adapter.output.filesystem.LocalFileSystemAdapter;
import top.yumbo.ai.reviewer.adapter.input.hackathon.adapter.output.gitee.GiteeAdapter;
import top.yumbo.ai.reviewer.adapter.input.hackathon.application.CodeReviewOrchestrator;
import top.yumbo.ai.reviewer.adapter.input.hackathon.domain.model.CodeReviewRequest;
import top.yumbo.ai.reviewer.adapter.input.hackathon.domain.model.CodeReviewResult;
import top.yumbo.ai.reviewer.adapter.input.hackathon.domain.port.GitHubPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gitee 集成端到端测试
 *
 * 演示使用 Gitee（码云）完成完整的代码审查流程
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GiteeIntegrationEndToEndTest {

    private static CodeReviewOrchestrator orchestrator;
    private static Path workingDirectory;

    // 使用一个小的开源 Java 项目作为测试
    private static final String TEST_REPO = "https://gitee.com/dromara/hutool.git";

    @BeforeAll
    static void setUp() throws IOException {
        workingDirectory = Paths.get("target/test-gitee-e2e");
        Files.createDirectories(workingDirectory);

        // 初始化适配器
        GiteeAdapter giteeAdapter = new GiteeAdapter(workingDirectory);
        LocalFileSystemAdapter fileSystemAdapter = new LocalFileSystemAdapter();

        // 检查是否有 DeepSeek API Key
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("⚠️  未配置 DEEPSEEK_API_KEY 环境变量，AI 评审将跳过");
        }

        DeepSeekAIAdapter aiAdapter = new DeepSeekAIAdapter(
            apiKey != null ? apiKey : "dummy-key",
            "deepseek-chat",
            0.7
        );

        // 创建编排器
        orchestrator = new CodeReviewOrchestrator(giteeAdapter, fileSystemAdapter, aiAdapter);
    }

    @AfterAll
    static void tearDown() {
        // 清理测试目录
        if (workingDirectory != null && Files.exists(workingDirectory)) {
            try {
                Files.walk(workingDirectory)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException e) {
                System.err.println("清理测试目录失败: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试完整的 Gitee 代码审查流程")
    void testCompleteGiteeWorkflow() throws Exception {
        System.out.println("\n========================================");
        System.out.println("开始 Gitee 集成测试");
        System.out.println("========================================\n");

        // 创建审查请求
        CodeReviewRequest request = CodeReviewRequest.builder()
                .repositoryUrl(TEST_REPO)
                .branch(null)  // 使用默认分支
                .includePatterns(Arrays.asList(
                        "**/*.java",
                        "**/*.md",
                        "**/pom.xml"
                ))
                .excludePatterns(Arrays.asList(
                        "**/target/**",
                        "**/.git/**",
                        "**/test/**"
                ))
                .focusAreas(Arrays.asList(
                        "代码质量",
                        "设计模式",
                        "文档完整性"
                ))
                .build();

        System.out.println("✓ 创建审查请求");
        System.out.println("  - 仓库: " + TEST_REPO);
        System.out.println("  - 分支: 默认分支");
        System.out.println("  - 文件模式: " + request.getIncludePatterns());
        System.out.println();

        // 执行审查
        System.out.println("开始执行代码审查...");
        CodeReviewResult result = orchestrator.reviewCode(request);

        // 验证结果
        assertNotNull(result, "审查结果不应为空");
        System.out.println("\n✓ 审查完成！");
        System.out.println();

        // 显示结果
        System.out.println("========================================");
        System.out.println("审查结果统计");
        System.out.println("========================================");
        System.out.println("仓库名称: " + result.getRepositoryName());
        System.out.println("总文件数: " + result.getTotalFiles());
        System.out.println("已审查文件数: " + result.getReviewedFiles());
        System.out.println("总代码行数: " + result.getTotalLines());
        System.out.println("审查开始时间: " + result.getStartTime());
        System.out.println("审查结束时间: " + result.getEndTime());
        System.out.println("耗时(秒): " + result.getDurationSeconds());

        if (result.getGithubMetrics() != null) {
            GitHubPort.GitHubMetrics metrics = result.getGithubMetrics();
            System.out.println("\n========================================");
            System.out.println("仓库指标");
            System.out.println("========================================");
            System.out.println("拥有者: " + metrics.getOwnerName());
            System.out.println("提交数: " + metrics.getCommitCount());
            System.out.println("贡献者数: " + metrics.getContributorCount());
            System.out.println("分支列表: " + metrics.getBranches());
            System.out.println("有 README: " + (metrics.isHasReadme() ? "是" : "否"));
            System.out.println("有 LICENSE: " + (metrics.isHasLicense() ? "是" : "否"));
        }

        // 验证基本数据
        assertTrue(result.getTotalFiles() > 0, "应该至少有一个文件");
        assertNotNull(result.getRepositoryName(), "仓库名称不应为空");

        System.out.println("\n========================================");
        System.out.println("Gitee 集成测试通过！✓");
        System.out.println("========================================\n");
    }

    @Test
    @Order(2)
    @DisplayName("测试 Gitee 仓库指标获取")
    void testGetGiteeMetrics() throws Exception {
        System.out.println("\n========================================");
        System.out.println("测试 Gitee 仓库指标获取");
        System.out.println("========================================\n");

        GiteeAdapter giteeAdapter = new GiteeAdapter(workingDirectory);
        GitHubPort.GitHubMetrics metrics = giteeAdapter.getRepositoryMetrics(TEST_REPO);

        assertNotNull(metrics, "指标不应为空");
        assertTrue(metrics.getCommitCount() > 0, "应该有提交记录");
        assertTrue(metrics.getContributorCount() > 0, "应该有贡献者");
        assertNotNull(metrics.getBranches(), "分支列表不应为空");

        System.out.println("✓ 成功获取 Gitee 仓库指标");
        System.out.println("  - 仓库: " + metrics.getRepositoryName());
        System.out.println("  - 拥有者: " + metrics.getOwnerName());
        System.out.println("  - 提交数: " + metrics.getCommitCount());
        System.out.println("  - 贡献者: " + metrics.getContributorCount());
        System.out.println("  - 分支: " + metrics.getBranches());
        System.out.println();
    }

    @Test
    @Order(3)
    @DisplayName("测试多个 Gitee 仓库对比")
    void testMultipleGiteeRepositories() {
        System.out.println("\n========================================");
        System.out.println("测试多个 Gitee 仓库对比");
        System.out.println("========================================\n");

        List<String> repos = Arrays.asList(
                "https://gitee.com/dromara/hutool.git"
                // 可以添加更多仓库进行对比
        );

        GiteeAdapter giteeAdapter = new GiteeAdapter(workingDirectory);

        for (String repo : repos) {
            try {
                System.out.println("检查仓库: " + repo);
                boolean accessible = giteeAdapter.isRepositoryAccessible(repo);
                System.out.println("  可访问: " + (accessible ? "是" : "否"));

                if (accessible) {
                    String defaultBranch = giteeAdapter.getDefaultBranch(repo);
                    System.out.println("  默认分支: " + defaultBranch);
                }
                System.out.println();

            } catch (Exception e) {
                System.err.println("  ✗ 检查失败: " + e.getMessage());
            }
        }

        System.out.println("✓ 仓库对比完成\n");
    }
}

