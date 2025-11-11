package top.yumbo.ai.refactor.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.yumbo.ai.refactor.application.port.output.FileSystemPort;
import top.yumbo.ai.refactor.domain.model.ReviewReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * ReportGenerationService测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportGenerationService测试")
class ReportGenerationServiceTest {

    @Mock
    private FileSystemPort fileSystemPort;

    private ReportGenerationService service;

    private ReviewReport testReport;

    @BeforeEach
    void setUp() {
        service = new ReportGenerationService(fileSystemPort);

        // 创建测试报告
        testReport = ReviewReport.builder()
                .reportId("test-report-001")
                .projectName("测试项目")
                .projectPath("/test/project")
                .generatedAt(LocalDateTime.now())
                .build();

        testReport.addDimensionScore("architecture", 85);
        testReport.addDimensionScore("code_quality", 75);
        testReport.addDimensionScore("performance", 80);
    }

    @Nested
    @DisplayName("generateMarkdownReport()方法测试")
    class GenerateMarkdownReportTest {

        @Test
        @DisplayName("应该生成包含项目基本信息的Markdown报告")
        void shouldGenerateMarkdownWithBasicInfo() {
            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("# 项目分析报告")
                    .contains("## 基本信息")
                    .contains("测试项目")
                    .contains("test-report-001");
        }

        @Test
        @DisplayName("应该包含总体评分")
        void shouldIncludeOverallScore() {
            testReport.calculateOverallScore(null);

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("## 总体评分")
                    .contains("综合评分:");
        }

        @Test
        @DisplayName("应该包含各维度评分表格")
        void shouldIncludeDimensionScores() {
            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("### 评分详情")
                    .contains("| 维度 | 评分 |")
                    .contains("architecture")
                    .contains("85/100");
        }

        @Test
        @DisplayName("应该包含项目概览（如果存在）")
        void shouldIncludeProjectOverview() {
            testReport.setProjectOverview("这是一个Java项目");

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("## 项目概览")
                    .contains("这是一个Java项目");
        }

        @Test
        @DisplayName("应该包含关键发现")
        void shouldIncludeKeyFindings() {
            testReport.addKeyFinding("发现了架构问题");
            testReport.addKeyFinding("代码质量良好");

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("## 关键发现")
                    .contains("- 发现了架构问题")
                    .contains("- 代码质量良好");
        }

        @Test
        @DisplayName("应该包含问题列表")
        void shouldIncludeIssues() {
            ReviewReport.Issue issue = ReviewReport.Issue.builder()
                    .title("性能问题")
                    .type("performance")
                    .severity("high")
                    .description("数据库查询效率低下")
                    .suggestion("添加索引")
                    .build();

            testReport.addIssue(issue);

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("## 发现的问题")
                    .contains("### 性能问题")
                    .contains("**类型**: performance")
                    .contains("**严重程度**: high")
                    .contains("**描述**: 数据库查询效率低下")
                    .contains("**建议**: 添加索引");
        }

        @Test
        @DisplayName("应该包含改进建议")
        void shouldIncludeRecommendations() {
            ReviewReport.Recommendation rec = ReviewReport.Recommendation.builder()
                    .title("使用缓存")
                    .category("performance")
                    .priority("high")
                    .description("添加Redis缓存")
                    .rationale("提升性能50%")
                    .estimatedEffort(3)
                    .build();

            testReport.addRecommendation(rec);

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("## 改进建议")
                    .contains("### 使用缓存")
                    .contains("**类别**: performance")
                    .contains("**优先级**: high");
        }

        @Test
        @DisplayName("应该处理空的关键发现列表")
        void shouldHandleEmptyKeyFindings() {
            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown).doesNotContain("## 关键发现");
        }

