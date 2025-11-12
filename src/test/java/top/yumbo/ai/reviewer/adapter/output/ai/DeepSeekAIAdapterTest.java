package top.yumbo.ai.reviewer.adapter.output.ai;

import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DeepSeekAIAdapter 测试类
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
@DisplayName("DeepSeekAIAdapter 测试")
class DeepSeekAIAdapterTest {

    private DeepSeekAIAdapter adapter;
    private AIServiceConfig testConfig;

    @BeforeEach
    void setUp() {
        // 使用测试配置
        testConfig = new AIServiceConfig(
                "test-api-key",
                "https://test.api.deepseek.com/v1",
                "deepseek-chat",
                2000,
                0.3,
                2,
                3,
                500,
                5000,
                10000,
                null
        );

        adapter = new DeepSeekAIAdapter(testConfig);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.shutdown();
        }
    }

    @Test
    @DisplayName("应该成功创建适配器实例")
    void shouldCreateAdapter() {
        assertThat(adapter).isNotNull();
        assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
    }

    @Test
    @DisplayName("应该验证适配器已初始化")
    void shouldCheckAvailability() {
        // 测试适配器已正确初始化（不依赖网络）
        assertThat(adapter).isNotNull();
        assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
        // 注意：实际的可用性检查需要真实的 API 连接，在单元测试中跳过
    }

    @Test
    @DisplayName("应该返回最大并发数")
    void shouldReturnMaxConcurrency() {
        int maxConcurrency = adapter.getMaxConcurrency();
        assertThat(maxConcurrency).isGreaterThan(0);
        assertThat(maxConcurrency).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("应该拒绝null提示词")
    void shouldRejectNullPrompt() {
        assertThatThrownBy(() -> adapter.analyze(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("应该拒绝空提示词")
    void shouldRejectEmptyPrompt() {
        assertThatThrownBy(() -> adapter.analyze(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("应该正确关闭适配器")
    void shouldShutdownProperly() {
        DeepSeekAIAdapter tempAdapter = new DeepSeekAIAdapter(testConfig);
        tempAdapter.shutdown();

        // 验证关闭后不能使用
        assertThatThrownBy(() -> tempAdapter.analyze("test"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("应该使用默认值填充未提供的配置")
    void shouldUseDefaultValuesForMissingConfig() {
        AIServiceConfig minimalConfig = new AIServiceConfig(
                "api-key",
                null,  // 使用默认baseUrl
                null,  // 使用默认model
                0,     // 使用默认maxTokens
                -1,    // 使用默认temperature
                0,     // 使用默认maxConcurrency
                -1,    // 使用默认maxRetries
                0,     // 使用默认retryDelayMillis
                0,     // 使用默认connectTimeoutMillis
                0,     // 使用默认readTimeoutMillis
                null   // region
        );

        DeepSeekAIAdapter adapterWithDefaults = new DeepSeekAIAdapter(minimalConfig);

        assertThat(adapterWithDefaults).isNotNull();
        assertThat(adapterWithDefaults.getProviderName()).isEqualTo("DeepSeek");

        adapterWithDefaults.shutdown();
    }

    @Test
    @DisplayName("应该接受有效的API密钥")
    void shouldAcceptValidApiKey() {
        AIServiceConfig validConfig = new AIServiceConfig(
                "sk-test1234567890",
                "https://api.deepseek.com/v1",
                "deepseek-chat",
                4000,
                0.3,
                3,
                3,
                1000,
                30000,
                60000,
                null
        );

        DeepSeekAIAdapter validAdapter = new DeepSeekAIAdapter(validConfig);
        assertThat(validAdapter).isNotNull();
        assertThat(validAdapter.getProviderName()).isEqualTo("DeepSeek");
        validAdapter.shutdown();
    }

    @Test
    @DisplayName("应该处理不同的模型名称")
    void shouldHandleDifferentModelNames() {
        AIServiceConfig config = new AIServiceConfig(
                "test-key",
                "https://test.api.com",
                "custom-model",
                2000,
                0.5,
                2,
                3,
                500,
                5000,
                10000,
                null
        );

        DeepSeekAIAdapter customAdapter = new DeepSeekAIAdapter(config);
        assertThat(customAdapter).isNotNull();
        customAdapter.shutdown();
    }

    @Test
    @DisplayName("应该处理不同的温度参数")
    void shouldHandleDifferentTemperatures() {
        AIServiceConfig lowTemp = new AIServiceConfig(
                "test-key",
                "https://test.api.com",
                "model",
                2000,
                0.0,  // 低温度
                2,
                3,
                500,
                5000,
                10000,
                null
        );

        AIServiceConfig highTemp = new AIServiceConfig(
                "test-key",
                "https://test.api.com",
                "model",
                2000,
                1.0,  // 高温度
                2,
                3,
                500,
                5000,
                10000,
                null
        );

        DeepSeekAIAdapter lowTempAdapter = new DeepSeekAIAdapter(lowTemp);
        DeepSeekAIAdapter highTempAdapter = new DeepSeekAIAdapter(highTemp);

        assertThat(lowTempAdapter).isNotNull();
        assertThat(highTempAdapter).isNotNull();

        lowTempAdapter.shutdown();
        highTempAdapter.shutdown();
    }
}

