package top.yumbo.ai.refactor.application.service;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.refactor.application.port.input.ReportGenerationUseCase;
import top.yumbo.ai.refactor.application.port.output.FileSystemPort;
import top.yumbo.ai.refactor.domain.model.ReviewReport;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * 报告生成服务实现
 */
@Slf4j
public class ReportGenerationService implements ReportGenerationUseCase {

    private final FileSystemPort fileSystemPort;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportGenerationService(FileSystemPort fileSystemPort) {
        this.fileSystemPort = fileSystemPort;
    }

    @Override
    public String generateMarkdownReport(ReviewReport report) {
        log.info("生成Markdown报告: {}", report.getReportId());

        StringBuilder md = new StringBuilder();

        // 标题
        md.append("# 项目分析报告\n\n");

        // 基本信息
        md.append("## 基本信息\n\n");
        md.append("- **项目名称**: ").append(report.getProjectName()).append("\n");
        md.append("- **项目路径**: ").append(report.getProjectPath()).append("\n");
        md.append("- **生成时间**: ").append(report.getGeneratedAt().format(DATE_FORMATTER)).append("\n");
        md.append("- **报告ID**: ").append(report.getReportId()).append("\n\n");

        // 总体评分
        md.append("## 总体评分\n\n");
        md.append("**综合评分: ").append(report.getOverallScore()).append("/100**");
        md.append(" (").append(report.getGrade()).append(")\n\n");

        // 各维度评分
        md.append("### 评分详情\n\n");
        md.append("| 维度 | 评分 |\n");
        md.append("|------|------|\n");
        for (var entry : report.getDimensionScores().entrySet()) {
            md.append("| ").append(entry.getKey()).append(" | ")
              .append(entry.getValue()).append("/100 |\n");
        }
        md.append("\n");

        // 项目概览
        if (report.getProjectOverview() != null) {
            md.append("## 项目概览\n\n");
            md.append(report.getProjectOverview()).append("\n\n");
        }

        // 架构分析
        if (report.getArchitectureAnalysis() != null) {
            md.append("## 架构分析\n\n");
            md.append(report.getArchitectureAnalysis().getAnalysisResult()).append("\n\n");
        }

        // 关键发现
        if (!report.getKeyFindings().isEmpty()) {
            md.append("## 关键发现\n\n");
            for (String finding : report.getKeyFindings()) {
                md.append("- ").append(finding).append("\n");
            }
            md.append("\n");
        }

        // 问题列表
        if (!report.getIssues().isEmpty()) {
            md.append("## 发现的问题\n\n");
            for (ReviewReport.Issue issue : report.getIssues()) {
                md.append("### ").append(issue.getTitle()).append("\n\n");
                md.append("- **类型**: ").append(issue.getType()).append("\n");
                md.append("- **严重程度**: ").append(issue.getSeverity()).append("\n");
                if (issue.getFilePath() != null) {
                    md.append("- **文件**: ").append(issue.getFilePath()).append("\n");
                }
                md.append("- **描述**: ").append(issue.getDescription()).append("\n");
                if (issue.getSuggestion() != null) {
                    md.append("- **建议**: ").append(issue.getSuggestion()).append("\n");
                }
                md.append("\n");
            }
        }

        // 改进建议
        if (!report.getRecommendations().isEmpty()) {
            md.append("## 改进建议\n\n");
            for (ReviewReport.Recommendation rec : report.getRecommendations()) {
                md.append("### ").append(rec.getTitle()).append("\n\n");
                md.append("- **类别**: ").append(rec.getCategory()).append("\n");
                md.append("- **优先级**: ").append(rec.getPriority()).append("\n");
                md.append("- **描述**: ").append(rec.getDescription()).append("\n");
                md.append("- **理由**: ").append(rec.getRationale()).append("\n");
                md.append("- **预计工作量**: ").append(rec.getEstimatedEffort()).append("人天\n\n");
            }
        }

        return md.toString();
    }

    @Override
    public String generateHtmlReport(ReviewReport report) {
        log.info("生成HTML报告: {}", report.getReportId());

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>项目分析报告 - ").append(report.getProjectName()).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        h1 { color: #333; }\n");
        html.append("        .score { font-size: 24px; font-weight: bold; color: #4CAF50; }\n");
        html.append("        table { border-collapse: collapse; width: 100%; }\n");
        html.append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("        th { background-color: #4CAF50; color: white; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("    <h1>项目分析报告</h1>\n");
        html.append("    <p><strong>项目名称:</strong> ").append(report.getProjectName()).append("</p>\n");
        html.append("    <p><strong>生成时间:</strong> ")
            .append(report.getGeneratedAt().format(DATE_FORMATTER)).append("</p>\n");

        html.append("    <h2>总体评分</h2>\n");
        html.append("    <p class=\"score\">").append(report.getOverallScore()).append("/100 (")
            .append(report.getGrade()).append(")</p>\n");

        html.append("    <h2>评分详情</h2>\n");
        html.append("    <table>\n");
        html.append("        <tr><th>维度</th><th>评分</th></tr>\n");
        for (var entry : report.getDimensionScores().entrySet()) {
            html.append("        <tr><td>").append(entry.getKey()).append("</td><td>")
                .append(entry.getValue()).append("/100</td></tr>\n");
        }
        html.append("    </table>\n");

        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    @Override
    public String generateJsonReport(ReviewReport report) {
        log.info("生成JSON报告: {}", report.getReportId());

        // 简化实现：使用手动JSON构建
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"reportId\": \"").append(report.getReportId()).append("\",\n");
        json.append("  \"projectName\": \"").append(report.getProjectName()).append("\",\n");
        json.append("  \"overallScore\": ").append(report.getOverallScore()).append(",\n");
        json.append("  \"grade\": \"").append(report.getGrade()).append("\",\n");
        json.append("  \"generatedAt\": \"").append(report.getGeneratedAt()).append("\"\n");
        json.append("}\n");

        return json.toString();
    }

    @Override
    public void saveReport(ReviewReport report, Path outputPath, String format) {
        log.info("保存报告到文件: {}, format={}", outputPath, format);

        String content = switch (format.toLowerCase()) {
            case "html" -> generateHtmlReport(report);
            case "json" -> generateJsonReport(report);
            default -> generateMarkdownReport(report);
        };

        fileSystemPort.writeFileContent(outputPath, content);
        log.info("报告已保存: {}", outputPath);
    }
}

