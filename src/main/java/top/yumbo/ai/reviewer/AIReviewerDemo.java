package top.yumbo.ai.reviewer;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.config.Config;
import top.yumbo.ai.reviewer.entity.AnalysisResult;
import top.yumbo.ai.reviewer.exception.AnalysisException;
import top.yumbo.ai.reviewer.report.ReportBuilder;

import java.io.IOException;

/**
 * AI Reviewer 演示类
 */
@Slf4j
public class AIReviewerDemo {

    public static void main(String[] args) {
        log.info("=== AI Reviewer 演示 ===");

        try {
            // 演示1: 分析当前项目
            analyzeCurrentProject();

            // 演示2: 分析外部项目（需要配置路径）
            // analyzeExternalProject("/path/to/external/project");

        } catch (Exception e) {
            log.error("演示执行失败", e);
            System.err.println("错误: " + e.getMessage());
        }
    }

    /**
     * 分析当前项目
     */
    private static void analyzeCurrentProject() throws IOException, AnalysisException {
        log.info("演示: 分析当前AI Reviewer项目");

        // 加载配置
        Config config = Config.loadDefault();
        log.info("配置加载成功");

        // 创建AI评审器
        AIReviewer reviewer = AIReviewer.builder()
                .withConfig(config)
                .build();

        // 分析项目（假设项目根目录是当前目录的父目录）
        String projectPath = System.getProperty("user.dir");
        log.info("项目路径: {}", projectPath);

        AnalysisResult result = reviewer.analyzeProject(projectPath);

        // 输出结果
        log.info("分析完成!");
        log.info("项目: {}", result.getProjectName());
        log.info("总体评分: {}/100", result.getOverallScore());
        log.info("架构评分: {}/100", result.getArchitectureScore());
        log.info("代码质量评分: {}/100", result.getCodeQualityScore());
        log.info("技术债务评分: {}/100", result.getTechnicalDebtScore());
        log.info("功能评分: {}/100", result.getFunctionalityScore());

        // 生成报告
        ReportBuilder reportBuilder = new ReportBuilder();
        String markdownReport = reportBuilder.generateMarkdownReport(result);
        String htmlReport = reportBuilder.generateHtmlReport(result);

        // 保存报告
        reportBuilder.saveReport(result, "ai-reviewer-analysis-report.md", "markdown");
        reportBuilder.saveReport(result, "ai-reviewer-analysis-report.html", "html");

        log.info("报告已生成: ai-reviewer-analysis-report.md 和 ai-reviewer-analysis-report.html");

        // 打印摘要
        if (result.getSummaryReport() != null) {
            System.out.println("\n=== 分析摘要 ===");
            System.out.println(result.getSummaryReport().getContent());
        }
    }

    /**
     * 分析外部项目
     */
    private static void analyzeExternalProject(String projectPath) throws IOException, AnalysisException {
        log.info("演示: 分析外部项目 - {}", projectPath);

        // 加载配置
        Config config = Config.loadDefault();

        // 可以根据项目类型调整配置
        // config.getAnalysis().setDomainKnowledge(...);

        // 创建AI评审器
        AIReviewer reviewer = AIReviewer.builder()
                .withConfig(config)
                .build();

        // 执行分析
        AnalysisResult result = reviewer.analyzeProject(projectPath);

        // 输出结果
        System.out.println("=== 外部项目分析结果 ===");
        System.out.println("项目: " + result.getProjectName());
        System.out.println("总体评分: " + result.getOverallScore() + "/100");

        // 生成报告
        ReportBuilder reportBuilder = new ReportBuilder();
        reportBuilder.saveReport(result, projectPath + "/analysis-report.md", "markdown");
        reportBuilder.saveReport(result, projectPath + "/analysis-report.html", "html");

        log.info("外部项目分析完成，报告已保存到项目目录");
    }

    /**
     * 演示配置验证
     */
    private static void demonstrateConfigValidation() {
        log.info("演示: 配置验证");

        try {
            Config config = Config.loadDefault();

            // 验证AI服务配置
            if (config.getAiService().getApiKey() == null ||
                config.getAiService().getApiKey().startsWith("$")) {
                log.warn("AI服务API密钥未配置，请设置环境变量 DEEPSEEK_API_KEY");
            }

            // 验证文件扫描配置
            log.info("包含文件模式: {}", config.getFileScan().getIncludePatterns().size());
            log.info("核心文件模式: {}", config.getFileScan().getCoreFilePatterns().size());

            // 验证分析配置
            log.info("分析维度: {}", config.getAnalysis().getAnalysisDimensions());

            log.info("配置验证完成");

        } catch (Exception e) {
            log.error("配置验证失败", e);
        }
    }
}
