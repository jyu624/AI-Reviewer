package top.yumbo.ai.reviewer.adapter.ai.config;

/**
 * AI 服务配置
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-12
 */
public record AIServiceConfig(
        String apiKey,
        String baseUrl,
        String model,
        int maxTokens,
        double temperature,
        int maxConcurrency,
        int maxRetries,
        int retryDelayMillis,
        int connectTimeoutMillis,
        int readTimeoutMillis,
        String region  // AWS region (for Bedrock)
) {
    /**
     * 创建默认配置
     */
    public static AIServiceConfig createDefault(String apiKey, String model) {
        return new AIServiceConfig(
                apiKey,
                null,
                model,
                4000,
                0.3,
                3,
                3,
                1000,
                30000,
                60000,
                null
        );
    }

    /**
     * 创建 Bedrock 配置
     */
    public static AIServiceConfig createBedrock(String accessKey, String secretKey, String model, String region) {
        String apiKey = accessKey != null && secretKey != null ? accessKey + ":" + secretKey : null;
        return new AIServiceConfig(
                apiKey,
                null,
                model,
                4000,
                0.3,
                3,
                3,
                1000,
                30000,
                60000,
                region
        );
    }
}
