package top.yumbo.ai.refactor.application.port.input;

import top.yumbo.ai.refactor.domain.model.ReviewReport;

import java.nio.file.Path;

/**
 * 报告生成用例接口（输入端口）
 */
public interface ReportGenerationUseCase {

    /**
     * 生成Markdown报告
     * @param report 评审报告
     * @return Markdown内容
     */
    String generateMarkdownReport(ReviewReport report);

    /**
     * 生成HTML报告
     * @param report 评审报告
     * @return HTML内容
     */
    String generateHtmlReport(ReviewReport report);

    /**
     * 生成JSON报告
     * @param report 评审报告
     * @return JSON内容
     */
    String generateJsonReport(ReviewReport report);

    /**
     * 保存报告到文件
     * @param report 评审报告
     * @param outputPath 输出路径
     * @param format 格式 (markdown/html/json)
     */
    void saveReport(ReviewReport report, Path outputPath, String format);
}

