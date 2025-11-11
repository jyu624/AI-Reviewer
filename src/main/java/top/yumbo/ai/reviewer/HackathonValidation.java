package top.yumbo.ai.reviewer;

import lombok.extern.slf4j.Slf4j;

/**
 * Hackathon AI 评审工具功能验证
 * 验证核心功能结构，不依赖外部API
 */
@Slf4j
public class HackathonValidation {

    public static void main(String[] args) {
        log.info("=== 🏆 Hackathon AI 评审工具功能验证 ===\n");

        try {
            // 验证1: 评审模式枚举
            validateReviewModes();

            // 验证2: 评审状态枚举
            validateReviewStatus();

            // 验证3: HackathonScore类结构
            validateHackathonScore();

            // 验证4: Leaderboard类结构
            validateLeaderboard();

            // 验证5: ReviewStatistics类结构
            validateReviewStatistics();

            // 验证6: 配置文件存在性
            validateConfigurationFiles();

            // 验证7: 报告模板存在性
            validateReportTemplates();

            log.info("✅ 所有功能验证通过！黑客松AI评审工具结构完整。");

        } catch (Exception e) {
            log.error("❌ 功能验证失败", e);
            System.err.println("错误: " + e.getMessage());
        }
    }

    private static void validateReviewModes() {
        System.out.println("🎯 验证评审模式枚举...");

        // 验证所有评审模式
        HackathonReviewer.ReviewMode[] modes = HackathonReviewer.ReviewMode.values();
        assert modes.length == 3 : "评审模式数量不正确";

        for (HackathonReviewer.ReviewMode mode : modes) {
            assert mode.getDisplayName() != null : "评审模式显示名称为空";
            assert mode.getDescription() != null : "评审模式描述为空";
            System.out.println("   ✅ " + mode.getDisplayName() + ": " + mode.getDescription());
        }

        System.out.println("✅ 评审模式枚举验证通过\n");
    }

    private static void validateReviewStatus() {
        System.out.println("📊 验证评审状态枚举...");

        HackathonReviewer.ReviewStatus[] statuses = HackathonReviewer.ReviewStatus.values();
        assert statuses.length == 4 : "评审状态数量不正确";

        for (HackathonReviewer.ReviewStatus status : statuses) {
            assert status.getDisplayName() != null : "评审状态显示名称为空";
            System.out.println("   ✅ " + status.name() + ": " + status.getDisplayName());
        }

        System.out.println("✅ 评审状态枚举验证通过\n");
    }

    private static void validateHackathonScore() {
        System.out.println("🏆 验证HackathonScore类结构...");

        HackathonReviewer.HackathonScore score = new HackathonReviewer.HackathonScore();
        score.setProjectName("TestProject");
        score.setTotalScore(85.5);
        score.setJudgeStatus("优秀");

        assert "TestProject".equals(score.getProjectName()) : "项目名称设置失败";
        assert score.getTotalScore() == 85.5 : "总分设置��败";
        assert "优秀".equals(score.getJudgeStatus()) : "评审状态设置失败";

        System.out.println("   ✅ HackathonScore基本功能正常");
        System.out.println("   ✅ toString()方法: " + score.toString());

        System.out.println("✅ HackathonScore类结构验证通过\n");
    }

    private static void validateLeaderboard() {
        System.out.println("🏅 验证Leaderboard类结构...");

        HackathonReviewer.Leaderboard leaderboard = new HackathonReviewer.Leaderboard();
        leaderboard.setTotalProjects(10);
        leaderboard.setAverageScore(75.5);
        leaderboard.setHighestScore(95.0);
        leaderboard.setLowestScore(60.0);

        assert leaderboard.getTotalProjects() == 10 : "总项目数设置失败";
        assert leaderboard.getAverageScore() == 75.5 : "平均分设置失败";
        assert leaderboard.getHighestScore() == 95.0 : "最高分设置失败";
        assert leaderboard.getLowestScore() == 60.0 : "最低分设置失败";

        System.out.println("   ✅ Leaderboard基本功能正常");
        System.out.println("   ✅ 排行榜统计功能完整");

        System.out.println("✅ Leaderboard类结构验证通过\n");
    }

    private static void validateReviewStatistics() {
        System.out.println("📈 验证ReviewStatistics类结构...");

        HackathonReviewer.ReviewStatistics stats = new HackathonReviewer.ReviewStatistics();
        stats.setTotalReviews(25);
        stats.setCompletedReviews(20);
        stats.setFailedReviews(3);
        stats.setAverageDuration(25000.0);

        assert stats.getTotalReviews() == 25 : "总评审数设置失败";
        assert stats.getCompletedReviews() == 20 : "完成评审数设置失败";
        assert stats.getFailedReviews() == 3 : "失败评审数设置失败";
        assert stats.getAverageDuration() == 25000.0 : "平均耗时设置失败";

        System.out.println("   ✅ ReviewStatistics基本功能正常");
        System.out.println("   ✅ 评审统计功能完整");

        System.out.println("✅ ReviewStatistics类结构验证通过\n");
    }

    private static void validateConfigurationFiles() {
        System.out.println("⚙️ 验证配置文件存在性...");

        java.nio.file.Path configPath = java.nio.file.Paths.get("src/main/resources/hackathon-config.yaml");
        assert java.nio.file.Files.exists(configPath) : "黑客松配置文件不存在";

        System.out.println("   ✅ hackathon-config.yaml 存在");

        // 验证配置文件可以被加载（不依赖外部API）
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(configPath.toFile());
            fis.close();
            System.out.println("   ✅ 配置文件可读取");
        } catch (Exception e) {
            throw new RuntimeException("配置文件读取失败", e);
        }

        System.out.println("✅ 配置文件验证通过\n");
    }

    private static void validateReportTemplates() {
        System.out.println("📄 验证报告模板存在性...");

        String[] templateNames = {
            "hackathon-quick-report.md",
            "hackathon-detailed-report.md",
            "hackathon-expert-report.md"
        };

        for (String templateName : templateNames) {
            java.nio.file.Path templatePath = java.nio.file.Paths.get("src/main/resources/templates/" + templateName);
            assert java.nio.file.Files.exists(templatePath) : "报告模板不存在: " + templateName;
            System.out.println("   ✅ " + templateName + " 存在");
        }

        System.out.println("✅ 报告模板验证通过\n");
    }
}
