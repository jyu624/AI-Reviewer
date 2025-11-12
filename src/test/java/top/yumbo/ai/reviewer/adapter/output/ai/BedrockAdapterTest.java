package top.yumbo.ai.reviewer.adapter.output.ai;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * AWS Bedrock 适配器测试
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
class BedrockAdapterTest {

    private BedrockAdapter bedrockAdapter;

    @BeforeEach
    void setUp() {
        // 从环境变量读取配置
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv("AWS_REGION");
        String modelId = System.getenv("AWS_BEDROCK_MODEL_ID");

        // 构建 API Key（格式：accessKey:secretKey）
        String apiKey = null;
        if (accessKey != null && secretKey != null) {
            apiKey = accessKey + ":" + secretKey;
        }

        // 使用默认配置
        AIServiceConfig config = new AIServiceConfig(
                apiKey,
                null, // baseUrl (Bedrock 不需要)
                modelId != null ? modelId : "anthropic.claude-v2",
                1000, // maxTokens
                0.3,  // temperature
                3,    // maxConcurrency
                3,    // maxRetries
                1000, // retryDelayMillis
                30000, // connectTimeoutMillis
                60000, // readTimeoutMillis
                region != null ? region : "us-east-1"
        );

        bedrockAdapter = new BedrockAdapter(config);
    }

    @AfterEach
    void tearDown() {
        if (bedrockAdapter != null) {
            bedrockAdapter.shutdown();
        }
    }

    @Test
    void testGetProviderName() {
        // Given & When
        String providerName = bedrockAdapter.getProviderName();

        // Then
        assertThat(providerName).isNotNull();
        assertThat(providerName).contains("AWS Bedrock");
        System.out.println("Provider: " + providerName);
    }

    @Test
    void testIsAvailable() {
        // Given & When
        boolean available = bedrockAdapter.isAvailable();

        // Then
        assertThat(available).isTrue();
        System.out.println("Bedrock 服务可用: " + available);
    }

    @Test
    void testGetMaxConcurrency() {
        // Given & When
        int maxConcurrency = bedrockAdapter.getMaxConcurrency();

        // Then
        assertThat(maxConcurrency).isGreaterThan(0);
        assertThat(maxConcurrency).isLessThanOrEqualTo(3);
        System.out.println("最大并发数: " + maxConcurrency);
    }

    @Test
    void testGetModelId() {
        // Given & When
        String modelId = bedrockAdapter.getModelId();

        // Then
        assertThat(modelId).isNotNull();
        assertThat(modelId).isNotEmpty();
        System.out.println("Model ID: " + modelId);
    }

    @Test
    void testGetRegion() {
        // Given & When
        String region = bedrockAdapter.getRegion();

        // Then
        assertThat(region).isNotNull();
        assertThat(region).isNotEmpty();
        System.out.println("AWS Region: " + region);
    }

    @Test
    @Disabled("需要有效的 AWS 凭证和网络连接")
    void testAnalyzeSync() {
        // Given
        String prompt = "请用一句话介绍 AWS Bedrock。";

        // When
        String result = bedrockAdapter.analyze(prompt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.length()).isGreaterThan(10);
        System.out.println("同步分析结果: " + result);
    }

    @Test
    @Disabled("需要有效的 AWS 凭证和网络连接")
    void testAnalyzeAsync() throws Exception {
        // Given
        String prompt = "请用一句话介绍人工智能。";

        // When
        CompletableFuture<String> future = bedrockAdapter.analyzeAsync(prompt);
        String result = future.get(30, TimeUnit.SECONDS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.length()).isGreaterThan(10);
        System.out.println("异步分析结果: " + result);
    }

    @Test
    @Disabled("需要有效的 AWS 凭证和网络连接")
    void testAnalyzeBatchAsync() throws Exception {
        // Given
        String[] prompts = {
                "什么是云计算？",
                "什么是机器学习？",
                "什么是深度学习？"
        };

        // When
        CompletableFuture<String[]> future = bedrockAdapter.analyzeBatchAsync(prompts);
        String[] results = future.get(60, TimeUnit.SECONDS);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        for (int i = 0; i < results.length; i++) {
            assertThat(results[i]).isNotEmpty();
            System.out.println("批量分析结果 " + (i + 1) + ": " + results[i]);
        }
    }

    @Test
    void testAnalyzeWithNullPrompt() {
        // Given
        String prompt = null;

        // When & Then
        assertThatThrownBy(() -> bedrockAdapter.analyze(prompt))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testAnalyzeWithEmptyPrompt() {
        // Given
        String prompt = "";

        // When & Then
        assertThatThrownBy(() -> bedrockAdapter.analyze(prompt))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testShutdown() {
        // Given
        BedrockAdapter adapter = bedrockAdapter;

        // When
        adapter.shutdown();

        // Then
        // 验证关闭后不能继续使用
        assertThatThrownBy(() -> adapter.analyze("测试"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testGetActiveRequests() {
        // Given & When
        int activeRequests = bedrockAdapter.getActiveRequests();

        // Then
        assertThat(activeRequests).isGreaterThanOrEqualTo(0);
        System.out.println("活跃请求数: " + activeRequests);
    }
}

