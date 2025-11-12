package top.yumbo.ai.reviewer.examples;

import top.yumbo.ai.reviewer.domain.model.ReviewReport;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示如何使用新的评语功能
 */
public class ReviewReportWithCommentsExample {

    public static void main(String[] args) {
        // 创建带评语的评审报告示例
        ReviewReport report = ReviewReport.builder()
                .reportId("example-001")
                .projectName("示例项目")
                .projectPath("/path/to/project")
                .generatedAt(LocalDateTime.now())
                .build();

        // 设置总体评语
        report.setOverallSummary(
                "这是一个设计良好的项目，整体代码质量高，架构清晰合理。" +
                "项目采用六边形架构设计，层次分明，依赖关系清晰。" +
                "代码规范性好，注释完善，具有良好的可维护性和可扩展性。" +
                "建议继续完善测试覆盖率，增加更多的集成测试。"
        );

        // 添加各维度评分和评语
        report.addDimensionScore("architecture", 85);
        report.addDimensionComment("architecture",
                "架构设计：采用六边形架构模式，核心业务逻辑与外部依赖解耦良好。" +
                "使用了依赖注入，提高了代码的可测试性。" +
                "建议：进一步完善领域模型，增强业务规则的表达能力。"
        );

        report.addDimensionScore("code_quality", 90);
        report.addDimensionComment("code_quality",
                "代码质量：代码整洁，命名规范，遵循Java编码规范。" +
                "使用了Lombok减少样板代码，提高了开发效率。" +
                "注释完善，易于理解和维护。" +
                "建议：增加代码复杂度分析，识别潜在的优化点。"
        );

        report.addDimensionScore("innovation", 80);
        report.addDimensionComment("innovation",
                "创新性：项目采用了现代化的架构设计理念，具有一定的创新性。" +
                "引入了AI评审功能，这在同类项目中较为少见。" +
                "建议：可以考虑引入更多的AI能力，如自动修复建议等。"
        );

        report.addDimensionScore("completeness", 85);
        report.addDimensionComment("completeness",
                "完整性：功能实现完整，文档齐全。" +
                "包含了CLI工具、配置管理、报告生成等核心功能。" +
                "文档结构清晰，包含了架构说明、使用指南等。" +
                "建议：补充API文档和部署指南。"
        );

        report.addDimensionScore("documentation", 80);
        report.addDimensionComment("documentation",
                "文档质量：README清晰，包含了项目介绍、使用说明等。" +
                "代码注释完善，易于理解。" +
                "建议：增加架构图和流程图，帮助理解系统设计。"
        );

        // 计算总体评分
        Map<String, Double> weights = new HashMap<>();
        weights.put("architecture", 0.25);
        weights.put("code_quality", 0.30);
        weights.put("innovation", 0.20);
        weights.put("completeness", 0.15);
        weights.put("documentation", 0.10);
        report.calculateOverallScore(weights);

        // 打印结果
        System.out.println("=== 评审报告示例 ===");
        System.out.println("项目: " + report.getProjectName());
        System.out.println("总体评分: " + report.getOverallScore() + "/100 (" + report.getGrade() + ")");
        System.out.println("\n总体评语:");
        System.out.println(report.getOverallSummary());

        System.out.println("\n各维度评分和评语:");
        report.getDimensionScores().forEach((dimension, score) -> {
            System.out.println("\n" + dimension + ": " + score + "/100");
            String comment = report.getDimensionComment(dimension);
            if (comment != null && !comment.isBlank()) {
                System.out.println("  评语: " + comment);
            }
        });
    }
}

