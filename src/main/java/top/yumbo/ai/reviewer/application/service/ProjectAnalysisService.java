package top.yumbo.ai.reviewer.application.service;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.input.ProjectAnalysisUseCase;
import top.yumbo.ai.reviewer.application.port.output.AIServicePort;
import top.yumbo.ai.reviewer.application.port.output.CachePort;
import top.yumbo.ai.reviewer.application.port.output.FileSystemPort;
import top.yumbo.ai.reviewer.domain.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目分析服务实现
 * 编排业务流程，协调各个端口完成项目分析
 */
@Slf4j
public class ProjectAnalysisService implements ProjectAnalysisUseCase {

    private final AIServicePort aiServicePort;
    private final CachePort cachePort;
    private final FileSystemPort fileSystemPort;

    // 任务存储
    private final Map<String, AnalysisTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, ReviewReport> reports = new ConcurrentHashMap<>();

    @Inject
    public ProjectAnalysisService(
            AIServicePort aiServicePort,
            CachePort cachePort,
            FileSystemPort fileSystemPort) {
        this.aiServicePort = aiServicePort;
        this.cachePort = cachePort;
        this.fileSystemPort = fileSystemPort;
    }

    @Override
    public AnalysisTask analyzeProject(Project project) {
        log.info("开始分析项目: {}", project.getName());

        // 验证项目有效性
        if (!project.isValid()) {
            throw new IllegalArgumentException("项目信息无效");
        }

        // 创建分析任务
        String taskId = generateTaskId();
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .project(project)
                .configuration(AnalysisConfiguration.builder().build())
                .build();

        task.start();
        tasks.put(taskId, task);

        try {
            // 执行分析流程
            ReviewReport report = performAnalysis(task);
            reports.put(taskId, report);

            task.complete();
            log.info("项目分析完成: taskId={}, score={}", taskId, report.getOverallScore());

        } catch (Exception e) {
            log.error("项目分析失败: taskId={}", taskId, e);
            task.fail(e.getMessage(), e);
            throw new RuntimeException("项目分析失败: " + e.getMessage(), e);
        }

        return task;
    }

    @Override
    public String analyzeProjectAsync(Project project) {
        log.info("开始异步分析项目: {}", project.getName());

        if (!project.isValid()) {
            throw new IllegalArgumentException("项目信息无效");
        }

        // 创建分析任务
        String taskId = generateTaskId();
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .project(project)
                .configuration(AnalysisConfiguration.builder().build())
                .build();

        task.start();
        tasks.put(taskId, task);

        // 异步执行分析
        CompletableFuture.runAsync(() -> {
            try {
                ReviewReport report = performAnalysis(task);
                reports.put(taskId, report);
                task.complete();
                log.info("异步项目分析完成: taskId={}, score={}", taskId, report.getOverallScore());
            } catch (Exception e) {
                log.error("异步项目分析失败: taskId={}", taskId, e);
                task.fail(e.getMessage(), e);
            }
        });

        return taskId;
    }

    @Override
    public AnalysisTask getTaskStatus(String taskId) {
        AnalysisTask task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        return task;
    }

    @Override
    public void cancelTask(String taskId) {
        AnalysisTask task = tasks.get(taskId);
        if (task != null) {
            task.cancel();
            log.info("任务已取消: taskId={}", taskId);
        }
    }

    @Override
    public ReviewReport getAnalysisResult(String taskId) {
        ReviewReport report = reports.get(taskId);
        if (report == null) {
            throw new IllegalArgumentException("分析结果不存在: " + taskId);
        }
        return report;
    }

