package top.yumbo.ai.reviewer.application.service.prompt;

import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;

/**
 * AI提示词构建器
 *
 * 将项目信息和代码洞察转换为结构化的AI提示词
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
public class AIPromptBuilder {

    /**
     * 构建增强版项目分析提示词
     */
    public String buildEnhancedPrompt(Project project, CodeInsight insight) {
        StringBuilder prompt = new StringBuilder();

        // 1. 项目基础信息
        prompt.append(buildBasicInfo(project));

        // 2. 代码结构洞察（来自AST）
        if (insight != null) {
            prompt.append(insight.toAIPrompt());
        }

        // 3. 分析任务
        prompt.append(buildAnalysisTasks());

        return prompt.toString();
    }

    /**
     * 构建基础信息
     */
    private String buildBasicInfo(Project project) {
        return String.format("""
            请分析以下项目的整体情况：
            
            项目名称: %s
            项目类型: %s
            文件数量: %d
            代码行数: %d
            
            """,
            project.getName(),
            project.getType().getDisplayName(),
            project.getSourceFiles().size(),
            project.getTotalLines()
        );
    }

    /**
     * 构建分析任务
     */
    private String buildAnalysisTasks() {
        return """
            
            ## 分析任务
            
            基于以上信息，请评估：
            1. 架构设计合理性（分层、解耦、可扩展性）
            2. 代码质量水平（复杂度、可读性、可维护性）
            3. 技术栈选型的合理性
            4. 潜在的技术债务和风险点
            5. 改进建议（具体到类和方法级别）
            
            请给出总体评语和各维度评分（0-100分）。
            """;
    }

    /**
     * 构建简化版提示词（用于token限制场景）
     */
    public String buildSimplifiedPrompt(Project project, CodeInsight insight) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(buildBasicInfo(project));

        if (insight != null && insight.getComplexityMetrics() != null) {
            prompt.append("## 代码质量概览\n\n");
            prompt.append(insight.getComplexityMetrics().toSummary());
            prompt.append("\n");
        }

        prompt.append("\n请对项目进行整体评估，给出评分和改进建议。\n");

        return prompt.toString();
    }
}

