package top.yumbo.ai.reviewer.integration.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import top.yumbo.ai.reviewer.domain.model.*;
import top.yumbo.ai.reviewer.domain.model.SourceFile.FileCategory;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 领域模型集成测试
 * 测试领域模型之间的协作和交互
 */
@DisplayName("领域模型集成测试")
class DomainModelIntegrationTest {

    private Project testProject;
    private List<SourceFile> sourceFiles;

    @BeforeEach
    void setUp() {
        sourceFiles = createTestSourceFiles();
        testProject = createTestProject(sourceFiles);
    }

    @Nested
    @DisplayName("Project + SourceFile协作测试")
    class ProjectSourceFileIntegrationTest {

        @Test
        @DisplayName("Project应该正确管理SourceFile集合")
        void shouldManageSourceFilesCorrectly() {
            // 验证：Project应该包含所有源文件
            assertThat(testProject.getSourceFiles()).hasSize(5);
            assertThat(testProject.getSourceFiles()).containsAll(sourceFiles);
        }

        @Test
        @DisplayName("Project应该能够添加新的SourceFile")
        void shouldAddNewSourceFile() {
            // 准备
            SourceFile newFile = SourceFile.builder()
                    .path(Paths.get("/test/NewFile.java"))
                    .fileName("NewFile.java")
                    .extension("java")
                    .relativePath("NewFile.java")
                    .lineCount(50)
                    .sizeInBytes(1500L)
                    .isCore(false)
                    .category(FileCategory.SOURCE_CODE)
                    .build();

            int originalSize = testProject.getSourceFiles().size();

            // 执行
            testProject.addSourceFile(newFile);

            // 验证
            assertThat(testProject.getSourceFiles()).hasSize(originalSize + 1);
            assertThat(testProject.getSourceFiles()).contains(newFile);
        }

        @Test
        @DisplayName("Project应该正确计算总行数")
        void shouldCalculateTotalLinesCorrectly() {
            // 计算预期值
            int expectedLines = sourceFiles.stream()
                    .mapToInt(SourceFile::getLineCount)
                    .sum();

            // 验证
            assertThat(testProject.getTotalLines()).isEqualTo(expectedLines);
        }

        @Test
        @DisplayName("Project应该正确筛选核心文件")
        void shouldFilterCoreFilesCorrectly() {
            // 计算预期值
            long expectedCoreCount = sourceFiles.stream()
                    .filter(SourceFile::isCore)
                    .count();

            // 验证
            List<SourceFile> coreFiles = testProject.getCoreFiles();
            assertThat(coreFiles).hasSize((int) expectedCoreCount);
            assertThat(coreFiles).allMatch(SourceFile::isCore);
        }

        @Test
        @DisplayName("Project应该根据SourceFile推断语言")
        void shouldInferLanguageFromSourceFiles() {
            // 执行
            String language = testProject.getLanguage();

            // 验证：应该是Java项目（因为大部分是.java文件）
            assertThat(language).isEqualTo("java");
        }
    }

    @Nested
    @DisplayName("AnalysisTask + Project协作测试")
    class AnalysisTaskProjectIntegrationTest {

        @Test
        @DisplayName("AnalysisTask应该关联Project")
        void shouldAssociateWithProject() {
            // 准备
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-001")
                    .project(testProject)
                    .build();

            // 验证
            assertThat(task.getProject()).isEqualTo(testProject);
            assertThat(task.getProject().getName()).isEqualTo("TestProject");
        }

        @Test
        @DisplayName("AnalysisTask应该正确管理分析进度")
        void shouldManageAnalysisProgress() {
            // 准备
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(100);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-002")
                    .project(testProject)
                    .progress(progress)
                    .build();

            // 执行：启动任务
            task.start();

            // 验证：状态和进度应该初始化
            assertThat(task.isRunning()).isTrue();
            assertThat(task.getStartedAt()).isNotNull();
            assertThat(task.getProgress()).isNotNull();
        }