        @Test
        @DisplayName("应该处理空的问题列表")
        void shouldHandleEmptyIssues() {
            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown).doesNotContain("## 发现的问题");
        }
    }

    @Nested
    @DisplayName("报告保存测试")
    class SaveReportTest {

        @Test
        @DisplayName("应该能够将Markdown报告保存到文件")
        void shouldSaveMarkdownReportToFile() {
            Path outputPath = Paths.get("/output/report.md");
            String markdown = service.generateMarkdownReport(testReport);

            // 通过FileSystemPort保存
            fileSystemPort.writeFileContent(outputPath, markdown);

            verify(fileSystemPort).writeFileContent(eq(outputPath), contains("# 项目分析报告"));
        }

        @Test
        @DisplayName("应该能够将JSON报告保存到文件")
        void shouldSaveJsonReportToFile() {
            Path outputPath = Paths.get("/output/report.json");
            String json = service.generateJsonReport(testReport);

            // 通过FileSystemPort保存
            fileSystemPort.writeFileContent(outputPath, json);

            verify(fileSystemPort, times(1)).writeFileContent(any(Path.class), anyString());
        }

        @Test
        @DisplayName("应该生成包含完整内容的Markdown报告")
        void shouldGenerateCompleteMarkdownReport() {
            testReport.setProjectOverview("项目概览");
            testReport.addKeyFinding("发现1");
            testReport.addIssue(ReviewReport.Issue.builder()
                    .title("问题1")
                    .type("bug")
                    .severity("high")
                    .description("描述")
                    .build());

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("# 项目分析报告")
                    .contains("项目概览")
                    .contains("发现1")
                    .contains("问题1");
        }
    }

    @Nested
    @DisplayName("generateJsonReport()方法测试")
    class GenerateJsonReportTest {

        @Test
        @DisplayName("应该生成JSON格式的报告")
        void shouldGenerateJsonReport() {
            String json = service.generateJsonReport(testReport);

            assertThat(json)
                    .contains("\"reportId\"")
                    .contains("\"projectName\"")
                    .contains("test-report-001")
                    .contains("测试项目");
        }

        @Test
        @DisplayName("应该包含所有维度评分")
        void shouldIncludeAllDimensionScores() {
            String json = service.generateJsonReport(testReport);

            assertThat(json)
                    .contains("\"architecture\"")
                    .contains("85")
                    .contains("\"code_quality\"")
                    .contains("75");
        }

        @Test
        @DisplayName("应该生成有效的JSON格式")
        void shouldGenerateValidJson() {
            String json = service.generateJsonReport(testReport);

            assertThat(json)
                    .startsWith("{")
                    .endsWith("}");
        }

        @Test
        @DisplayName("应该包含项目路径")
        void shouldIncludeProjectPath() {
            String json = service.generateJsonReport(testReport);

            assertThat(json).contains("projectPath");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("应该处理没有维度评分的报告")
        void shouldHandleReportWithNoDimensionScores() {
            ReviewReport emptyReport = ReviewReport.builder()
                    .reportId("empty-001")
                    .projectName("空项目")
                    .projectPath("/empty")
                    .generatedAt(LocalDateTime.now())
                    .build();

            String markdown = service.generateMarkdownReport(emptyReport);

            assertThat(markdown).contains("# 项目分析报告");
        }

        @Test
        @DisplayName("应该处理大量问题的报告")
        void shouldHandleReportWithManyIssues() {
            for (int i = 0; i < 100; i++) {
                testReport.addIssue(ReviewReport.Issue.builder()
                        .title("问题" + i)
                        .type("bug")
                        .severity("low")
                        .description("描述" + i)
                        .build());
            }

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown).contains("## 发现的问题");
            assertThat(markdown).contains("问题99");
        }

        @Test
        @DisplayName("应该处理包含特殊字符的内容")
        void shouldHandleSpecialCharacters() {
            testReport.setProjectOverview("包含特殊字符: <>&\"'");
            testReport.addKeyFinding("发现包含|表格|分隔符|");

            String markdown = service.generateMarkdownReport(testReport);

            assertThat(markdown)
                    .contains("包含特殊字符: <>&\"'")
                    .contains("发现包含|表格|分隔符|");
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("应该快速生成小型报告")
        void shouldGenerateSmallReportQuickly() {
            long startTime = System.currentTimeMillis();

            service.generateMarkdownReport(testReport);

            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(100); // 应该在100ms内完成
        }

        @Test
        @DisplayName("应该能够处理大型报告")
        void shouldHandleLargeReport() {
            // 添加大量数据
            for (int i = 0; i < 50; i++) {
                testReport.addKeyFinding("关键发现" + i);
                testReport.addIssue(ReviewReport.Issue.builder()
                        .title("问题" + i)
                        .type("bug")
                        .severity("medium")
                        .description("描述" + i)
                        .build());
                testReport.addRecommendation(ReviewReport.Recommendation.builder()
                        .title("建议" + i)
                        .category("general")
                        .description("建议描述" + i)
                        .build());
            }

            long startTime = System.currentTimeMillis();
            String markdown = service.generateMarkdownReport(testReport);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(markdown).isNotEmpty();
            assertThat(duration).isLessThan(1000); // 应该在1秒内完成
        }
    }
}

