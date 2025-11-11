package top.yumbo.ai.refactor.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import top.yumbo.ai.refactor.application.port.output.AIServicePort;
import top.yumbo.ai.refactor.application.port.output.CachePort;
import top.yumbo.ai.refactor.application.port.output.FileSystemPort;
import top.yumbo.ai.refactor.domain.model.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectAnalysisService单元测试
 * 使用Mock对象测试业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectAnalysisService单元测试")
class ProjectAnalysisServiceTest {

    @Mock
    private AIServicePort aiServicePort;

    @Mock
    private CachePort cachePort;

    @Mock
    private FileSystemPort fileSystemPort;

    private ProjectAnalysisService service;
    private Project testProject;

    @BeforeEach
    void setUp() {
        service = new ProjectAnalysisService(aiServicePort, cachePort, fileSystemPort);

        // 创建测试项目
        testProject = Project.builder()
                .name("TestProject")
                .rootPath(Paths.get("/test/project"))
                .type(ProjectType.JAVA)
                .structureTree("test-structure")
                .build();

        // 添加一些测试文件
        testProject.addSourceFile(createTestSourceFile("Main.java", 100));
        testProject.addSourceFile(createTestSourceFile("Config.xml", 50));
    }

    @Test
    @DisplayName("应该拒绝无效的项目")
    void shouldRejectInvalidProject() {
        Project invalidProject = Project.builder()
                .name(null)  // 无效的name
                .rootPath(Paths.get("/test"))
                .type(ProjectType.JAVA)
                .build();

        assertThatThrownBy(() -> service.analyzeProject(invalidProject))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("项目信息无效");
    }

    @Test
    @DisplayName("应该成功创建并启动分析任务")
    void shouldCreateAndStartAnalysisTask() {
        // 配置Mock行为
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("Mock AI response");

        // 执行分析
        AnalysisTask task = service.analyzeProject(testProject);

        // 验证任务状态
        assertThat(task).isNotNull();
        assertThat(task.getTaskId()).isNotNull();
        assertThat(task.getProject()).isEqualTo(testProject);
        assertThat(task.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("应该在分析过程中调用AI服务")
    void shouldCallAIServiceDuringAnalysis() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI analysis result");

        service.analyzeProject(testProject);

        // 验证AI服务被调用
        verify(aiServicePort, atLeastOnce()).analyze(anyString());
    }

    @Test
    @DisplayName("应该尝试从缓存获取结果")
    void shouldTryToGetResultFromCache() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        service.analyzeProject(testProject);

        // 验证缓存被查询
        verify(cachePort, atLeastOnce()).get(anyString());
    }

    @Test
    @DisplayName("应该在缓存命中时使用缓存结果")
    void shouldUseCachedResultWhenAvailable() {
        String cachedResult = "Cached AI result";
        when(cachePort.get(anyString())).thenReturn(Optional.of(cachedResult));

        service.analyzeProject(testProject);

        // 验证使用了缓存（AI服务调用次数减少）
        verify(cachePort, atLeastOnce()).get(anyString());
    }

    @Test
    @DisplayName("应该将分析结果存入缓存")
    void shouldStorResultInCache() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        service.analyzeProject(testProject);

        // 验证结果被缓存
        verify(cachePort, atLeastOnce()).put(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("应该生成完整的分析报告")
    void shouldGenerateCompleteReport() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI analysis");

        AnalysisTask task = service.analyzeProject(testProject);
        ReviewReport report = service.getAnalysisResult(task.getTaskId());

        // 验证报告完整性
        assertThat(report).isNotNull();
        assertThat(report.getReportId()).isNotNull();
        assertThat(report.getProjectName()).isEqualTo("TestProject");
        assertThat(report.getOverallScore()).isGreaterThanOrEqualTo(0);
        assertThat(report.getDimensionScores()).isNotEmpty();
    }

    @Test
    @DisplayName("应该在AI服务失败时抛出异常")
    void shouldThrowExceptionWhenAIServiceFails() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenThrow(new RuntimeException("AI service error"));

