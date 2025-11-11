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
 * Google Gemini 适配器
 * 支持Gemini Pro和Gemini Pro Vision
 */
@Slf4j
public class GeminiAdapter implements AIServicePort {

    private static final String DEFAULT_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String DEFAULT_MODEL = "gemini-pro";
    private static final double TEMPERATURE = 0.5;
    private static final int TIMEOUT_SECONDS = 60;
    private static final int MAX_RETRIES = 3;

    // Gemini的免费额度很友好
    private static final double FREE_TIER_COST = 0.0;

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    /**
     * 构造函数 - 使用默认配置
     */
    public GeminiAdapter(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    /**
     * 构造函数 - 自定义配置
     */
    public GeminiAdapter(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        log.info("Gemini适配器初始化完成: model={}", model);
    }

    @Override
    public String analyze(String prompt) {
        log.info("Gemini同步分析开始: prompt长度={}", prompt.length());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = callGemini(prompt);
                log.info("Gemini分析完成: 响应长度={}", response.length());
                return response;
            } catch (Exception e) {
                log.warn("Gemini调用失败 (尝试 {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    log.error("Gemini调用最终失败", e);
                    throw new RuntimeException("Gemini API调用失败: " + e.getMessage(), e);
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

        throw new RuntimeException("Gemini API调用失败: 超过最大重试次数");
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
        return "Google Gemini";
    }

    @Override
    public boolean isAvailable() {
        try {
            String testPrompt = "Hello";
            callGemini(testPrompt);
            return true;
        } catch (Exception e) {
            log.warn("Gemini服务不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getMaxConcurrency() {
        return 10; // Gemini支持较高并发
    }

    @Override
    public void shutdown() {
        log.info("Gemini适配器关闭");
    }

    /**
     * 调用Gemini API
     */
    private String callGemini(String prompt) throws IOException, InterruptedException {
        // 构建请求URL
        String url = String.format(DEFAULT_API_URL, model) + "?key=" + apiKey;

        // 构建请求体
        JSONObject requestBody = new JSONObject();

        JSONObject content = new JSONObject();
        JSONObject part = new JSONObject();
        part.put("text", prompt);
        content.put("parts", new Object[]{part});
        requestBody.put("contents", new Object[]{content});

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", TEMPERATURE);
        requestBody.put("generationConfig", generationConfig);

        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // 检查响应状态
        if (response.statusCode() == 429) {
            throw new RuntimeException("Gemini速率限制: 请求过于频繁");
        } else if (response.statusCode() == 400) {
            throw new RuntimeException("Gemini请求错误: " + response.body());
        } else if (response.statusCode() == 403) {
            throw new RuntimeException("Gemini认证失败: API Key无效");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini请求失败: " + response.statusCode() +
                    ", " + response.body());
        }

        // 解析响应
        JSONObject responseJson = JSON.parseObject(response.body());

        // 记录token使用情况（Gemini在免费层通常不返回详细usage）
        if (responseJson.containsKey("usageMetadata")) {
            JSONObject usage = responseJson.getJSONObject("usageMetadata");
            int promptTokens = usage.getIntValue("promptTokenCount");
            int candidatesTokens = usage.getIntValue("candidatesTokenCount");
            int totalTokens = usage.getIntValue("totalTokenCount");

            log.info("Gemini token使用: prompt={}, candidates={}, total={}, cost=$0 (免费)",
                    promptTokens, candidatesTokens, totalTokens);
        }

        // 提取响应内容
        try {
            return responseJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        } catch (Exception e) {
            log.error("解析Gemini响应失败: {}", response.body());
            throw new RuntimeException("解析Gemini响应失败", e);
        }
    }

    /**
     * 估算token数量
     */
    public int estimateTokens(String text) {
        int chars = text.length();
        boolean hasChinese = text.matches(".*[\\u4e00-\\u9fa5].*");

        if (hasChinese) {
            return (int) (chars / 1.5);
        } else {
            return chars / 4;
        }
    }

    /**
     * 估算成本（Gemini免费层）
     */
    public double getCost(int tokens) {
        return FREE_TIER_COST; // 免费使用
    }
}
