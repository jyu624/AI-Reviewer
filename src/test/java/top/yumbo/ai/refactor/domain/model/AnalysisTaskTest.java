package top.yumbo.ai.refactor.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * AnalysisTask领域模型单元测试
 */
@DisplayName("AnalysisTask领域模型测试")
class AnalysisTaskTest {

    private AnalysisTask task;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .name("TestProject")
                .rootPath(Paths.get("/test"))
                .type(ProjectType.JAVA)
                .build();

        task = AnalysisTask.builder()
                .taskId("task-123")
                .project(testProject)
                .configuration(AnalysisConfiguration.builder().build())
                .build();
    }

    @Nested
    @DisplayName("初始状态测试")
    class InitialStateTest {

        @Test
        @DisplayName("新创建的任务应该是PENDING状态")
        void shouldBePendingStatusWhenCreated() {
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.PENDING);
        }

        @Test
        @DisplayName("新创建的任务应该有createdAt时间")
        void shouldHaveCreatedAtTime() {
            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("新创建的任务的startedAt应该为null")
        void shouldHaveNullStartedAt() {
            assertThat(task.getStartedAt()).isNull();
        }

        @Test
        @DisplayName("新创建的任务的completedAt应该为null")
        void shouldHaveNullCompletedAt() {
            assertThat(task.getCompletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("start()方法测试")
    class StartMethodTest {

        @Test
        @DisplayName("启动任务应该改变状态为RUNNING")
        void shouldChangeStatusToRunningWhenStarted() {
            task.start();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.RUNNING);
        }

        @Test
        @DisplayName("启动任务应该设置startedAt时间")
        void shouldSetStartedAtTimeWhenStarted() {
            LocalDateTime beforeStart = LocalDateTime.now();

            task.start();

            assertThat(task.getStartedAt()).isNotNull();
            assertThat(task.getStartedAt()).isAfterOrEqualTo(beforeStart);
            assertThat(task.getStartedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("启动任务应该初始化进度对象")
        void shouldInitializeProgressWhenStarted() {
            task.start();

            assertThat(task.getProgress()).isNotNull();
        }

        @Test
        @DisplayName("已经运行的任务不应该重复启动")
        void shouldNotRestartRunningTask() {
            task.start();
            LocalDateTime firstStartTime = task.getStartedAt();

            // 尝试重新启动
            task.start();

            assertThat(task.getStartedAt()).isEqualTo(firstStartTime);
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.RUNNING);
        }
    }

    @Nested
    @DisplayName("complete()方法测试")
    class CompleteMethodTest {

        @Test
        @DisplayName("完成任务应该改变状态为COMPLETED")
        void shouldChangeStatusToCompletedWhenCompleted() {
            task.start();
            task.complete();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.COMPLETED);
        }

        @Test
        @DisplayName("完成任务应该设置completedAt时间")
        void shouldSetCompletedAtTimeWhenCompleted() {
            task.start();
            LocalDateTime beforeComplete = LocalDateTime.now();

            task.complete();

            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.getCompletedAt()).isAfterOrEqualTo(beforeComplete);
        }

        @Test
        @DisplayName("完成任务应该标记进度为完成")
        void shouldMarkProgressAsCompletedWhenTaskCompleted() {
            task.start();
            task.complete();

            assertThat(task.getProgress().isCompleted()).isTrue();
        }

        @Test
        @DisplayName("未启动的任务不应该完成")
        void shouldNotCompletePendingTask() {
            task.complete();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.PENDING);
            assertThat(task.getCompletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("fail()方法测试")
    class FailMethodTest {

        @Test
        @DisplayName("失败应该改变状态为FAILED")
        void shouldChangeStatusToFailedWhenFailed() {
            task.start();
            task.fail("Test error", new RuntimeException("Test"));

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.FAILED);
        }

        @Test
        @DisplayName("失败应该设置错误消息")
        void shouldSetErrorMessageWhenFailed() {
            task.start();
            task.fail("Test error message", new RuntimeException());

            assertThat(task.getErrorMessage()).isEqualTo("Test error message");
        }

        @Test
        @DisplayName("失败应该设置异常对象")
        void shouldSetExceptionWhenFailed() {
            task.start();
            Exception testException = new RuntimeException("Test");

            task.fail("Error", testException);

            assertThat(task.getException()).isEqualTo(testException);
        }

        @Test
        @DisplayName("失败应该设置completedAt时间")
        void shouldSetCompletedAtTimeWhenFailed() {
            task.start();
            LocalDateTime beforeFail = LocalDateTime.now();

            task.fail("Error", null);

            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.getCompletedAt()).isAfterOrEqualTo(beforeFail);
        }
    }

    @Nested
    @DisplayName("cancel()方法测试")
    class CancelMethodTest {

        @Test
        @DisplayName("取消运行中的任务应该改变状态为CANCELLED")
        void shouldCancelRunningTask() {
            task.start();
            task.cancel();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.CANCELLED);
        }

        @Test
        @DisplayName("取消等待中的任务应该改变状态为CANCELLED")
        void shouldCancelPendingTask() {
            task.cancel();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.CANCELLED);
        }

        @Test
        @DisplayName("取消任务应该设置completedAt时间")
        void shouldSetCompletedAtTimeWhenCancelled() {
            task.start();
            LocalDateTime beforeCancel = LocalDateTime.now();

            task.cancel();

            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.getCompletedAt()).isAfterOrEqualTo(beforeCancel);
        }

        @Test
        @DisplayName("已完成的任务不应该被取消")
        void shouldNotCancelCompletedTask() {
            task.start();
            task.complete();

            task.cancel();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("状态查询方法测试")
    class StatusQueryMethodsTest {

        @Test
        @DisplayName("isRunning()应该正确返回运行状态")
        void shouldReturnTrueWhenRunning() {
            assertThat(task.isRunning()).isFalse();

            task.start();
            assertThat(task.isRunning()).isTrue();

            task.complete();
            assertThat(task.isRunning()).isFalse();
        }

        @Test
        @DisplayName("isCompleted()应该正确返回完成状态")
        void shouldReturnTrueWhenCompleted() {
            assertThat(task.isCompleted()).isFalse();

            task.start();
            assertThat(task.isCompleted()).isFalse();

            task.complete();
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed()应该正确返回失败状态")
        void shouldReturnTrueWhenFailed() {
            assertThat(task.isFailed()).isFalse();

            task.start();
            assertThat(task.isFailed()).isFalse();

            task.fail("Error", null);
            assertThat(task.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("getDurationMillis()方法测试")
    class GetDurationMillisTest {

        @Test
        @DisplayName("未启动的任务耗时应该为0")
        void shouldReturnZeroForPendingTask() {
            assertThat(task.getDurationMillis()).isZero();
        }

        @Test
        @DisplayName("已完成任务应该返回正确的耗时")
        void shouldReturnCorrectDurationForCompletedTask() throws InterruptedException {
            task.start();
            Thread.sleep(100); // 等待至少100ms
            task.complete();

            long duration = task.getDurationMillis();

            assertThat(duration).isGreaterThanOrEqualTo(100);
            assertThat(duration).isLessThan(1000); // 不应该超过1秒
        }

        @Test
        @DisplayName("运行中的任务应该返回当前耗时")
        void shouldReturnCurrentDurationForRunningTask() throws InterruptedException {
            task.start();
            Thread.sleep(100);

            long duration = task.getDurationMillis();

            assertThat(duration).isGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("状态转换测试")
    class StateTransitionTest {

        @Test
        @DisplayName("完整的成功流程：PENDING → RUNNING → COMPLETED")
        void shouldFollowSuccessFlow() {
            // PENDING
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.PENDING);
            assertThat(task.isRunning()).isFalse();
            assertThat(task.isCompleted()).isFalse();

            // RUNNING
            task.start();
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.RUNNING);
            assertThat(task.isRunning()).isTrue();
            assertThat(task.isCompleted()).isFalse();

            // COMPLETED
            task.complete();
            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.COMPLETED);
            assertThat(task.isRunning()).isFalse();
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("失败流程：PENDING → RUNNING → FAILED")
        void shouldFollowFailureFlow() {
            task.start();
            task.fail("Error", null);

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.FAILED);
            assertThat(task.isRunning()).isFalse();
            assertThat(task.isFailed()).isTrue();
        }

        @Test
        @DisplayName("取消流程：PENDING → CANCELLED")
        void shouldFollowCancelFlow() {
            task.cancel();

            assertThat(task.getStatus()).isEqualTo(AnalysisTask.AnalysisStatus.CANCELLED);
        }
    }
}