    /**
     * 执行分析流程
     */
    private ReviewReport performAnalysis(AnalysisTask task) {
        Project project = task.getProject();
        AnalysisProgress progress = task.getProgress();

        // 设置进度
        progress.setTotalSteps(6);

        // 1. 第一阶段：项目概览分析
        progress.updatePhase("项目概览分析");
        progress.updateTask("分析项目结构和技术栈");
        String projectOverview = analyzeProjectOverview(project);
        progress.incrementCompleted();

        // 2. 第二阶段：架构分析
        progress.updatePhase("架构分析");
        progress.updateTask("分析项目架构设计");
        ReviewReport.ArchitectureAnalysis architectureAnalysis = analyzeArchitecture(project);
        progress.incrementCompleted();

        // 3. 第三阶段：代码质量分析
        progress.updatePhase("代码质量分析");
        progress.updateTask("分析代码质量和规范");
        int codeQualityScore = analyzeCodeQuality(project);
        progress.incrementCompleted();

        // 4. 第四阶段：技术债务分析
        progress.updatePhase("技术债务分析");
        progress.updateTask("识别技术债务");
        int technicalDebtScore = analyzeTechnicalDebt(project);
        progress.incrementCompleted();

        // 5. 第五阶段：功能完整性分析
        progress.updatePhase("功能分析");
        progress.updateTask("评估功能完整性");
        int functionalityScore = analyzeFunctionality(project);
        progress.incrementCompleted();

        // 6. 第六阶段：生成报告
        progress.updatePhase("生成报告");
        progress.updateTask("汇总分析结果");

        // 构建报告
        ReviewReport report = ReviewReport.builder()
                .reportId(UUID.randomUUID().toString())
                .projectName(project.getName())
                .projectPath(project.getRootPath().toString())
                .projectOverview(projectOverview)
                .architectureAnalysis(architectureAnalysis)
                .build();

        // 添加维度评分
        report.addDimensionScore("architecture", 80);
        report.addDimensionScore("code_quality", codeQualityScore);
        report.addDimensionScore("technical_debt", technicalDebtScore);
        report.addDimensionScore("functionality", functionalityScore);
        report.addDimensionScore("business_value", 75);
        report.addDimensionScore("test_coverage", 70);

        // 计算总体评分
        report.calculateOverallScore(task.getConfiguration().getDimensionWeights());

        progress.incrementCompleted();

        return report;
    }

    /**
     * 分析项目概览
     */
    private String analyzeProjectOverview(Project project) {
        String cacheKey = "overview:" + project.getName();

        // 检查缓存
        var cached = cachePort.get(cacheKey);
        if (cached.isPresent()) {
            log.debug("使用缓存的项目概览: {}", project.getName());
            return cached.get();
        }

        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下项目的整体情况：\n\n");
        prompt.append("项目名称: ").append(project.getName()).append("\n");
        prompt.append("项目类型: ").append(project.getType().getDisplayName()).append("\n");
        prompt.append("文件数量: ").append(project.getSourceFiles().size()).append("\n");
        prompt.append("代码行数: ").append(project.getTotalLines()).append("\n\n");
        prompt.append("项目结构:\n").append(project.getStructureTree()).append("\n\n");
        prompt.append("请输出：\n");
        prompt.append("1. 项目的核心功能（1-2句话）\n");
        prompt.append("2. 使用的主要技术栈\n");
        prompt.append("3. 项目的整体架构风格\n");

        // 调用AI服务
        String result = aiServicePort.analyze(prompt.toString());

        // 缓存结果
        cachePort.put(cacheKey, result, 3600); // 1小时

        return result;
    }

    /**
     * 分析架构
     */
    private ReviewReport.ArchitectureAnalysis analyzeArchitecture(Project project) {
        String prompt = "分析项目架构设计，评估分层、模块化、耦合度等方面。项目: " + project.getName();
        String result = aiServicePort.analyze(prompt);

        return ReviewReport.ArchitectureAnalysis.builder()
                .architectureStyle("分层架构")
                .analysisResult(result)
                .build();
    }

    /**
     * 分析代码质量
     */
    private int analyzeCodeQuality(Project project) {
        // 简化实现：基于文件数量和代码行数估算
        int fileCount = project.getSourceFiles().size();
        int totalLines = project.getTotalLines();

        // 简单的评分逻辑
        int score = 70; // 基础分
        if (fileCount > 50) score += 5;
        if (totalLines > 5000) score += 5;

        return Math.min(score, 100);
    }

    /**
     * 分析技术债务
     */
    private int analyzeTechnicalDebt(Project project) {
        // 简化实现
        return 75;
    }

    /**
     * 分析功能完整性
     */
    private int analyzeFunctionality(Project project) {
        // 简化实现
        return 80;
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "task-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 关闭服务，释放资源
     */
    public void shutdown() {
        log.info("关闭项目分析服务");
        aiServicePort.shutdown();
        cachePort.close();
        tasks.clear();
        reports.clear();
    }
}
