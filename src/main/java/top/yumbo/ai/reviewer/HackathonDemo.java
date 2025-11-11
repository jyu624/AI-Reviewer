package top.yumbo.ai.reviewer;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.HackathonReviewer.HackathonScore;
import top.yumbo.ai.reviewer.HackathonReviewer.ReviewMode;
import top.yumbo.ai.reviewer.HackathonReviewer.Leaderboard;
import top.yumbo.ai.reviewer.HackathonReviewer.ReviewStatistics;

import java.util.Arrays;
import java.util.List;

/**
 * Hackathon AI 源码评分工具演示 - 专业版
 * 展示专业黑客松AI评分工具的完整功能
 */
@Slf4j
public class HackathonDemo {

    public static void main(String[] args) {
        log.info("=== 🏆 Hackathon AI 源码评分工具演示 - 专业版 ===\n");

        try {
            HackathonReviewer reviewer = new HackathonReviewer();

            // 演示1: 智能评审 (自动选择评审模式)
            demonstrateSmartReview(reviewer);

            // 演示2: 多模式评审对比
            demonstrateMultiModeReview(reviewer);

            // 演示3: 批量评审和排行榜
            demonstrateBatchReviewAndLeaderboard(reviewer);

            // 演示4: 评审统计和历史
            demonstrateReviewStatistics(reviewer);

            // 演示5: 专业评审报告生成
            demonstrateProfessionalReports(reviewer);

            // 清理资源
            reviewer.shutdown();

        } catch (Exception e) {
            log.error("Hackathon演示执行失败", e);
            System.err.println("错误: " + e.getMessage());
        }
    }

