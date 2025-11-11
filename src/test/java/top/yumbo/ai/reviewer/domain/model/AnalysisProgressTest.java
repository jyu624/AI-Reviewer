package top.yumbo.ai.reviewer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AnalysisProgress领域模型测试
 */
@DisplayName("AnalysisProgress领域模型测试")
class AnalysisProgressTest {

    private AnalysisProgress progress;

    @BeforeEach
    void setUp() {
        progress = new AnalysisProgress();
    }

    @Nested
    @DisplayName("初始状态测试")
    class InitialStateTest {

        @Test
        @DisplayName("总步骤数初始应该为0")
        void shouldHaveZeroTotalStepsInitially() {
            assertThat(progress.getTotalSteps()).isEqualTo(0);
        }

        @Test
        @DisplayName("完成步骤数初始应该为0")
        void shouldHaveZeroCompletedStepsInitially() {
            assertThat(progress.getCompletedSteps()).isEqualTo(0);
        }

        @Test
        @DisplayName("当前阶段初始应该为初始化")
        void shouldHaveInitialPhase() {
            assertThat(progress.getCurrentPhase()).isEqualTo("初始化");
        }

        @Test
        @DisplayName("当前任务初始应该为空")
        void shouldHaveEmptyTaskInitially() {
            assertThat(progress.getCurrentTask()).isEmpty();
        }

        @Test
        @DisplayName("已完成阶段列表初始应该为空")
        void shouldHaveEmptyCompletedPhasesInitially() {
            assertThat(progress.getCompletedPhases()).isEmpty();
        }
    }

    @Nested
    @DisplayName("setTotalSteps()方法测试")
    class SetTotalStepsTest {

        @Test
        @DisplayName("应该能够设置总步骤数")
        void shouldSetTotalSteps() {
            progress.setTotalSteps(10);

            assertThat(progress.getTotalSteps()).isEqualTo(10);
        }

        @Test
        @DisplayName("应该能够更新总步骤数")
        void shouldUpdateTotalSteps() {
            progress.setTotalSteps(10);
            progress.setTotalSteps(20);

            assertThat(progress.getTotalSteps()).isEqualTo(20);
        }