        @Test
        @DisplayName("AnalysisTask完成时应该标记Progress为完成")
        void shouldMarkProgressAsCompletedWhenTaskCompletes() {
            // 准备
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(10);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-003")
                    .project(testProject)
                    .progress(progress)
                    .build();

            // 执行
            task.start();

            // 模拟进度更新
            for (int i = 0; i < 10; i++) {
                progress.incrementCompleted();
            }

            task.complete();

            // 验证
            assertThat(task.isCompleted()).isTrue();
            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(progress.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("AnalysisTask应该正确记录任务耗时")
        void shouldRecordTaskDuration() throws InterruptedException {
            // 准备
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-004")
                    .project(testProject)
                    .build();

            // 执行
            task.start();
            Thread.sleep(100); // 等待100ms
            task.complete();

            // 验证：耗时应该大于等于100ms
            assertThat(task.getDurationMillis()).isGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("ReviewReport + AnalysisTask协作测试")
    class ReviewReportAnalysisTaskIntegrationTest {

        @Test
        @DisplayName("应该能够为完成的任务生成报告")
        void shouldGenerateReportForCompletedTask() {
            // 准备
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-005")
                    .project(testProject)
                    .build();

            task.start();
            task.complete();

            // 执行：生成报告
            ReviewReport report = ReviewReport.builder()
                    .reportId("report-001")
                    .projectName(task.getProject().getName())
                    .projectPath(task.getProject().getRootPath().toString())
                    .overallScore(85)
                    .generatedAt(LocalDateTime.now())
                    .build();

            // 验证：报告应该关联正确的项目信息
            assertThat(report.getProjectName()).isEqualTo(testProject.getName());
            assertThat(report.getProjectPath()).isEqualTo(testProject.getRootPath().toString());
        }

        @Test
        @DisplayName("报告的生成时间应该在任务完成之后")
        void shouldGenerateReportAfterTaskCompletion() throws InterruptedException {
            // 准备
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-006")
                    .project(testProject)
                    .build();

            task.start();
            Thread.sleep(50);
            task.complete();

            // 执行：生成报告
            LocalDateTime reportTime = LocalDateTime.now();
            ReviewReport report = ReviewReport.builder()
                    .reportId("report-002")
                    .projectName(task.getProject().getName())
                    .projectPath(task.getProject().getRootPath().toString())
                    .overallScore(90)
                    .generatedAt(reportTime)
                    .build();

            // 验证：报告时间应该晚于任务完成时间
            assertThat(report.getGeneratedAt()).isAfterOrEqualTo(task.getCompletedAt());
        }
    }

    @Nested
    @DisplayName("AnalysisProgress + AnalysisTask状态同步测试")
    class AnalysisProgressTaskSyncTest {

        @Test
        @DisplayName("任务启动时应该初始化进度")
        void shouldInitializeProgressWhenTaskStarts() {
            // 准备
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-007")
                    .project(testProject)
                    .build();

            // 执行
            task.start();

            // 验证：进度应该被创建
            assertThat(task.getProgress()).isNotNull();
            assertThat(task.getProgress().getCurrentPhase()).isEqualTo("初始化");
        }

        @Test
        @DisplayName("任务进度应该实时更新")
        void shouldUpdateProgressInRealTime() {
            // 准备
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(10);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-008")
                    .project(testProject)
                    .progress(progress)
                    .build();

            task.start();

            // 执行：更新进度
            progress.updatePhase("扫描文件");
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();

            // 验证
            assertThat(progress.getCurrentPhase()).isEqualTo("扫描文件");
            assertThat(progress.getCompletedSteps()).isEqualTo(3);
            assertThat(progress.getPercentage()).isCloseTo(30.0, within(0.1));
        }

        @Test
        @DisplayName("任务完成时进度应该达到100%")
        void shouldReach100PercentWhenTaskCompletes() {
            // 准备
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(5);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-009")
                    .project(testProject)
                    .progress(progress)
                    .build();

            task.start();

            // 执行：完成所有步骤
            for (int i = 0; i < 5; i++) {
                progress.incrementCompleted();
            }
            task.complete();

            // 验证
            assertThat(progress.isCompleted()).isTrue();
            assertThat(progress.getPercentage()).isEqualTo(100.0);
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("任务取消时进度应该停止")
        void shouldStopProgressWhenTaskIsCancelled() {
            // 准备
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(10);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-010")
                    .project(testProject)
                    .progress(progress)
                    .build();

            task.start();

            // 执行：部分完成后取消
            progress.incrementCompleted();
            progress.incrementCompleted();
            int stepsBeforeCancel = progress.getCompletedSteps();

            task.cancel();

            // 验证：进度不应该继续增加
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.CANCELLED);
            assertThat(progress.getCompletedSteps()).isEqualTo(stepsBeforeCancel);
        }
    }

    @Nested
    @DisplayName("完整对象图集成测试")
    class CompleteObjectGraphIntegrationTest {

        @Test
        @DisplayName("应该创建完整的分析对象图")
        void shouldCreateCompleteAnalysisObjectGraph() {
            // 准备：创建完整的对象图
            // Project ← SourceFiles
            Project project = createTestProject(sourceFiles);

            // AnalysisProgress
            AnalysisProgress progress = new AnalysisProgress();
            progress.setTotalSteps(100);

            // AnalysisConfiguration
            AnalysisConfiguration config = AnalysisConfiguration.builder()
                    .enableCaching(true)
                    .maxConcurrency(3)
                    .build();

            // AnalysisTask ← Project, Progress, Configuration
            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-complete")
                    .project(project)
                    .progress(progress)
                    .configuration(config)
                    .build();

            // 执行：模拟完整流程
            task.start();

            // 使用task创建的progress对象（start()会创建新的）
            AnalysisProgress actualProgress = task.getProgress();
            actualProgress.setTotalSteps(100);

            // 更新进度
            actualProgress.updatePhase("扫描文件");
            actualProgress.setCompletedSteps(20);

            actualProgress.updatePhase("AI分析");
            actualProgress.setCompletedSteps(60);

            actualProgress.updatePhase("生成报告");
            actualProgress.setCompletedSteps(100);

            task.complete();

            // 生成报告
            ReviewReport report = ReviewReport.builder()
                    .reportId("report-complete")
                    .projectName(project.getName())
                    .projectPath(project.getRootPath().toString())
                    .overallScore(88)
                    .generatedAt(LocalDateTime.now())
                    .build();

            // 验证：对象图完整性
            assertThat(task.getProject()).isEqualTo(project);
            assertThat(task.getProgress()).isNotNull();
            assertThat(task.getConfiguration()).isEqualTo(config);
            assertThat(task.isCompleted()).isTrue();

            assertThat(project.getSourceFiles()).hasSize(5);
            assertThat(actualProgress.isCompleted()).isTrue();
            assertThat(report.getProjectName()).isEqualTo(project.getName());
        }

        @Test
        @DisplayName("对象之间的关联应该保持一致性")
        void shouldMaintainConsistencyBetweenObjects() {
            // 准备
            Project project = createTestProject(sourceFiles);

            AnalysisTask task = AnalysisTask.builder()
                    .taskId("task-consistency")
                    .project(project)
                    .build();

            ReviewReport report = ReviewReport.builder()
                    .reportId("report-consistency")
                    .projectName(project.getName())
                    .projectPath(project.getRootPath().toString())
                    .overallScore(85)
                    .generatedAt(LocalDateTime.now())
                    .build();

            // 验证：数据一致性
            assertThat(task.getProject().getName())
                    .isEqualTo(report.getProjectName());

            assertThat(task.getProject().getRootPath().toString())
                    .isEqualTo(report.getProjectPath());

            assertThat(task.getProject().getSourceFiles().size())
                    .isEqualTo(project.getSourceFiles().size());
        }
    }

    // ============= 辅助方法 =============

    /**
     * 创建测试用的SourceFile列表
     */
    private List<SourceFile> createTestSourceFiles() {
        List<SourceFile> files = new ArrayList<>();

        files.add(SourceFile.builder()
                .path(Paths.get("/test/Main.java"))
                .fileName("Main.java")
                .extension("java")
                .relativePath("Main.java")
                .lineCount(100)
                .sizeInBytes(3000L)
                .isCore(true)
                .category(FileCategory.ENTRY_POINT)
                .build());

        files.add(SourceFile.builder()
                .path(Paths.get("/test/Service.java"))
                .fileName("Service.java")
                .extension("java")
                .relativePath("Service.java")
                .lineCount(200)
                .sizeInBytes(6000L)
                .isCore(true)
                .category(FileCategory.SOURCE_CODE)
                .build());

        files.add(SourceFile.builder()
                .path(Paths.get("/test/Controller.java"))
                .fileName("Controller.java")
                .extension("java")
                .relativePath("Controller.java")
                .lineCount(150)
                .sizeInBytes(4500L)
                .isCore(true)
                .category(FileCategory.SOURCE_CODE)
                .build());

        files.add(SourceFile.builder()
                .path(Paths.get("/test/config.yaml"))
                .fileName("config.yaml")
                .extension("yaml")
                .relativePath("config.yaml")
                .lineCount(50)
                .sizeInBytes(1000L)
                .isCore(false)
                .category(FileCategory.CONFIG)
                .build());

        files.add(SourceFile.builder()
                .path(Paths.get("/test/Test.java"))
                .fileName("Test.java")
                .extension("java")
                .relativePath("Test.java")
                .lineCount(80)
                .sizeInBytes(2400L)
                .isCore(false)
                .category(FileCategory.TEST)
                .build());

        return files;
    }

    /**
     * 创建测试Project
     */
    private Project createTestProject(List<SourceFile> files) {
        return Project.builder()
                .name("TestProject")
                .rootPath(Paths.get("/test"))
                .type(ProjectType.JAVA)  // 添加type字段
                .sourceFiles(files)
                .build();
    }
}

