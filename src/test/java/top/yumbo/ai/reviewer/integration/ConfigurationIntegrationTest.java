package top.yumbo.ai.reviewer.integration;

import org.junit.jupiter.api.*;
import top.yumbo.ai.reviewer.infrastructure.config.Configuration;
import top.yumbo.ai.reviewer.infrastructure.config.ConfigurationLoader;
import top.yumbo.ai.reviewer.infrastructure.factory.AIServiceFactory;
import top.yumbo.ai.reviewer.application.port.output.AIServicePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 配置系统集成测试
 *
 * 测试配置加载和 AI 服务创建的完整流程
 *
 * @author AI-Reviewer Team
 * @version 2.0
 * @since 2025-11-13
 */
@DisplayName("配置系统集成测试")
class ConfigurationIntegrationTest {

    @BeforeEach
    void setUp() {
        // 清理所有系统属性
        System.clearProperty("ai.provider");
        System.clearProperty("ai.apiKey");
        System.clearProperty("ai.model");
        System.clearProperty("ai.baseUrl");
        System.clearProperty("ai.maxTokens");
        System.clearProperty("ai.temperature");
    }

    @AfterEach
    void tearDown() {
        // 清理系统属性
        System.clearProperty("ai.provider");
        System.clearProperty("ai.apiKey");
        System.clearProperty("ai.model");
    }

    @Test
    @DisplayName("应该从配置到 AI 服务的完整流程")
    void shouldCompleteFlowFromConfigToAIService() {
        // 1. 设置配置
        System.setProperty("ai.provider", "deepseek");
        System.setProperty("ai.apiKey", "test-integration-key");
        System.setProperty("ai.model", "deepseek-chat");

        // 2. 加载配置
        Configuration config = ConfigurationLoader.load();

        // 3. 验证配置
        assertThat(config.getAiProvider()).isEqualTo("deepseek");
        assertThat(config.getAiApiKey()).isEqualTo("test-integration-key");
        assertThat(config.getAiModel()).isEqualTo("deepseek-chat");

        // 4. 创建 AI 服务配置
        Configuration.AIServiceConfig aiConfig = config.getAIServiceConfig();

        // 5. 创建 AI 服务
        AIServicePort aiService = AIServiceFactory.create(aiConfig);

        // 6. 验证 AI 服务
        assertThat(aiService).isNotNull();
        assertThat(aiService.getProviderName()).isEqualTo("DeepSeek");

        // 清理
        aiService.shutdown();
    }

    @Test
    @DisplayName("应该支持切换不同的 AI 服务提供商")
    void shouldSupportSwitchingProviders() {
        String[] providers = {"deepseek", "openai", "claude", "gemini"};
        String[] expectedNames = {"DeepSeek", "OpenAI", "Claude", "Gemini"};

        for (int i = 0; i < providers.length; i++) {
            System.setProperty("ai.provider", providers[i]);
            System.setProperty("ai.apiKey", "test-key");

            Configuration config = ConfigurationLoader.load();
            AIServicePort aiService = AIServiceFactory.create(config.getAIServiceConfig());

            assertThat(aiService.getProviderName()).isEqualTo(expectedNames[i]);

            aiService.shutdown();
        }
    }

    @Test
    @DisplayName("应该验证配置完整性")
    void shouldValidateConfigurationCompleteness() {
        // 缺少 API Key 应该抛出异常
        System.clearProperty("ai.apiKey");

        assertThatThrownBy(() -> ConfigurationLoader.load())
                .isInstanceOf(Configuration.ConfigurationException.class)
                .hasMessageContaining("API Key");
    }

    @Test
    @DisplayName("应该处理配置优先级")
    void shouldHandleConfigurationPriority() {
        // 系统属性应该优先
        System.setProperty("ai.provider", "openai");
        System.setProperty("ai.apiKey", "system-key");
        System.setProperty("ai.model", "gpt-4");

        Configuration config = ConfigurationLoader.load();

        assertThat(config.getAiProvider()).isEqualTo("openai");
        assertThat(config.getAiApiKey()).isEqualTo("system-key");
        assertThat(config.getAiModel()).isEqualTo("gpt-4");
    }

    @Test
    @DisplayName("应该使用默认值填充缺失配置")
    void shouldUseDefaultValuesForMissingConfig() {
        System.setProperty("ai.apiKey", "test-key");
        System.setProperty("ai.provider", "deepseek");
        // 不设置其他参数

        Configuration config = ConfigurationLoader.load();

        assertThat(config.getAiMaxTokens()).isNotNull();
        assertThat(config.getAiTemperature()).isNotNull();
        assertThat(config.getAiMaxRetries()).isNotNull();
    }

    @Test
    @DisplayName("应该支持 AWS Bedrock 配置")
    void shouldSupportBedrockConfiguration() {
        System.setProperty("ai.provider", "bedrock");
        System.setProperty("ai.apiKey", "test-key");

        Configuration config = ConfigurationLoader.load();
        Configuration.AIServiceConfig aiConfig = config.getAIServiceConfig();

        assertThat(aiConfig.provider()).isEqualTo("bedrock");
        assertThat(aiConfig.awsRegion()).isNotNull();

        AIServicePort aiService = AIServiceFactory.create(aiConfig);
        assertThat(aiService.getProviderName()).contains("Bedrock");

        aiService.shutdown();
    }

    @Test
    @DisplayName("应该传递所有配置参数到 AI 服务")
    void shouldPassAllConfigParametersToAIService() {
        System.setProperty("ai.provider", "openai");
        System.setProperty("ai.apiKey", "test-key");
        System.setProperty("ai.model", "gpt-4-turbo");

        Configuration config = ConfigurationLoader.load();
        Configuration.AIServiceConfig aiConfig = config.getAIServiceConfig();

        assertThat(aiConfig.provider()).isEqualTo("openai");
        assertThat(aiConfig.apiKey()).isEqualTo("test-key");
        assertThat(aiConfig.model()).isEqualTo("gpt-4-turbo");
        assertThat(aiConfig.maxTokens()).isNotNull();
        assertThat(aiConfig.temperature()).isNotNull();
        assertThat(aiConfig.maxRetries()).isNotNull();
        assertThat(aiConfig.connectTimeoutMillis()).isNotNull();
        assertThat(aiConfig.readTimeoutMillis()).isNotNull();
    }

    @Test
    @DisplayName("应该处理自定义配置值")
    void shouldHandleCustomConfigValues() {
        System.setProperty("ai.provider", "deepseek");
        System.setProperty("ai.apiKey", "custom-key");
        System.setProperty("ai.model", "custom-model");

        Configuration config = ConfigurationLoader.load();

        assertThat(config.getAiModel()).isEqualTo("custom-model");

        AIServicePort aiService = AIServiceFactory.create(config.getAIServiceConfig());
        assertThat(aiService).isNotNull();

        aiService.shutdown();
    }

    @Test
    @DisplayName("应该支持配置别名")
    void shouldSupportConfigurationAliases() {
        // 测试 provider 别名
        String[][] aliasTests = {
                {"anthropic", "Claude"},
                {"google", "Gemini"},
                {"aws", "Bedrock"}
        };

        for (String[] test : aliasTests) {
            System.setProperty("ai.provider", test[0]);
            System.setProperty("ai.apiKey", "test-key");

            Configuration config = ConfigurationLoader.load();
            AIServicePort aiService = AIServiceFactory.create(config.getAIServiceConfig());

            assertThat(aiService.getProviderName()).contains(test[1]);

            aiService.shutdown();
        }
    }
}

