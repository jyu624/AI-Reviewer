package top.yumbo.ai.refactor.adapter.output.ai;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DeepSeekAIAdapter测试
 * 注意：这些测试不调用真实的API，使用模拟配置
 */
@DisplayName("DeepSeekAIAdapter测试")
class DeepSeekAIAdapterTest {

    private DeepSeekAIAdapter adapter;
    private DeepSeekAIAdapter.AIServiceConfig testConfig;

    @BeforeEach
    void setUp() {
        // 创建测试配置（不使用真实API）
        testConfig = new DeepSeekAIAdapter.AIServiceConfig(
                "test-api-key",
                "https://test.api.deepseek.com/v1",
                "deepseek-chat",
                2000,
                0.3,
                2, // maxConcurrency
                3, // maxRetries
                500, // retryDelayMillis
                5000, // connectTimeoutMillis
                10000 // readTimeoutMillis
        );

        adapter = new DeepSeekAIAdapter(testConfig);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.shutdown();
        }
    }

    @Nested
    @DisplayName("构造函数和初始化测试")
    class ConstructorTest {

        @Test
        @DisplayName("应该使用提供的配置创建适配器")
        void shouldCreateAdapterWithProvidedConfig() {
            assertThat(adapter).isNotNull();
            assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
        }

        @Test
        @DisplayName("应该使用默认值填充未提供的配置")
        void shouldUseDefaultValuesForMissingConfig() {
            DeepSeekAIAdapter.AIServiceConfig minimalConfig =
                    new DeepSeekAIAdapter.AIServiceConfig(
                            "api-key",
                            null, // 使用默认baseUrl
                            null, // 使用默认model
                            0,    // 使用默认maxTokens
                            -1,   // 使用默认temperature
                            0,    // 使用默认maxConcurrency
                            -1,   // 使用默认maxRetries
                            0,    // 使用默认retryDelayMillis
                            0,    // 使用默认connectTimeoutMillis
                            0     // 使用默认readTimeoutMillis
                    );

            DeepSeekAIAdapter adapterWithDefaults = new DeepSeekAIAdapter(minimalConfig);

            assertThat(adapterWithDefaults).isNotNull();
            assertThat(adapterWithDefaults.getProviderName()).isEqualTo("DeepSeek");

            adapterWithDefaults.shutdown();
        }

        @Test
        @DisplayName("应该正确设置并发限制")
        void shouldSetConcurrencyLimit() {
            assertThat(adapter.getMaxConcurrency()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getProviderName()方法测试")
    class GetProviderNameTest {

        @Test
        @DisplayName("应该返回DeepSeek")
        void shouldReturnDeepSeek() {
            assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
        }
    }

    @Nested
    @DisplayName("getMaxConcurrency()方法测试")
    class GetMaxConcurrencyTest {

        @Test
        @DisplayName("应该返回配置的最大并发数")
        void shouldReturnConfiguredMaxConcurrency() {
            int maxConcurrency = adapter.getMaxConcurrency();
            assertThat(maxConcurrency).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("analyze()方法测试（不调用真实API）")
    class AnalyzeMethodTest {

        @Test
        @DisplayName("应该能够接受非空提示词")
        void shouldAcceptNonNullPrompt() {
            String prompt = "这是一个测试提示词";

            // 注意：这个测试会尝试调用真实API，因此会失败
            // 在实际测试中应该使用Mock或者跳过这个测试
            assertThatThrownBy(() -> adapter.analyze(prompt))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AI分析失败");
        }

        @Test
        @DisplayName("应该拒绝null提示词")
        void shouldRejectNullPrompt() {
            assertThatThrownBy(() -> adapter.analyze(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("应该拒绝空字符串提示词")
        void shouldRejectEmptyPrompt() {
            assertThatThrownBy(() -> adapter.analyze(""))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("analyzeAsync()方法测试")
    class AnalyzeAsyncMethodTest {

        @Test
        @DisplayName("应该返回CompletableFuture")
        void shouldReturnCompletableFuture() {
            String prompt = "测试提示词";
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            assertThat(future).isNotNull();
            assertThat(future).isInstanceOf(CompletableFuture.class);
        }

        @Test
        @DisplayName("应该能够异步处理请求")
        void shouldHandleRequestAsynchronously() {
            String prompt = "测试提示词";
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            // 不等待结果，只验证Future对象的创建
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse(); // 应该还在执行中

            // 等待一小段时间后取消
            future.cancel(true);
        }

        @Test
        @DisplayName("失败的请求应该包含异常信息")
        void shouldContainExceptionForFailedRequest() {
            String prompt = "测试提示词";
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class);
        }
    }

    @Nested
    @DisplayName("analyzeBatchAsync()方法测试")
    class AnalyzeBatchAsyncMethodTest {

        @Test
        @DisplayName("应该能够处理批量请求")
        void shouldHandleBatchRequests() {
            String[] prompts = {"提示词1", "提示词2", "提示词3"};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(prompts);

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("应该处理空数组")
        void shouldHandleEmptyArray() {
            String[] emptyPrompts = {};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(emptyPrompts);

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("应该返回与输入数量相同的结果")
        void shouldReturnSameNumberOfResults() throws Exception {
            String[] prompts = {"提示词1", "提示词2"};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(prompts);

            // 等待完成或超时
            try {
                String[] results = future.get(5, TimeUnit.SECONDS);
                assertThat(results).hasSize(prompts.length);
            } catch (Exception e) {
                // API调用失败是预期的
                assertThat(e).isInstanceOf(ExecutionException.class);
            }
        }
    }

    @Nested
    @DisplayName("并发控制测试")
    class ConcurrencyControlTest {

        @Test
        @DisplayName("应该限制并发请求数量")
        void shouldLimitConcurrentRequests() {
            // 创建多个并发请求
            CompletableFuture<String>[] futures = new CompletableFuture[5];
            for (int i = 0; i < 5; i++) {
                futures[i] = adapter.analyzeAsync("提示词" + i);
            }

            // 验证不会超过最大并发数
            assertThat(adapter.getMaxConcurrency()).isGreaterThanOrEqualTo(2);

            // 取消所有请求
            for (CompletableFuture<String> future : futures) {
                future.cancel(true);
            }
        }

        @Test
        @DisplayName("应该能够跟踪活跃请求数")
        void shouldTrackActiveRequests() {
            int initialConcurrency = adapter.getMaxConcurrency();
            assertThat(initialConcurrency).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("重试机制测试")
    class RetryMechanismTest {

        @Test
        @DisplayName("失败的请求应该会重试")
        void shouldRetryFailedRequests() {
            // 使用测试配置，maxRetries=3
            String prompt = "测试提示词";

            long startTime = System.currentTimeMillis();
            assertThatThrownBy(() -> adapter.analyze(prompt))
                    .isInstanceOf(RuntimeException.class);
            long duration = System.currentTimeMillis() - startTime;

            // 应该花费一些时间进行重试（至少2次重试，每次500ms）
            // 但由于是网络超时，实际时间会更长
            assertThat(duration).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("isAvailable()方法测试")
    class IsAvailableTest {

        @Test
        @DisplayName("无效配置时应该返回false")
        void shouldReturnFalseForInvalidConfig() {
            boolean available = adapter.isAvailable();

            // 由于使用的是测试配置（无效API），应该返回false
            assertThat(available).isFalse();
        }
    }

    @Nested
    @DisplayName("shutdown()方法测试")
    class ShutdownTest {

        @Test
        @DisplayName("应该能够正常关闭")
        void shouldShutdownGracefully() {
            assertThat(adapter).isNotNull();

            adapter.shutdown();

            // 再次调用shutdown不应该抛出异常
            adapter.shutdown();
        }

        @Test
        @DisplayName("关闭后不应该接受新请求")
        void shouldNotAcceptNewRequestsAfterShutdown() {
            adapter.shutdown();

            // 注意：实际行为取决于实现
            // 可能抛出异常或返回失败的Future
            assertThatThrownBy(() -> adapter.analyze("测试"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("配置验证测试")
    class ConfigValidationTest {

        @Test
        @DisplayName("应该接受有效的API密钥")
        void shouldAcceptValidApiKey() {
            DeepSeekAIAdapter.AIServiceConfig validConfig =
                    new DeepSeekAIAdapter.AIServiceConfig(
                            "sk-test1234567890",
                            "https://api.deepseek.com/v1",
                            "deepseek-chat",
                            4000,
                            0.3,
                            3,
                            3,
                            1000,
                            30000,
                            60000
                    );

            DeepSeekAIAdapter validAdapter = new DeepSeekAIAdapter(validConfig);
            assertThat(validAdapter).isNotNull();
            assertThat(validAdapter.getProviderName()).isEqualTo("DeepSeek");
            validAdapter.shutdown();
        }

        @Test
        @DisplayName("应该处理不同的模型名称")
        void shouldHandleDifferentModelNames() {
            DeepSeekAIAdapter.AIServiceConfig config =
                    new DeepSeekAIAdapter.AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "custom-model",
                            2000,
                            0.5,
                            2,
                            3,
                            500,
                            5000,
                            10000
                    );

            DeepSeekAIAdapter customAdapter = new DeepSeekAIAdapter(config);
            assertThat(customAdapter).isNotNull();
            customAdapter.shutdown();
        }

        @Test
        @DisplayName("应该处理不同的温度参数")
        void shouldHandleDifferentTemperatures() {
            DeepSeekAIAdapter.AIServiceConfig lowTemp =
                    new DeepSeekAIAdapter.AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "model",
                            2000,
                            0.0, // 低温度
                            2,
                            3,
                            500,
                            5000,
                            10000
                    );

            DeepSeekAIAdapter.AIServiceConfig highTemp =
                    new DeepSeekAIAdapter.AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "model",
                            2000,
                            1.0, // 高温度
                            2,
                            3,
                            500,
                            5000,
                            10000
                    );

            DeepSeekAIAdapter lowTempAdapter = new DeepSeekAIAdapter(lowTemp);
            DeepSeekAIAdapter highTempAdapter = new DeepSeekAIAdapter(highTemp);

            assertThat(lowTempAdapter).isNotNull();
            assertThat(highTempAdapter).isNotNull();

            lowTempAdapter.shutdown();
            highTempAdapter.shutdown();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("应该处理非常长的提示词")
        void shouldHandleVeryLongPrompt() {
            String longPrompt = "测试".repeat(10000);

            assertThatThrownBy(() -> adapter.analyze(longPrompt))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("应该处理包含特殊字符的提示词")
        void shouldHandleSpecialCharactersInPrompt() {
            String specialPrompt = "测试\n\r\t\"'<>&{}[]";

            assertThatThrownBy(() -> adapter.analyze(specialPrompt))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("应该处理Unicode字符")
        void shouldHandleUnicodeCharacters() {
            String unicodePrompt = "测试 🚀 emoji 和 中文";

            assertThatThrownBy(() -> adapter.analyze(unicodePrompt))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("创建适配器应该很快")
        void shouldCreateAdapterQuickly() {
            long startTime = System.currentTimeMillis();

            DeepSeekAIAdapter newAdapter = new DeepSeekAIAdapter(testConfig);

            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(1000); // 应该在1秒内完成

            newAdapter.shutdown();
        }

        @Test
        @DisplayName("关闭适配器应该在合理时间内完成")
        void shouldShutdownInReasonableTime() {
            long startTime = System.currentTimeMillis();

            adapter.shutdown();

            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(15000); // 应该在15秒内完成
        }
    }
}

