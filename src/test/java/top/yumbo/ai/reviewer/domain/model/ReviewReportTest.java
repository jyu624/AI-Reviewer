package top.yumbo.ai.reviewer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ReviewReport领域模型单元测试
 */
@DisplayName("ReviewReport领域模型测试")
class ReviewReportTest {

    private ReviewReport report;
    private Map<String, Double> defaultWeights;

    @BeforeEach
    void setUp() {
        report = ReviewReport.builder()
                .reportId("report-123")
                .projectName("TestProject")
                .projectPath("/test/path")
                .build();

        defaultWeights = new HashMap<>();
        defaultWeights.put("architecture", 0.20);
        defaultWeights.put("code_quality", 0.20);
        defaultWeights.put("technical_debt", 0.15);
        defaultWeights.put("functionality", 0.20);
        defaultWeights.put("business_value", 0.15);
        defaultWeights.put("test_coverage", 0.10);
    }

    @Nested
    @DisplayName("基本属性测试")
    class BasicPropertiesTest {

        @Test
        @DisplayName("应该正确创建ReviewReport对象")
        void shouldCreateReportWithBasicProperties() {
            assertThat(report.getReportId()).isEqualTo("report-123");
            assertThat(report.getProjectName()).isEqualTo("TestProject");
            assertThat(report.getProjectPath()).isEqualTo("/test/path");
            assertThat(report.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("应该初始化空的集合")
        void shouldInitializeEmptyCollections() {
            assertThat(report.getDimensionScores()).isEmpty();
            assertThat(report.getModuleAnalyses()).isEmpty();
            assertThat(report.getIssues()).isEmpty();
            assertThat(report.getRecommendations()).isEmpty();
            assertThat(report.getKeyFindings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addDimensionScore()和getDimensionScore()测试")
    class DimensionScoreTest {

        @Test
        @DisplayName("应该成功添加维度评分")
        void shouldAddDimensionScore() {
            report.addDimensionScore("architecture", 85);

            assertThat(report.getDimensionScore("architecture")).isEqualTo(85);
        }

        @Test
        @DisplayName("应该支持添加多个维度评分")
        void shouldAddMultipleDimensionScores() {
            report.addDimensionScore("architecture", 85);
            report.addDimensionScore("code_quality", 75);
            report.addDimensionScore("technical_debt", 90);

            assertThat(report.getDimensionScores()).hasSize(3);
            assertThat(report.getDimensionScore("architecture")).isEqualTo(85);
            assertThat(report.getDimensionScore("code_quality")).isEqualTo(75);
            assertThat(report.getDimensionScore("technical_debt")).isEqualTo(90);
        }

        @Test
        @DisplayName("获取不存在的维度应该返回0")
        void shouldReturnZeroForNonExistentDimension() {
            assertThat(report.getDimensionScore("non_existent")).isZero();
        }

        @Test
        @DisplayName("应该允许更新已有维度的评分")
        void shouldAllowUpdatingExistingDimensionScore() {
            report.addDimensionScore("architecture", 80);
            report.addDimensionScore("architecture", 90); // 更新

            assertThat(report.getDimensionScore("architecture")).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("calculateOverallScore()方法测试")
    class CalculateOverallScoreTest {

        @Test
        @DisplayName("应该根据权重正确计算总分")
        void shouldCalculateOverallScoreWithWeights() {
            // 添加所有维度评分
            report.addDimensionScore("architecture", 80);
            report.addDimensionScore("code_quality", 70);
            report.addDimensionScore("technical_debt", 90);
            report.addDimensionScore("functionality", 85);
            report.addDimensionScore("business_value", 75);
            report.addDimensionScore("test_coverage", 60);

            report.calculateOverallScore(defaultWeights);

            // 计算期望值: 80*0.2 + 70*0.2 + 90*0.15 + 85*0.2 + 75*0.15 + 60*0.1
            // = 16 + 14 + 13.5 + 17 + 11.25 + 6 = 77.75 ≈ 78
            assertThat(report.getOverallScore()).isEqualTo(78);
        }

        @Test
        @DisplayName("当没有评分时应该返回0")
        void shouldReturnZeroWhenNoScores() {
            report.calculateOverallScore(defaultWeights);

            assertThat(report.getOverallScore()).isZero();
        }

        @Test
        @DisplayName("应该正确处理部分维度评分")
        void shouldHandlePartialDimensionScores() {
            report.addDimensionScore("architecture", 100);
            report.addDimensionScore("code_quality", 100);

            Map<String, Double> partialWeights = new HashMap<>();
            partialWeights.put("architecture", 0.5);
            partialWeights.put("code_quality", 0.5);

            report.calculateOverallScore(partialWeights);

            assertThat(report.getOverallScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("应该正确处理边界值评分")
        void shouldHandleBoundaryScores() {
            report.addDimensionScore("architecture", 0);
            report.addDimensionScore("code_quality", 100);

            Map<String, Double> weights = new HashMap<>();
            weights.put("architecture", 0.5);
            weights.put("code_quality", 0.5);

            report.calculateOverallScore(weights);

            assertThat(report.getOverallScore()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("isPassThreshold()方法测试")
    class IsPassThresholdTest {

        @Test
        @DisplayName("评分高于阈值应该返回true")
        void shouldPassWhenScoreAboveThreshold() {
            report.addDimensionScore("architecture", 80);
            report.calculateOverallScore(Map.of("architecture", 1.0));

            assertThat(report.isPassThreshold(70)).isTrue();
        }

        @Test
        @DisplayName("评分等于阈值应该返回true")
        void shouldPassWhenScoreEqualsThreshold() {
            report.addDimensionScore("architecture", 80);
            report.calculateOverallScore(Map.of("architecture", 1.0));

            assertThat(report.isPassThreshold(80)).isTrue();
        }

        @Test
        @DisplayName("评分低于阈值应该返回false")
        void shouldNotPassWhenScoreBelowThreshold() {
            report.addDimensionScore("architecture", 60);
            report.calculateOverallScore(Map.of("architecture", 1.0));

            assertThat(report.isPassThreshold(70)).isFalse();
        }
    }

    @Nested
    @DisplayName("getGrade()方法测试")
    class GetGradeTest {

        @Test
        @DisplayName("评分90-100应该返回A")
        void shouldReturnAForScore90To100() {
            report.addDimensionScore("test", 95);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("A");
        }

        @Test
        @DisplayName("评分80-89应该返回B")
        void shouldReturnBForScore80To89() {
            report.addDimensionScore("test", 85);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("B");
        }

        @Test
        @DisplayName("评分70-79应该返回C")
        void shouldReturnCForScore70To79() {
            report.addDimensionScore("test", 75);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("C");
        }

        @Test
        @DisplayName("评分60-69应该返回D")
        void shouldReturnDForScore60To69() {
            report.addDimensionScore("test", 65);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("D");
        }

        @Test
        @DisplayName("评分<60应该返回F")
        void shouldReturnFForScoreBelow60() {
            report.addDimensionScore("test", 50);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("F");
        }

        @Test
        @DisplayName("边界值90应该返回A")
        void shouldReturnAForScore90() {
            report.addDimensionScore("test", 90);
            report.calculateOverallScore(Map.of("test", 1.0));

            assertThat(report.getGrade()).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("Issue管理测试")
    class IssueManagementTest {

        @Test
        @DisplayName("应该成功添加Issue")
        void shouldAddIssue() {
            ReviewReport.Issue issue = ReviewReport.Issue.builder()
                    .type("architecture")
                    .severity("high")
                    .title("Test Issue")
                    .description("Test description")
                    .build();

            report.addIssue(issue);

            assertThat(report.getIssues()).hasSize(1);
            assertThat(report.getIssues()).contains(issue);
        }

        @Test
        @DisplayName("应该忽略null的Issue")
        void shouldIgnoreNullIssue() {
            report.addIssue(null);

            assertThat(report.getIssues()).isEmpty();
        }

        @Test
        @DisplayName("应该支持添加多个Issue")
        void shouldAddMultipleIssues() {
            ReviewReport.Issue issue1 = ReviewReport.Issue.builder().title("Issue 1").build();
            ReviewReport.Issue issue2 = ReviewReport.Issue.builder().title("Issue 2").build();

            report.addIssue(issue1);
            report.addIssue(issue2);

            assertThat(report.getIssues()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Recommendation管理测试")
    class RecommendationManagementTest {

        @Test
        @DisplayName("应该成功添加Recommendation")
        void shouldAddRecommendation() {
            ReviewReport.Recommendation rec = ReviewReport.Recommendation.builder()
                    .category("performance")
                    .priority("high")
                    .title("Optimize queries")
                    .description("Use indexes")
                    .build();

            report.addRecommendation(rec);

            assertThat(report.getRecommendations()).hasSize(1);
            assertThat(report.getRecommendations()).contains(rec);
        }

        @Test
        @DisplayName("应该忽略null的Recommendation")
        void shouldIgnoreNullRecommendation() {
            report.addRecommendation(null);

            assertThat(report.getRecommendations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("KeyFinding管理测试")
    class KeyFindingManagementTest {

        @Test
        @DisplayName("应该成功添加KeyFinding")
        void shouldAddKeyFinding() {
            report.addKeyFinding("Important finding 1");

            assertThat(report.getKeyFindings()).hasSize(1);
            assertThat(report.getKeyFindings()).contains("Important finding 1");
        }

        @Test
        @DisplayName("应该忽略null的KeyFinding")
        void shouldIgnoreNullKeyFinding() {
            report.addKeyFinding(null);

            assertThat(report.getKeyFindings()).isEmpty();
        }

        @Test
        @DisplayName("应该忽略空字符串的KeyFinding")
        void shouldIgnoreBlankKeyFinding() {
            report.addKeyFinding("   ");

            assertThat(report.getKeyFindings()).isEmpty();
        }

        @Test
        @DisplayName("应该支持添加多个KeyFinding")
        void shouldAddMultipleKeyFindings() {
            report.addKeyFinding("Finding 1");
            report.addKeyFinding("Finding 2");
            report.addKeyFinding("Finding 3");

            assertThat(report.getKeyFindings()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("完整场景测试")
    class CompleteScenarioTest {

        @Test
        @DisplayName("应该支持构建完整的报告")
        void shouldBuildCompleteReport() {
            // 添加维度评分
            report.addDimensionScore("architecture", 85);
            report.addDimensionScore("code_quality", 80);
            report.addDimensionScore("technical_debt", 75);

            // 计算总分
            Map<String, Double> weights = Map.of(
                "architecture", 0.4,
                "code_quality", 0.4,
                "technical_debt", 0.2
            );
            report.calculateOverallScore(weights);

            // 添加问题
            report.addIssue(ReviewReport.Issue.builder()
                    .type("architecture")
                    .severity("medium")
                    .title("Tight coupling")
                    .build());

            // 添加建议
            report.addRecommendation(ReviewReport.Recommendation.builder()
                    .category("refactoring")
                    .priority("high")
                    .title("Extract interface")
                    .build());

            // 添加关键发现
            report.addKeyFinding("Good architecture design");

            // 验证完整性
            assertThat(report.getOverallScore()).isGreaterThan(0);
            assertThat(report.getGrade()).isNotBlank();
            assertThat(report.getDimensionScores()).isNotEmpty();
            assertThat(report.getIssues()).hasSize(1);
            assertThat(report.getRecommendations()).hasSize(1);
            assertThat(report.getKeyFindings()).hasSize(1);
        }
    }
}

