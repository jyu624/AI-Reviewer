package top.yumbo.ai.reviewer.application.hackathon.cli;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.adapter.storage.local.LocalFileSystemAdapter;
import top.yumbo.ai.reviewer.adapter.repository.git.GitRepositoryAdapter;
import top.yumbo.ai.reviewer.application.hackathon.service.HackathonIntegrationService;
import top.yumbo.ai.reviewer.application.hackathon.service.LeaderboardService;
import top.yumbo.ai.reviewer.application.hackathon.service.TeamManagementService;
import top.yumbo.ai.reviewer.application.port.output.CloneRequest;
import top.yumbo.ai.reviewer.application.port.output.RepositoryPort;
import top.yumbo.ai.reviewer.application.service.ProjectAnalysisService;
import top.yumbo.ai.reviewer.application.service.ReportGenerationService;
import top.yumbo.ai.reviewer.domain.model.*;
import top.yumbo.ai.reviewer.application.hackathon.cli.dto.*;
import top.yumbo.ai.reviewer.application.hackathon.cli.parser.TeamSubmissionParser;
import com.alibaba.fastjson2.JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 黑客松交互式命令行应用
 * 提供友好的交互式界面进行黑客松项目评审
 *
 * <p>职责：
 * <ul>
 *   <li>提供交互式黑客松项目评审流程</li>
 *   <li>管理团队和参赛作品</li>
 *   <li>生成排行榜和评审报告</li>
 * </ul>
 *
 * @author AI-Reviewer Team
 * @version 2.0 (六边形架构重构版)
 * @since 2025-11-13
 */
@Slf4j
public class HackathonInteractiveApp {

    private final HackathonIntegrationService integrationService;
    private final TeamManagementService teamService;
    private final LeaderboardService leaderboardService;
    private final ProjectAnalysisService analysisService;
    private final ReportGenerationService reportService;
    private final LocalFileSystemAdapter fileSystemAdapter;
    private final Scanner scanner;

    public HackathonInteractiveApp(
            HackathonIntegrationService integrationService,
            TeamManagementService teamService,
            LeaderboardService leaderboardService,
            ProjectAnalysisService analysisService,
            ReportGenerationService reportService,
            LocalFileSystemAdapter fileSystemAdapter) {
        this.integrationService = integrationService;
        this.teamService = teamService;
        this.leaderboardService = leaderboardService;
        this.analysisService = analysisService;
        this.reportService = reportService;
        this.fileSystemAdapter = fileSystemAdapter;
        this.scanner = new Scanner(System.in);
    }