    private static void demonstrateSmartReview(HackathonReviewer reviewer) {
        System.out.println("🎯 演示1: 智能评审 (自动选择评审模式)");
        System.out.println("-".repeat(60));

        try {
            // 对当前项目进行智能评审
            HackathonScore score = reviewer.smartReview(".");

            System.out.println("📊 智能评审结果:");
            System.out.printf("项目名称: %s%n", score.getProjectName());
            System.out.printf("评审模式: %s%n", score.getReviewMode().getDisplayName());
            System.out.printf("总评分: %.1f/100%n", score.getTotalScore());
            System.out.printf("评审状态: %s%n", score.getJudgeStatus());
            System.out.printf("评审时间: %s%n", score.getReviewTime());
            System.out.println();

            System.out.println("📈 详细评分:");
            System.out.printf("├─ 架构设计: %.1f/100%n", score.getArchitectureScore());
            System.out.printf("├─ 代码质量: %.1f/100%n", score.getCodeQualityScore());
            System.out.printf("├─ 功能完整性: %.1f/100%n", score.getFunctionalityScore());
            System.out.printf("├─ 商业价值: %.1f/100%n", score.getBusinessValueScore());
            System.out.printf("├─ 测试覆盖率: %.1f/100%n", score.getTestCoverageScore());
            System.out.printf("└─ 创新性: %.1f/100%n", score.getInnovationScore());

        } catch (Exception e) {
            System.out.println("❌ 智能评审失败: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateMultiModeReview(HackathonReviewer reviewer) {
        System.out.println("🎯 演示2: 多模式评审对比");
        System.out.println("-".repeat(60));

        ReviewMode[] modes = {ReviewMode.QUICK, ReviewMode.DETAILED, ReviewMode.EXPERT};

        for (ReviewMode mode : modes) {
            try {
                System.out.printf("🔍 %s模式评审:%n", mode.getDisplayName());

                long startTime = System.currentTimeMillis();
                HackathonScore score = reviewer.review(".", mode);
                long duration = System.currentTimeMillis() - startTime;

                System.out.printf("   评分: %.1f/100 (%s)%n", score.getTotalScore(), score.getJudgeStatus());
                System.out.printf("   耗时: %dms%n", duration);
                System.out.printf("   描述: %s%n", mode.getDescription());
                System.out.println();

            } catch (Exception e) {
                System.out.printf("   ❌ 评审失败: %s%n%n", e.getMessage());
            }
        }
    }

    private static void demonstrateBatchReviewAndLeaderboard(HackathonReviewer reviewer) {
        System.out.println("🎯 演示3: 批量评审和排行榜");
        System.out.println("-".repeat(60));

        // 模拟多个参赛项目 (实际使用时替换为真实项目路径)
        List<String> projectPaths = Arrays.asList(
                ".",  // 当前项目作为示例
                "."   // 重复用于演示
        );

        try {
            // 批量评审
            List<HackathonScore> scores = reviewer.batchReview(projectPaths, ReviewMode.DETAILED);

            System.out.println("📊 批量评审结果:");
            for (int i = 0; i < scores.size(); i++) {
                HackathonScore score = scores.get(i);
                System.out.printf("%d. %s%n", i + 1, score.toString());
            }
            System.out.println();

            // 生成排行榜
            Leaderboard leaderboard = reviewer.generateLeaderboard(scores);

            System.out.println("🏆 排行榜统计:");
            System.out.printf("总项目数: %d%n", leaderboard.getTotalProjects());
            System.out.printf("平均分: %.1f%n", leaderboard.getAverageScore());
            System.out.printf("最高分: %.1f%n", leaderboard.getHighestScore());
            System.out.printf("最低分: %.1f%n", leaderboard.getLowestScore());
            System.out.println();

            System.out.println("📈 状态分布:");
            leaderboard.getStatusStatistics().forEach((status, count) ->
                System.out.printf("   %s: %d 个项目%n", status, count));

        } catch (Exception e) {
            System.out.println("❌ 批量评审失败: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateReviewStatistics(HackathonReviewer reviewer) {
        System.out.println("🎯 演示4: 评审统计和历史");
        System.out.println("-".repeat(60));

        ReviewStatistics stats = reviewer.getReviewStatistics();

        System.out.println("📈 评审统计信息:");
        System.out.printf("总评审数: %d%n", stats.getTotalReviews());
        System.out.printf("完成评审: %d%n", stats.getCompletedReviews());
        System.out.printf("失败评审: %d%n", stats.getFailedReviews());
        System.out.printf("平均耗时: %.0fms%n", stats.getAverageDuration());
        System.out.println();

        // 显示评审历史
        List<HackathonReviewer.ReviewRecord> history = reviewer.getReviewHistory();
        if (!history.isEmpty()) {
            System.out.println("📋 评审历史记录:");
            history.forEach(record -> {
                System.out.printf("   %s - %s [%s] (耗时: %dms)%n",
                        record.getProjectPath(),
                        record.getMode().getDisplayName(),
                        record.getStatus().getDisplayName(),
                        record.getDuration());
            });
        }

        System.out.println();
    }

    private static void demonstrateProfessionalReports(HackathonReviewer reviewer) {
        System.out.println("🎯 演示5: 专业评审报告生成");
        System.out.println("-".repeat(60));

        try {
            // 生成不同模式的评审报告
            HackathonScore quickScore = reviewer.review(".", ReviewMode.QUICK);
            HackathonScore detailedScore = reviewer.review(".", ReviewMode.DETAILED);
            HackathonScore expertScore = reviewer.review(".", ReviewMode.EXPERT);

            // 生成各种模式的报告
            reviewer.generateReviewReport(quickScore, "hackathon-quick-report.md", ReviewMode.QUICK);
            reviewer.generateReviewReport(detailedScore, "hackathon-detailed-report.md", ReviewMode.DETAILED);
            reviewer.generateReviewReport(expertScore, "hackathon-expert-report.md", ReviewMode.EXPERT);

            System.out.println("📄 专业评审报告已生成:");
            System.out.println("   • hackathon-quick-report.md (快速评审)");
            System.out.println("   • hackathon-detailed-report.md (详细评审)");
            System.out.println("   • hackathon-expert-report.md (专家评审)");

            System.out.println();
            System.out.println("💡 报告特性:");
            System.out.println("   • 结构化评分展示");
            System.out.println("   • 详细技术分析");
            System.out.println("   • 评审意见和建议");
            System.out.println("   • 专业评审结论");

        } catch (Exception e) {
            System.out.println("❌ 报告生成失败: " + e.getMessage());
        }

        System.out.println();
    }
}