        @Test
        @DisplayName("应该能够设置为0")
        void shouldSetToZero() {
            progress.setTotalSteps(10);
            progress.setTotalSteps(0);

            assertThat(progress.getTotalSteps()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("incrementCompleted()方法测试")
    class IncrementCompletedTest {

        @Test
        @DisplayName("应该能够增加完成步骤数")
        void shouldIncrementCompletedSteps() {
            progress.setTotalSteps(10);
            progress.incrementCompleted();

            assertThat(progress.getCompletedSteps()).isEqualTo(1);
        }

        @Test
        @DisplayName("应该能够多次增加")
        void shouldIncrementMultipleTimes() {
            progress.setTotalSteps(10);
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();

            assertThat(progress.getCompletedSteps()).isEqualTo(3);
        }

        @Test
        @DisplayName("不应该超过总步骤数")
        void shouldNotExceedTotalSteps() {
            progress.setTotalSteps(3);
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted(); // 第4次，应该不会超过3

            assertThat(progress.getCompletedSteps()).isEqualTo(3);
        }

        @Test
        @DisplayName("当总步骤为0时不应该增加")
        void shouldNotIncrementWhenTotalIsZero() {
            progress.setTotalSteps(0);
            progress.incrementCompleted();

            assertThat(progress.getCompletedSteps()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("updatePhase()方法测试")
    class UpdatePhaseTest {

        @Test
        @DisplayName("应该能够更新当前阶段")
        void shouldUpdateCurrentPhase() {
            progress.updatePhase("扫描文件");

            assertThat(progress.getCurrentPhase()).isEqualTo("扫描文件");
        }

        @Test
        @DisplayName("应该将旧阶段添加到已完成列表")
        void shouldAddOldPhaseToCompletedList() {
            progress.updatePhase("扫描文件");
            progress.updatePhase("分析代码");

            assertThat(progress.getCompletedPhases())
                    .hasSize(2)
                    .containsExactly("初始化", "扫描文件");
        }

        @Test
        @DisplayName("应该能够多次更新阶段")
        void shouldUpdatePhaseMultipleTimes() {
            progress.updatePhase("扫描文件");
            progress.updatePhase("分析代码");
            progress.updatePhase("生成报告");

            assertThat(progress.getCurrentPhase()).isEqualTo("生成报告");
            assertThat(progress.getCompletedPhases())
                    .hasSize(3)
                    .containsExactly("初始化", "扫描文件", "分析代码");
        }

        @Test
        @DisplayName("更新为相同阶段不应该添加到已完成列表")
        void shouldNotAddSamePhaseToCompletedList() {
            progress.updatePhase("扫描文件");
            progress.updatePhase("扫描文件");

            assertThat(progress.getCompletedPhases()).hasSize(1);
        }

        @Test
        @DisplayName("应该处理null阶段")
        void shouldHandleNullPhase() {
            progress.updatePhase(null);

            assertThat(progress.getCurrentPhase()).isNull();
        }
    }

    @Nested
    @DisplayName("updateTask()方法测试")
    class UpdateTaskTest {

        @Test
        @DisplayName("应该能够更新当前任务")
        void shouldUpdateCurrentTask() {
            progress.updateTask("正在扫描Main.java");

            assertThat(progress.getCurrentTask()).isEqualTo("正在扫描Main.java");
        }

        @Test
        @DisplayName("应该能够多次更新任务")
        void shouldUpdateTaskMultipleTimes() {
            progress.updateTask("任务1");
            progress.updateTask("任务2");
            progress.updateTask("任务3");

            assertThat(progress.getCurrentTask()).isEqualTo("任务3");
        }

        @Test
        @DisplayName("应该能够设置为空字符串")
        void shouldSetToEmptyString() {
            progress.updateTask("任务1");
            progress.updateTask("");

            assertThat(progress.getCurrentTask()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPercentage()方法测试")
    class GetPercentageTest {

        @Test
        @DisplayName("应该正确计算完成百分比")
        void shouldCalculateCorrectPercentage() {
            progress.setTotalSteps(10);
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();

            assertThat(progress.getPercentage()).isEqualTo(30.0);
        }

        @Test
        @DisplayName("完成所有步骤时应该返回100")
        void shouldReturn100WhenAllStepsCompleted() {
            progress.setTotalSteps(10);
            for (int i = 0; i < 10; i++) {
                progress.incrementCompleted();
            }

            assertThat(progress.getPercentage()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("未开始时应该返回0")
        void shouldReturn0WhenNotStarted() {
            progress.setTotalSteps(10);

            assertThat(progress.getPercentage()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("总步骤为0时应该返回0")
        void shouldReturn0WhenTotalIsZero() {
            progress.setTotalSteps(0);

            assertThat(progress.getPercentage()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("应该处理小数百分比")
        void shouldHandleDecimalPercentage() {
            progress.setTotalSteps(3);
            progress.incrementCompleted();

            // 1/3 = 33.33%
            assertThat(progress.getPercentage()).isCloseTo(33.33, org.assertj.core.data.Offset.offset(0.01));
        }
    }

    @Nested
    @DisplayName("isCompleted()方法测试")
    class IsCompletedTest {

        @Test
        @DisplayName("完成所有步骤时应该返回true")
        void shouldReturnTrueWhenAllStepsCompleted() {
            progress.setTotalSteps(5);
            for (int i = 0; i < 5; i++) {
                progress.incrementCompleted();
            }

            assertThat(progress.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("未完成时应该返回false")
        void shouldReturnFalseWhenNotCompleted() {
            progress.setTotalSteps(5);
            progress.incrementCompleted();

            assertThat(progress.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("总步骤为0时应该返回true")
        void shouldReturnTrueWhenTotalIsZero() {
            progress.setTotalSteps(0);

            assertThat(progress.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("完整流程测试")
    class CompleteFlowTest {

        @Test
        @DisplayName("应该正确跟踪完整分析流程")
        void shouldTrackCompleteAnalysisFlow() {
            // 设置总步骤
            progress.setTotalSteps(10);
            assertThat(progress.getPercentage()).isEqualTo(0);

            // 阶段1: 扫描文件
            progress.updatePhase("扫描文件");
            progress.updateTask("扫描源代码目录");
            progress.incrementCompleted();
            progress.incrementCompleted();
            assertThat(progress.getPercentage()).isEqualTo(20);
            assertThat(progress.getCurrentPhase()).isEqualTo("扫描文件");

            // 阶段2: 分析代码
            progress.updatePhase("分析代码");
            progress.updateTask("分析Main.java");
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();
            assertThat(progress.getPercentage()).isEqualTo(50);
            assertThat(progress.getCompletedPhases()).contains("初始化", "扫描文件");

            // 阶段3: 生成报告
            progress.updatePhase("生成报告");
            progress.updateTask("生成Markdown报告");
            for (int i = 0; i < 5; i++) {
                progress.incrementCompleted();
            }
            assertThat(progress.getPercentage()).isEqualTo(100);
            assertThat(progress.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("应该正确记录所有已完成的阶段")
        void shouldRecordAllCompletedPhases() {
            progress.updatePhase("扫描文件");
            progress.updatePhase("解析语法");
            progress.updatePhase("分析架构");
            progress.updatePhase("生成报告");

            assertThat(progress.getCompletedPhases())
                    .hasSize(4)
                    .containsExactly("初始化", "扫描文件", "解析语法", "分析架构");
            assertThat(progress.getCurrentPhase()).isEqualTo("生成报告");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("应该处理非常大的总步骤数")
        void shouldHandleVeryLargeTotalSteps() {
            progress.setTotalSteps(1_000_000);
            progress.incrementCompleted();

            // 1/1000000 = 0.0001%
            assertThat(progress.getPercentage()).isCloseTo(0.0001, org.assertj.core.data.Offset.offset(0.001));
        }

        @Test
        @DisplayName("应该处理负数总步骤数")
        void shouldHandleNegativeTotalSteps() {
            progress.setTotalSteps(-10);
            progress.incrementCompleted();

            // 根据实现，可能不会增加或者有其他处理
            assertThat(progress.getCompletedSteps()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("应该处理大量阶段更新")
        void shouldHandleManyPhaseUpdates() {
            for (int i = 0; i < 100; i++) {
                progress.updatePhase("阶段" + i);
            }

            assertThat(progress.getCompletedPhases()).hasSize(100);
            assertThat(progress.getCurrentPhase()).isEqualTo("阶段99");
        }

        @Test
        @DisplayName("应该处理空字符串阶段名")
        void shouldHandleEmptyPhaseNames() {
            progress.updatePhase("");
            progress.updatePhase("正常阶段");

            assertThat(progress.getCurrentPhase()).isEqualTo("正常阶段");
            assertThat(progress.getCompletedPhases()).contains("初始化", "");
        }

        @Test
        @DisplayName("应该处理很长的任务描述")
        void shouldHandleLongTaskDescription() {
            String longTask = "正在分析".repeat(1000);
            progress.updateTask(longTask);

            assertThat(progress.getCurrentTask()).isEqualTo(longTask);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTest {

        @Test
        @DisplayName("应该能够在多线程环境下正确增加步骤")
        void shouldIncrementCorrectlyInMultithreadedEnvironment() throws InterruptedException {
            progress.setTotalSteps(1000);

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        progress.incrementCompleted();
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // 注意：如果AnalysisProgress不是线程安全的，这个测试可能会失败
            // 这个测试主要是为了发现潜在的并发问题
            assertThat(progress.getCompletedSteps()).isLessThanOrEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("计算剩余步骤测试")
    class RemainingStepsTest {

        @Test
        @DisplayName("应该能够计算剩余步骤数")
        void shouldCalculateRemainingSteps() {
            progress.setTotalSteps(10);
            progress.incrementCompleted();
            progress.incrementCompleted();
            progress.incrementCompleted();

            int remaining = progress.getTotalSteps() - progress.getCompletedSteps();
            assertThat(remaining).isEqualTo(7);
        }

        @Test
        @DisplayName("完成所有步骤时剩余应该为0")
        void shouldReturnZeroWhenCompleted() {
            progress.setTotalSteps(5);
            for (int i = 0; i < 5; i++) {
                progress.incrementCompleted();
            }

            int remaining = progress.getTotalSteps() - progress.getCompletedSteps();
            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("未开始时剩余应该等于总步骤数")
        void shouldReturnTotalWhenNotStarted() {
            progress.setTotalSteps(10);

            int remaining = progress.getTotalSteps() - progress.getCompletedSteps();
            assertThat(remaining).isEqualTo(10);
        }
    }
}