    /**
     * 启动交互式界面
     */
    public void start() {
        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> reviewSingleProject();
                case "2" -> reviewBatchProjects();
                case "3" -> manageTeams();
                case "4" -> viewLeaderboard();
                case "5" -> exportResults();
                case "6" -> showHelp();
                case "0" -> running = false;
                default -> System.out.println("❌ 无效选项，请重新选择");
            }
        }

        System.out.println("\n👋 评审完成！感谢使用黑客松评审工具!");
        scanner.close();
    }

    /**
     * 打印欢迎横幅
     */
    private void printWelcomeBanner() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║                                                ║");
        System.out.println("║        🏆 黑客松项目评审工具 🏆              ║");
        System.out.println("║                                                ║");
        System.out.println("║        基于AI的智能评分系统                   ║");
        System.out.println("║                                                ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }

    /**
     * 打印主菜单
     */
    private void printMenu() {
        System.out.println("\n📋 主菜单：");
        System.out.println("  1. 🔍 评审单个项目");
        System.out.println("  2. 📦 批量评审项目");
        System.out.println("  3. 👥 管理团队");
        System.out.println("  4. 🏅 查看排行榜");
        System.out.println("  5. 📊 导出结果");
        System.out.println("  6. ❓ 帮助");
        System.out.println("  0. 🚪 退出");
        System.out.print("\n请选择操作 [0-6]: ");
    }

    /**
     * 评审单个项目
     */
    private void reviewSingleProject() {
        System.out.println("\n🔍 === 评审单个项目 ===\n");

        // 1. 输入团队名称
        System.out.print("📝 团队名称: ");
        String teamName = scanner.nextLine().trim();

        // 2. 选择输入方式
        System.out.println("\n选择项目来源:");
        System.out.println("  1. GitHub/Gitee/Gitlab/ip URL");
        System.out.println("  2. 本地目录");
        System.out.print("选择 [1-2]: ");

        String sourceChoice = scanner.nextLine().trim();
        String url = null;
        String directory = null;

        switch (sourceChoice) {
            case "1" -> {
                System.out.print("Git URL: ");
                url = scanner.nextLine().trim();
            }
            case "2" -> {
                System.out.print("本地目录: ");
                directory = scanner.nextLine().trim();
            }

            default -> {
                System.out.println("❌ 无效选项");
                return;
            }
        }

        // 3. 执行评审
        try {
            System.out.println("\n⏳ 正在评审项目...");

            Path projectPath;
            if (url != null) {
                projectPath = cloneProject(url);
            } else {
                projectPath = Paths.get(directory);
                if (!Files.exists(projectPath)) {
                    System.out.println("❌ 目录不存在");
                    return;
                }
            }

            // 扫描和分析
            List<SourceFile> files = fileSystemAdapter.scanProjectFiles(projectPath);
            Project project = Project.builder()
                    .name(teamName)
                    .rootPath(projectPath)
                    .type(detectProjectType(files))
                    .sourceFiles(files)
                    .build();

            AnalysisTask task = analysisService.analyzeProject(project);

            if (task.isCompleted()) {
                ReviewReport report = analysisService.getAnalysisResult(task.getTaskId());
                printReviewResult(teamName, report);

                // 保存结果
                System.out.print("\n是否保存报告？[Y/n]: ");
                String save = scanner.nextLine().trim();
                if (save.isEmpty() || save.equalsIgnoreCase("Y")) {
                    saveReport(teamName, report);
                }
            } else {
                System.out.println("❌ 评审失败: " + task.getErrorMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ 评审失败: " + e.getMessage());
            log.error("Review failed", e);
        }
    }

    /**
     * 批量评审项目
     */
    private void reviewBatchProjects() {
        System.out.println("\n📦 === 批量评审项目 ===\n");
        System.out.println("支持的文件格式:");
        System.out.println("  • CSV: team_name,repo_url,contact_email,submission_time");
        System.out.println("  • JSON: {\"teams\": [{\"teamName\": \"...\", \"repoUrl\": \"...\"}]}");
        System.out.println("  • TXT: 每行一个 URL 或 团队名:URL");
        System.out.print("\n文件路径: ");

        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("❌ 文件路径不能为空");
            return;
        }

        Path inputFile = Paths.get(filePath);
        if (!Files.exists(inputFile)) {
            System.out.println("❌ 文件不存在: " + filePath);
            return;
        }

        try {
            // 解析团队提交文件
            System.out.println("\n⏳ 正在解析提交文件...");
            List<TeamSubmission> submissions = TeamSubmissionParser.parse(inputFile);
            System.out.println("✅ 找到 " + submissions.size() + " 个团队提交");

            if (submissions.isEmpty()) {
                System.out.println("❌ 没有找到有效的团队提交");
                return;
            }

            // 确认是否继续
            System.out.print("\n是否开始批量评审？[Y/n]: ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.isEmpty() && !confirm.equalsIgnoreCase("Y")) {
                System.out.println("❌ 已取消");
                return;
            }

            // 选择并行度
            System.out.print("\n并行评审线程数 [1-10, 默认4]: ");
            String parallelInput = scanner.nextLine().trim();
            int parallelism = parseParallelism(parallelInput, 4);

            // 执行批量评审
            System.out.println("\n⏳ 开始批量评审（并行度: " + parallelism + "）...\n");
            BatchReviewResult result = executeBatchReview(submissions, parallelism);

            // 显示结果摘要
            printBatchReviewSummary(result);

            // 询问是否导出详细报告
            System.out.print("\n是否导出详细报告？[Y/n]: ");
            String exportChoice = scanner.nextLine().trim();
            if (exportChoice.isEmpty() || exportChoice.equalsIgnoreCase("Y")) {
                exportBatchReviewReport(result);
            }

        } catch (IOException e) {
            System.out.println("❌ 读取文件失败: " + e.getMessage());
            log.error("Failed to read batch file", e);
        } catch (Exception e) {
            System.out.println("❌ 批量评审失败: " + e.getMessage());
            log.error("Batch review failed", e);
        }
    }

    /**
     * 管理团队
     */
    private void manageTeams() {
        System.out.println("\n👥 === 管理团队 ===\n");
        System.out.println("  1. 注册新团队");
        System.out.println("  2. 查看团队列表");
        System.out.println("  3. 查看团队详情");
        System.out.print("选择 [1-3]: ");

        String choice = scanner.nextLine().trim();
        // TODO: 实现团队管理逻辑
        System.out.println("💡 团队管理功能正在开发中...");
    }

    /**
     * 查看排行榜
     */
    private void viewLeaderboard() {
        System.out.println("\n🏅 === 排行榜 ===\n");
        // TODO: 实现排行榜显示逻辑
        System.out.println("💡 排行榜功能正在开发中...");
    }

    /**
     * 导出结果
     */
    private void exportResults() {
        System.out.println("\n📊 === 导出结果 ===\n");
        System.out.println("选择导出格式:");
        System.out.println("  1. JSON");
        System.out.println("  2. CSV");
        System.out.println("  3. Excel");
        System.out.print("选择 [1-3]: ");

        String choice = scanner.nextLine().trim();
        // TODO: 实现结果导出逻辑
        System.out.println("💡 导出功能正在开发中...");
    }

    /**
     * 显示帮助
     */
    private void showHelp() {
        System.out.println("\n❓ === 帮助信息 ===\n");
        System.out.println("🏆 黑客松项目评审工具");
        System.out.println("\n主要功能:");
        System.out.println("  • 支持GitHub/Gitee仓库自动克隆");
        System.out.println("  • 基于AI的智能评分");
        System.out.println("  • 多维度评分（代码质量、创新性、完整性、文档）");
        System.out.println("  • 自动生成排行榜");
        System.out.println("  • 团队管理和结果导出\n");

        System.out.println("评分维度:");
        System.out.println("  • 代码质量 (40%)");
        System.out.println("  • 创新性 (30%)");
        System.out.println("  • 完整性 (20%)");
        System.out.println("  • 文档质量 (10%)\n");
    }

    /**
     * 克隆项目
     */
    private Path cloneProject(String url) throws RepositoryPort.RepositoryException {
        System.out.println("⏳ 正在克隆项目...");
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "hackathon-repos");
        RepositoryPort repoPort = new GitRepositoryAdapter(tempDir);

        CloneRequest request = CloneRequest.builder()
                .url(url)
                .timeoutSeconds(300)
                .build();

        Path projectPath = repoPort.cloneRepository(request);
        System.out.println("✅ 克隆完成");
        return projectPath;
    }

    /**
     * 检测项目类型
     */
    private ProjectType detectProjectType(List<SourceFile> files) {
        int javaCount = 0, pythonCount = 0, jsCount = 0;
        for (SourceFile file : files) {
            switch (file.getProjectType()) {
                case JAVA -> javaCount++;
                case PYTHON -> pythonCount++;
                case JAVASCRIPT, TYPESCRIPT -> jsCount++;
            }
        }

        int max = Math.max(javaCount, Math.max(pythonCount, jsCount));
        if (max == javaCount) return ProjectType.JAVA;
        if (max == pythonCount) return ProjectType.PYTHON;
        return ProjectType.JAVASCRIPT;
    }

    /**
     * 打印评审结果
     */
    private void printReviewResult(String teamName, ReviewReport report) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 黑客松评审结果");
        System.out.println("=".repeat(60));
        System.out.println("团队: " + teamName);
        System.out.println("项目: " + report.getProjectName());
        System.out.println("总分: " + report.getOverallScore() + "/100 (" + report.getGrade() + ")");

        if (report.getDimensionScores() != null) {
            System.out.println("\n维度评分:");
            report.getDimensionScores().forEach((dimension, score) ->
                System.out.println("  • " + dimension + ": " + score + "/100")
            );
        }

        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * 保存报告
     */
    private void saveReport(String teamName, ReviewReport report) {
        try {
            String fileName = teamName.replaceAll("[^a-zA-Z0-9-_]", "_") + "-report";
            Path mdPath = Paths.get(fileName + ".md");
            Path jsonPath = Paths.get(fileName + ".json");

            reportService.saveReport(report, mdPath, "markdown");
            reportService.saveReport(report, jsonPath, "json");

            System.out.println("✅ 报告已保存:");
            System.out.println("  - " + mdPath.toAbsolutePath());
            System.out.println("  - " + jsonPath.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("❌ 保存报告失败: " + e.getMessage());
            log.error("Failed to save report", e);
        }
    }

    /**
     * 执行批量评审
     */
    private BatchReviewResult executeBatchReview(List<TeamSubmission> submissions, int parallelism) {
        long startTime = System.currentTimeMillis();
        List<ReviewResult> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (TeamSubmission submission : submissions) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ReviewResult result = reviewSingleSubmission(submission);
                    results.add(result);

                    if (result.isSuccess()) {
                        success.incrementAndGet();
                        System.out.println("✅ [" + completed.incrementAndGet() + "/" + submissions.size() + "] "
                            + submission.getTeamName() + " - 评审完成 (得分: " + result.getReport().getOverallScore() + ")");
                    } else {
                        failure.incrementAndGet();
                        System.out.println("❌ [" + completed.incrementAndGet() + "/" + submissions.size() + "] "
                            + submission.getTeamName() + " - 评审失败: " + result.getErrorMessage());
                    }
                } catch (Exception e) {
                    failure.incrementAndGet();
                    results.add(ReviewResult.failure(submission, e.getMessage()));
                    System.out.println("❌ [" + completed.incrementAndGet() + "/" + submissions.size() + "] "
                        + submission.getTeamName() + " - 异常: " + e.getMessage());
                    log.error("Review failed for team: " + submission.getTeamName(), e);
                }
            }, executor);

            futures.add(future);
        }

        // 等待所有任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            log.error("Error waiting for batch review completion", e);
        } finally {
            executor.shutdown();
        }

        long duration = System.currentTimeMillis() - startTime;

        return BatchReviewResult.builder()
            .totalCount(submissions.size())
            .successCount(success.get())
            .failureCount(failure.get())
            .duration(duration)
            .results(results)
            .build();
    }

    /**
     * 评审单个提交
     */
    private ReviewResult reviewSingleSubmission(TeamSubmission submission) {
        try {
            Path projectPath;

            // 克隆或使用本地路径
            if (submission.getRepoUrl().startsWith("http://") ||
                submission.getRepoUrl().startsWith("https://") ||
                submission.getRepoUrl().startsWith("git@")) {
                projectPath = cloneProject(submission.getRepoUrl());
            } else {
                projectPath = Paths.get(submission.getRepoUrl());
                if (!Files.exists(projectPath)) {
                    return ReviewResult.failure(submission, "本地路径不存在: " + submission.getRepoUrl());
                }
            }

            // 扫描和分析
            List<SourceFile> files = fileSystemAdapter.scanProjectFiles(projectPath);

            if (files.isEmpty()) {
                return ReviewResult.failure(submission, "没有找到可分析的源代码文件");
            }

            Project project = Project.builder()
                .name(submission.getTeamName())
                .rootPath(projectPath)
                .type(detectProjectType(files))
                .sourceFiles(files)
                .build();

            AnalysisTask task = analysisService.analyzeProject(project);

            if (task.isCompleted()) {
                ReviewReport report = analysisService.getAnalysisResult(task.getTaskId());
                return ReviewResult.success(submission, report);
            } else {
                return ReviewResult.failure(submission, task.getErrorMessage() != null ?
                    task.getErrorMessage() : "分析未完成");
            }

        } catch (Exception e) {
            log.error("Failed to review submission: " + submission.getTeamName(), e);
            return ReviewResult.failure(submission, e.getMessage());
        }
    }

    /**
     * 打印批量评审摘要
     */
    private void printBatchReviewSummary(BatchReviewResult result) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 批量评审完成");
        System.out.println("=".repeat(60));
        System.out.println("总数: " + result.getTotalCount());
        System.out.println("成功: " + result.getSuccessCount() + " (" +
            (result.getTotalCount() > 0 ? (result.getSuccessCount() * 100 / result.getTotalCount()) : 0) + "%)");
        System.out.println("失败: " + result.getFailureCount());
        System.out.println("耗时: " + formatDuration(result.getDuration()));

        // 显示成功的团队排名（按分数降序）
        List<ReviewResult> successResults = result.getResults().stream()
            .filter(ReviewResult::isSuccess)
            .sorted((a, b) -> Integer.compare(
                b.getReport().getOverallScore(),
                a.getReport().getOverallScore()))
            .toList();

        if (!successResults.isEmpty()) {
            System.out.println("\n🏆 排行榜（前10名）:");
            int rank = 1;
            for (ReviewResult r : successResults.stream().limit(10).toList()) {
                System.out.printf("  %2d. %-30s 得分: %d (%s)\n",
                    rank++,
                    r.getSubmission().getTeamName(),
                    r.getReport().getOverallScore(),
                    r.getReport().getGrade());
            }
        }

        // 显示失败的团队
        List<ReviewResult> failedResults = result.getResults().stream()
            .filter(r -> !r.isSuccess())
            .toList();

        if (!failedResults.isEmpty()) {
            System.out.println("\n❌ 失败的团队:");
            for (ReviewResult r : failedResults) {
                System.out.println("  • " + r.getSubmission().getTeamName() +
                    ": " + r.getErrorMessage());
            }
        }

        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * 导出批量评审报告
     */
    private void exportBatchReviewReport(BatchReviewResult result) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String baseFileName = "batch-review-" + timestamp;

            // 导出汇总报告（Markdown）
            Path summaryPath = Paths.get(baseFileName + "-summary.md");
            String summaryMd = generateBatchSummaryMarkdown(result);
            Files.writeString(summaryPath, summaryMd);
            System.out.println("✅ 汇总报告已保存: " + summaryPath.toAbsolutePath());

            // 导出详细结果（JSON）
            Path jsonPath = Paths.get(baseFileName + "-details.json");
            String jsonContent = JSON.toJSONString(result);
            Files.writeString(jsonPath, jsonContent);
            System.out.println("✅ 详细结果已保存: " + jsonPath.toAbsolutePath());

            // 导出CSV格式排行榜
            Path csvPath = Paths.get(baseFileName + "-leaderboard.csv");
            String csvContent = generateLeaderboardCSV(result);
            Files.writeString(csvPath, csvContent);
            System.out.println("✅ 排行榜已保存: " + csvPath.toAbsolutePath());

            // 为每个成功的团队生成独立报告
            System.out.print("\n是否为每个团队生成独立报告？[Y/n]: ");
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty() || choice.equalsIgnoreCase("Y")) {
                exportIndividualReports(result, timestamp);
            }

        } catch (Exception e) {
            System.out.println("❌ 导出报告失败: " + e.getMessage());
            log.error("Failed to export batch review report", e);
        }
    }

    /**
     * 导出各团队独立报告
     */
    private void exportIndividualReports(BatchReviewResult result, String timestamp) {
        try {
            Path reportsDir = Paths.get("batch-reports-" + timestamp);
            Files.createDirectories(reportsDir);

            int count = 0;
            for (ReviewResult r : result.getResults()) {
                if (r.isSuccess()) {
                    String teamFileName = r.getSubmission().getTeamName()
                        .replaceAll("[^a-zA-Z0-9-_]", "_");
                    Path teamReportPath = reportsDir.resolve(teamFileName + "-report.md");

                    reportService.saveReport(r.getReport(), teamReportPath, "markdown");
                    count++;
                }
            }

            System.out.println("✅ 已生成 " + count + " 份独立报告，保存在: " + reportsDir.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("❌ 生成独立报告失败: " + e.getMessage());
            log.error("Failed to export individual reports", e);
        }
    }

    /**
     * 生成批量评审汇总Markdown
     */
    private String generateBatchSummaryMarkdown(BatchReviewResult result) {
        StringBuilder md = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        md.append("# 黑客松批量评审报告\n\n");
        md.append("**生成时间**: ").append(timestamp).append("\n\n");

        md.append("## 📊 评审概况\n\n");
        md.append("| 指标 | 数值 |\n");
        md.append("|------|------|\n");
        md.append("| 总数 | ").append(result.getTotalCount()).append(" |\n");
        md.append("| 成功 | ").append(result.getSuccessCount()).append(" |\n");
        md.append("| 失败 | ").append(result.getFailureCount()).append(" |\n");
        md.append("| 成功率 | ").append(result.getTotalCount() > 0 ?
            String.format("%.1f%%", result.getSuccessCount() * 100.0 / result.getTotalCount()) : "0%")
            .append(" |\n");
        md.append("| 耗时 | ").append(formatDuration(result.getDuration())).append(" |\n\n");

        // 排行榜
        List<ReviewResult> successResults = result.getResults().stream()
            .filter(ReviewResult::isSuccess)
            .sorted((a, b) -> Integer.compare(
                b.getReport().getOverallScore(),
                a.getReport().getOverallScore()))
            .toList();

        if (!successResults.isEmpty()) {
            md.append("## 🏆 排行榜\n\n");
            md.append("| 排名 | 团队名称 | 总分 | 等级 | 代码质量 | 创新性 | 完整性 | 文档 |\n");
            md.append("|------|----------|------|------|----------|--------|--------|------|\n");

            int rank = 1;
            for (ReviewResult r : successResults) {
                ReviewReport report = r.getReport();
                Map<String, Integer> scores = report.getDimensionScores();

                md.append(String.format("| %d | %s | %d | %s | %d | %d | %d | %d |\n",
                    rank++,
                    r.getSubmission().getTeamName(),
                    report.getOverallScore(),
                    report.getGrade(),
                    scores.getOrDefault("代码质量", 0),
                    scores.getOrDefault("创新性", 0),
                    scores.getOrDefault("完整性", 0),
                    scores.getOrDefault("文档质量", 0)
                ));
            }
            md.append("\n");
        }

        // 失败的团队
        List<ReviewResult> failedResults = result.getResults().stream()
            .filter(r -> !r.isSuccess())
            .toList();

        if (!failedResults.isEmpty()) {
            md.append("## ❌ 评审失败的团队\n\n");
            md.append("| 团队名称 | 错误信息 |\n");
            md.append("|----------|----------|\n");

            for (ReviewResult r : failedResults) {
                md.append("| ").append(r.getSubmission().getTeamName())
                    .append(" | ").append(r.getErrorMessage()).append(" |\n");
            }
            md.append("\n");
        }

        md.append("---\n");
        md.append("*报告由 AI-Reviewer 自动生成*\n");

        return md.toString();
    }

    /**
     * 生成排行榜CSV
     */
    private String generateLeaderboardCSV(BatchReviewResult result) {
        StringBuilder csv = new StringBuilder();
        csv.append("排名,团队名称,总分,等级,代码质量,创新性,完整性,文档质量,联系邮箱\n");

        List<ReviewResult> successResults = result.getResults().stream()
            .filter(ReviewResult::isSuccess)
            .sorted((a, b) -> Integer.compare(
                b.getReport().getOverallScore(),
                a.getReport().getOverallScore()))
            .toList();

        int rank = 1;
        for (ReviewResult r : successResults) {
            ReviewReport report = r.getReport();
            Map<String, Integer> scores = report.getDimensionScores();

            csv.append(rank++).append(",")
                .append(r.getSubmission().getTeamName()).append(",")
                .append(report.getOverallScore()).append(",")
                .append(report.getGrade()).append(",")
                .append(scores.getOrDefault("代码质量", 0)).append(",")
                .append(scores.getOrDefault("创新性", 0)).append(",")
                .append(scores.getOrDefault("完整性", 0)).append(",")
                .append(scores.getOrDefault("文档质量", 0)).append(",")
                .append(r.getSubmission().getContactEmail() != null ?
                    r.getSubmission().getContactEmail() : "")
                .append("\n");
        }

        return csv.toString();
    }

    /**
     * 解析并行度参数
     */
    private int parseParallelism(String input, int defaultValue) {
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            int value = Integer.parseInt(input.trim());
            if (value < 1) return 1;
            return Math.min(value, 10);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 格式化持续时间
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 主程序入口（用于独立运行）
     */
    public static void main(String[] args) {
        System.out.println("💡 提示: 交互式应用需要完整的依赖注入容器");
        System.out.println("请使用 HackathonCommandLineApp 的命令行模式");
        System.out.println("或者通过 Guice 注入器获取 HackathonInteractiveApp 实例");
    }
}

