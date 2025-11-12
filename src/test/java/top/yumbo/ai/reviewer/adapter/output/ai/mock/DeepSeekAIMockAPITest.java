package top.yumbo.ai.reviewer.adapter.output.ai.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import top.yumbo.ai.reviewer.adapter.output.ai.AIServiceConfig;
import top.yumbo.ai.reviewer.adapter.output.ai.DeepSeekAIAdapter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeepSeek AI 适配器 Mock API 测试
 *
 * 使用 WireMock 模拟 DeepSeek API 响应
 *
 * @author AI-Reviewer Team
 * @version 2.0
 * @since 2025-11-13
 */
@DisplayName("DeepSeek Mock API 测试")
class DeepSeekAIMockAPITest {

    private WireMockServer wireMockServer;
    private DeepSeekAIAdapter adapter;

    @BeforeEach
    void setUp() {
        // 启动 WireMock 服务器
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        // 创建适配器，使用 WireMock URL
        String mockBaseUrl = "http://localhost:" + wireMockServer.port() + "/v1";
        AIServiceConfig config = new AIServiceConfig(
                "test-api-key",
                mockBaseUrl,
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
        adapter = new DeepSeekAIAdapter(config);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.shutdown();
        }
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("应该成功调用 Mock API")
    void shouldCallMockAPISuccessfully() {
        // 配置 Mock 响应
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "chatcmpl-test",
                                    "object": "chat.completion",
                                    "created": 1234567890,
                                    "model": "deepseek-chat",
                                    "choices": [{
                                        "index": 0,
                                        "message": {
                                            "role": "assistant",
                                            "content": "这是一个测试响应"
                                        },
                                        "finish_reason": "stop"
                                    }],
                                    "usage": {
                                        "prompt_tokens": 10,
                                        "completion_tokens": 20,
                                        "total_tokens": 30
                                    }
                                }
                                """)));

        // 调用 API
        String response = adapter.analyze("测试提示词");

        // 验证响应
        assertThat(response).isNotNull();
        assertThat(response).contains("测试响应");

        // 验证请求
        verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", containing("Bearer"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("应该处理 API 错误响应")
    void shouldHandleAPIErrorResponse() {
        // 配置错误响应
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("""
                                {
                                    "error": {
                                        "message": "Internal server error",
                                        "type": "server_error"
                                    }
                                }
                                """)));

        // 调用 API 应该抛出异常
        try {
            adapter.analyze("测试提示词");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }

        // 验证重试逻辑
        verify(exactly(3), postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    @DisplayName("应该处理超时")
    void shouldHandleTimeout() {
        // 配置延迟响应（超过超时时间）
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(15000) // 15秒延迟
                        .withBody("{}")));

        // 调用应该超时
        try {
            adapter.analyze("测试提示词");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("timeout");
        }
    }

    @Test
    @DisplayName("应该正确设置请求头")
    void shouldSetCorrectHeaders() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "choices": [{
                                        "message": {
                                            "content": "响应"
                                        }
                                    }]
                                }
                                """)));

        adapter.analyze("测试");

        verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", matching("Bearer test-api-key"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("应该正确格式化请求体")
    void shouldFormatRequestBodyCorrectly() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "choices": [{
                                        "message": {
                                            "content": "响应"
                                        }
                                    }]
                                }
                                """)));

        adapter.analyze("测试提示词");

        verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("model"))
                .withRequestBody(containing("deepseek-chat"))
                .withRequestBody(containing("messages"))
                .withRequestBody(containing("测试提示词")));
    }

    @Test
    @DisplayName("应该处理限流错误")
    void shouldHandleRateLimitError() {
        // 配置限流响应
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "1")
                        .withBody("""
                                {
                                    "error": {
                                        "message": "Rate limit exceeded",
                                        "type": "rate_limit_error"
                                    }
                                }
                                """)));

        // 调用应该重试
        try {
            adapter.analyze("测试提示词");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("API");
        }

        // 验证重试（至少 2 次）
        verify(2, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    @DisplayName("应该解析完整的响应体")
    void shouldParseCompleteResponse() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": "chatcmpl-123",
                                    "object": "chat.completion",
                                    "created": 1234567890,
                                    "model": "deepseek-chat",
                                    "choices": [{
                                        "index": 0,
                                        "message": {
                                            "role": "assistant",
                                            "content": "详细的分析结果：\\n1. 代码质量良好\\n2. 架构清晰\\n3. 文档完善"
                                        },
                                        "finish_reason": "stop"
                                    }],
                                    "usage": {
                                        "prompt_tokens": 100,
                                        "completion_tokens": 200,
                                        "total_tokens": 300
                                    }
                                }
                                """)));

        String response = adapter.analyze("请分析这段代码");

        assertThat(response).isNotNull();
        assertThat(response).contains("代码质量");
        assertThat(response).contains("架构清晰");
        assertThat(response).contains("文档完善");
    }
}

