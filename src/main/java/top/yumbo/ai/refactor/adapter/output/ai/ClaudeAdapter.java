package top.yumbo.ai.refactor.adapter.output.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.refactor.application.port.output.AIServicePort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Anthropic Claude 适配器
 * 支持Claude 3 Opus/Sonnet/Haiku
 */
@Slf4j
public class ClaudeAdapter implements AIServicePort {

    private static final String DEFAULT_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String DEFAULT_MODEL = "claude-3-opus-20240229";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int MAX_TOKENS = 4000;
    private static final int TIMEOUT_SECONDS = 90;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_CONTEXT_LENGTH = 200000; // Claude 3的优势

    // 成本估算（美元/1M tokens）
    private static final double OPUS_INPUT_COST = 15.0;
    private static final double OPUS_OUTPUT_COST = 75.0;
    private static final double SONNET_INPUT_COST = 3.0;
    private static final double SONNET_OUTPUT_COST = 15.0;

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final HttpClient httpClient;

    /**
     * 构造函数 - 使用默认配置
     */
    public ClaudeAdapter(String apiKey) {
        this(apiKey, DEFAULT_API_URL, DEFAULT_MODEL);
    }

    /**
     * 构造函数 - 自定义配置
     */
    public ClaudeAdapter(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        log.info("Claude适配器初始化完成: model={}, url={}", model, apiUrl);
    }

    @Override
    public String analyze(String prompt) {
        log.info("Claude同步分析开始: prompt长度={}", prompt.length());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = callClaude(prompt);
                log.info("Claude分析完成: 响应长度={}", response.length());
                return response;
            } catch (Exception e) {
                log.warn("Claude调用失败 (尝试 {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    log.error("Claude调用最终失败", e);
                    throw new RuntimeException("Claude API调用失败: " + e.getMessage(), e);
                }

                // 指数退避
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        throw new RuntimeException("Claude API调用失败: 超过最大重试次数");
    }

    @Override
    public CompletableFuture<String> analyzeAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> analyze(prompt));
    }

    @Override
    public CompletableFuture<String[]> analyzeBatchAsync(String[] prompts) {
        CompletableFuture<String>[] futures = new CompletableFuture[prompts.length];

        for (int i = 0; i < prompts.length; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                log.info("批量分析任务 {}/{} 开始", index + 1, prompts.length);
                return analyze(prompts[index]);
            });
        }

        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    String[] results = new String[prompts.length];
                    for (int i = 0; i < prompts.length; i++) {
                        results[i] = futures[i].join();
                    }
                    return results;
                });
    }

    @Override
    public String getProviderName() {
        return "Anthropic Claude 3";
    }

    @Override
    public boolean isAvailable() {
        try {
            String testPrompt = "Hello";
            callClaude(testPrompt);
            return true;
        } catch (Exception e) {
            log.warn("Claude服务不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getMaxConcurrency() {
        return 5; // Claude推荐的并发数
    }

    @Override
    public void shutdown() {
        log.info("Claude适配器关闭");
    }

    /**
     * 获取最大上下文长度
     */
    public int getMaxContextLength() {
        return MAX_CONTEXT_LENGTH;
    }

    /**
     * 调用Claude API
     */
    private String callClaude(String prompt) throws IOException, InterruptedException {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("max_tokens", MAX_TOKENS);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Object[]{message});

        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // 检查响应状态
        if (response.statusCode() == 429) {
            throw new RuntimeException("Claude速率限制: 请求过于频繁");
        } else if (response.statusCode() == 401) {
            throw new RuntimeException("Claude认证失败: API Key无效");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Claude请求失败: " + response.statusCode() +
                    ", " + response.body());
        }

        // 解析响应
        JSONObject responseJson = JSON.parseObject(response.body());

        // 记录token使用情况
        if (responseJson.containsKey("usage")) {
            JSONObject usage = responseJson.getJSONObject("usage");
            int inputTokens = usage.getIntValue("input_tokens");
            int outputTokens = usage.getIntValue("output_tokens");

            double cost = estimateCost(inputTokens, outputTokens);

            log.info("Claude token使用: input={}, output={}, cost=${}",
                    inputTokens, outputTokens, String.format("%.4f", cost));
        }

        // 提取响应内容
        return responseJson.getJSONArray("content")
                .getJSONObject(0)
                .getString("text");
    }

    /**
     * 估算成本
     */
    public double estimateCost(int inputTokens, int outputTokens) {
        double inputCost;
        double outputCost;

        if (model.contains("opus")) {
            inputCost = OPUS_INPUT_COST;
            outputCost = OPUS_OUTPUT_COST;
        } else {
            inputCost = SONNET_INPUT_COST;
            outputCost = SONNET_OUTPUT_COST;
        }

        return (inputTokens / 1000000.0) * inputCost +
               (outputTokens / 1000000.0) * outputCost;
    }

    /**
     * 估算token数量
     */
    public int estimateTokens(String text) {
        // Claude的token估算类似OpenAI
        int chars = text.length();
        boolean hasChinese = text.matches(".*[\\u4e00-\\u9fa5].*");

        if (hasChinese) {
            return (int) (chars / 1.5);
        } else {
            return chars / 4;
        }
    }
}
