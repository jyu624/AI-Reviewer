import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class DeepSeekTest {
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("AI_API_KEY"); // 替换为DeepSeek的Key
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        System.out.println(invoke("你好"));
    }

    public static String invoke(String prompt) throws Exception {
        // 构建请求体
        String requestBody = objectMapper.writeValueAsString(new HashMap<>() {{
            put("model", "deepseek-coder"); // 正确的模型名
            put("messages", List.of(new HashMap<>() {{
                put("role", "user");
                put("content", prompt);
            }}));
            put("temperature", 0.2);
            put("max_tokens", 512);
        }});

        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY) // DeepSeek的Key
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 发送请求并处理响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("DeepSeek API调用失败：" + response.statusCode() + " - " + response.body());
        }

        // 解析响应（提取生成的代码）
        JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("choices").get(0).get("message").get("content").asText();
    }
}