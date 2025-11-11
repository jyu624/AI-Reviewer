package top.yumbo.ai.reviewer.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import top.yumbo.ai.reviewer.config.Config;
import top.yumbo.ai.reviewer.exception.AnalysisException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI服务实现
 */
@Slf4j
public class DeepseekAIService implements AIService {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final int DEFAULT_MAX_TOKENS = 4000;
    private static final double DEFAULT_TEMPERATURE = 0.3;

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    public DeepseekAIService(Config.AIServiceConfig config) {
        this.apiKey = config.getApiKey();
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
        this.model = config.getModel() != null ? config.getModel() : DEFAULT_MODEL;
        this.maxTokens = config.getMaxTokens() > 0 ? config.getMaxTokens() : DEFAULT_MAX_TOKENS;
        this.temperature = config.getTemperature() >= 0 ? config.getTemperature() : DEFAULT_TEMPERATURE;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        log.info("初始化DeepSeek AI服务: model={}, baseUrl={}", model, baseUrl);
    }

    @Override
    public String analyze(String prompt) throws AnalysisException {
        log.debug("开始AI分析，提示词长度: {}", prompt.length());

        try {
            String requestBody = buildRequestBody(prompt);
            String response = makeApiCall(requestBody);
            return parseResponse(response);

        } catch (IOException e) {
            log.error("AI服务调用失败", e);
            throw new AnalysisException(AnalysisException.ErrorType.AI_SERVICE_ERROR,
                    "AI服务调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String[] analyzeBatch(String[] prompts) throws AnalysisException {
        String[] results = new String[prompts.length];

        for (int i = 0; i < prompts.length; i++) {
            results[i] = analyze(prompts[i]);
            // 添加延迟避免API限制
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AnalysisException("批量分析被中断", e);
            }
        }

        return results;
    }

    @Override
    public String getProviderName() {
        return "DeepSeek";
    }

    @Override
    public boolean isAvailable() {
        try {
            // 发送一个简单的测试请求
            String testPrompt = "Hello";
            analyze(testPrompt);
            return true;
        } catch (Exception e) {
            log.warn("AI服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String prompt) {
        JSONObject request = new JSONObject();
        request.put("model", model);
        request.put("messages", JSON.toJSON(buildMessages(prompt)));
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        request.put("stream", false);

        return request.toJSONString();
    }

    /**
     * 构建消息列表
     */
    private Object[] buildMessages(String prompt) {
        return new Object[] {
            JSON.toJSON(new JSONObject()
                .fluentPut("role", "user")
                .fluentPut("content", prompt))
        };
    }

    /**
     * 调用API
     */
    private String makeApiCall(String requestBody) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API调用失败: " + response.code() + " " + response.message());
            }

            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    /**
     * 解析响应
     */
    private String parseResponse(String response) throws AnalysisException {
        try {
            JSONObject jsonResponse = JSON.parseObject(response);

            // 检查错误
            if (jsonResponse.containsKey("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                throw new AnalysisException("AI服务返回错误: " + error.getString("message"));
            }

            // 提取内容
            if (jsonResponse.containsKey("choices")) {
                var choices = jsonResponse.getJSONArray("choices");
                if (!choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content");
                }
            }

            throw new AnalysisException("AI服务返回格式错误");

        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            throw new AnalysisException("解析AI响应失败: " + e.getMessage(), e);
        }
    }
}
