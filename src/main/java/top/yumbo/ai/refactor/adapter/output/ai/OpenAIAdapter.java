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
 * OpenAI GPT 适配器
 * 支持GPT-4和GPT-3.5模型
 */
@Slf4j
public class OpenAIAdapter implements AIServicePort {

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 2000;
    private static final int TIMEOUT_SECONDS = 60;
    private static final int MAX_RETRIES = 3;

    // 成本估算（美元/1K tokens）
    private static final double GPT4_INPUT_COST = 0.03;
    private static final double GPT4_OUTPUT_COST = 0.06;
    private static final double GPT35_INPUT_COST = 0.0015;
    private static final double GPT35_OUTPUT_COST = 0.002;

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final HttpClient httpClient;

    /**
     * 构造函数 - 使用默认配置
     */
    public OpenAIAdapter(String apiKey) {
        this(apiKey, DEFAULT_API_URL, DEFAULT_MODEL);
    }

    /**
     * 构造函数 - 自定义配置
     */
    public OpenAIAdapter(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        log.info("OpenAI适配器初始化完成: model={}, url={}", model, apiUrl);
    }

    @Override
    public String analyze(String prompt) {
        log.info("OpenAI同步分析开始: prompt长度={}", prompt.length());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = callOpenAI(prompt);
                log.info("OpenAI分析完成: 响应长度={}", response.length());
                return response;
            } catch (Exception e) {
                log.warn("OpenAI调用失败 (尝试 {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    log.error("OpenAI调用最终失败", e);
                    throw new RuntimeException("OpenAI API调用失败: " + e.getMessage(), e);
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

        throw new RuntimeException("OpenAI API调用失败: 超过最大重试次数");
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
        return "OpenAI " + model;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单的健康检查
            String testPrompt = "Hello";
            callOpenAI(testPrompt);
            return true;
        } catch (Exception e) {
            log.warn("OpenAI服务不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getMaxConcurrency() {
        return 5; // OpenAI推荐的并发数
    }

    @Override
    public void shutdown() {
        // HttpClient会自动清理资源
        log.info("OpenAI适配器关闭");
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String prompt) throws IOException, InterruptedException {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("temperature", TEMPERATURE);
        requestBody.put("max_tokens", MAX_TOKENS);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Object[]{message});

        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // 检查响应状态
        if (response.statusCode() == 429) {
            throw new RuntimeException("OpenAI速率限制: 请求过于频繁");
        } else if (response.statusCode() == 401) {
            throw new RuntimeException("OpenAI认证失败: API Key无效");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI请求失败: " + response.statusCode() +
                    ", " + response.body());
        }

        // 解析响应
        JSONObject responseJson = JSON.parseObject(response.body());

        // 记录token使用情况
        if (responseJson.containsKey("usage")) {
            JSONObject usage = responseJson.getJSONObject("usage");
            int promptTokens = usage.getIntValue("prompt_tokens");
            int completionTokens = usage.getIntValue("completion_tokens");
            int totalTokens = usage.getIntValue("total_tokens");

            double cost = estimateCost(promptTokens, completionTokens);

            log.info("OpenAI token使用: prompt={}, completion={}, total={}, cost=${}",
                    promptTokens, completionTokens, totalTokens,
                    String.format("%.4f", cost));
        }

        // 提取响应内容
        return responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    /**
     * 估算token数量（简单估算）
     */
    public int estimateTokens(String text) {
        // 粗略估算: 1个token约等于4个字符（英文）或1.5个字符（中文）
        int chars = text.length();
        boolean hasChinese = text.matches(".*[\\u4e00-\\u9fa5].*");

        if (hasChinese) {
            return (int) (chars / 1.5);
        } else {
            return chars / 4;
        }
    }

    /**
     * 估算成本
     */
    public double estimateCost(int promptTokens, int completionTokens) {
        double inputCost = model.contains("gpt-4") ? GPT4_INPUT_COST : GPT35_INPUT_COST;
        double outputCost = model.contains("gpt-4") ? GPT4_OUTPUT_COST : GPT35_OUTPUT_COST;

        return (promptTokens / 1000.0) * inputCost +
               (completionTokens / 1000.0) * outputCost;
    }

    /**
     * 从文本估算成本
     */
    public double getCost(String text) {
        int tokens = estimateTokens(text);
        // 假设输出token约为输入的20%
        int outputTokens = (int) (tokens * 0.2);
        return estimateCost(tokens, outputTokens);
    }
}