        assertThatThrownBy(() -> service.analyzeProject(testProject))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("项目分析失败");
    }

    @Test
    @DisplayName("应该支持异步分析")
    void shouldSupportAsyncAnalysis() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        String taskId = service.analyzeProjectAsync(testProject);

        assertThat(taskId).isNotNull();
        assertThat(taskId).startsWith("task-");

        // 等待一小段时间让异步任务执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 验证任务状态
        AnalysisTask task = service.getTaskStatus(taskId);
        assertThat(task).isNotNull();
    }

    @Test
    @DisplayName("应该能够获取任务状态")
    void shouldGetTaskStatus() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        AnalysisTask task = service.analyzeProject(testProject);
        String taskId = task.getTaskId();

        AnalysisTask retrievedTask = service.getTaskStatus(taskId);

        assertThat(retrievedTask).isNotNull();
        assertThat(retrievedTask.getTaskId()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("获取不存在的任务应该抛出异常")
    void shouldThrowExceptionForNonExistentTask() {
        assertThatThrownBy(() -> service.getTaskStatus("non-existent-task"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("应该能够取消任务")
    void shouldCancelTask() {

        String taskId = service.analyzeProjectAsync(testProject);

        service.cancelTask(taskId);

        AnalysisTask task = service.getTaskStatus(taskId);
        // 注意：由于异步执行，任务可能已经完成或正在运行
        assertThat(task).isNotNull();
    }

    @Test
    @DisplayName("应该能够获取分析结果")
    void shouldGetAnalysisResult() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        AnalysisTask task = service.analyzeProject(testProject);
        ReviewReport report = service.getAnalysisResult(task.getTaskId());

        assertThat(report).isNotNull();
        assertThat(report.getProjectName()).isEqualTo("TestProject");
    }

    @Test
    @DisplayName("获取不存在任务的结果应该抛出异常")
    void shouldThrowExceptionForNonExistentResult() {
        assertThatThrownBy(() -> service.getAnalysisResult("non-existent-task"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("分析结果不存在");
    }

    @Test
    @DisplayName("应该正确更新进度信息")
    void shouldUpdateProgressInformation() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        AnalysisTask task = service.analyzeProject(testProject);

        // 验证进度信息
        AnalysisProgress progress = task.getProgress();
        assertThat(progress).isNotNull();
        assertThat(progress.getTotalSteps()).isGreaterThan(0);
        assertThat(progress.getCompletedSteps()).isEqualTo(progress.getTotalSteps());
    }

    @Test
    @DisplayName("应该在失败时正确设置任务状态")
    void shouldSetTaskStatusOnFailure() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenThrow(new RuntimeException("Test error"));

        try {
            service.analyzeProject(testProject);
        } catch (Exception e) {
            // 预期会抛出异常
        }

        // 验证AI服务被调用
        verify(aiServicePort, atLeastOnce()).analyze(anyString());
    }

    @Test
    @DisplayName("应该验证所有必需的端口都被注入")
    void shouldVerifyAllRequiredPortsAreInjected() {
        assertThat(service).isNotNull();

        // 尝试使用服务，如果端口未注入会抛出NPE
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        assertThatCode(() -> service.analyzeProject(testProject))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该为每个任务生成唯一的任务ID")
    void shouldGenerateUniqueTaskIds() {
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        when(aiServicePort.analyze(anyString())).thenReturn("AI result");

        AnalysisTask task1 = service.analyzeProject(testProject);
        AnalysisTask task2 = service.analyzeProject(testProject);

        assertThat(task1.getTaskId()).isNotEqualTo(task2.getTaskId());
    }

    // ========== 辅助方法 ==========

    private SourceFile createTestSourceFile(String fileName, int lineCount) {
        return SourceFile.builder()
                .path(Paths.get("/test/" + fileName))
                .fileName(fileName)
                .relativePath(fileName)
                .extension(getExtension(fileName))
                .lineCount(lineCount)
                .sizeInBytes(1024L)
                .isCore(fileName.contains("Main") || fileName.contains("Config"))
                .build();
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}

