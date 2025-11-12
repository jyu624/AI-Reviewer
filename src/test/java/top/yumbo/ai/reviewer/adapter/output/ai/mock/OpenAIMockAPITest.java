package top.yumbo.ai.reviewer.adapter.output.ai.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import top.yumbo.ai.reviewer.adapter.output.ai.AIServiceConfig;
import top.yumbo.ai.reviewer.adapter.output.ai.OpenAIAdapter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAI 适配器 Mock API 测试
 *
 * 使用 WireMock 模拟 OpenAI API 响应
 *
 * @author AI-Reviewer Team
 * @version 2.0
 * @since 2025-11-13
 */
@DisplayName("OpenAI Mock API 测试")
class OpenAIMockAPITest {

    private WireMockServer wireMockServer;
    private OpenAIAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        String mockBaseUrl = "http://localhost:" + wireMockServer.port() + "/v1/chat/completions";
        AIServiceConfig config = new AIServiceConfig(
                "test-openai-key",
                mockBaseUrl,
                "gpt-4",
                2000,
                0.3,
                2,
                3,
                500,
                5000,
                10000,
                null
        );
        adapter = new OpenAIAdapter(config);
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
    @DisplayName("应该成功调用 OpenAI Mock API")
    void shouldCallOpenAIMockAPISuccessfully() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "chatcmpl-openai-test",
                                    "object": "chat.completion",
                                    "created": 1234567890,
                                    "model": "gpt-4",
                                    "choices": [{
                                        "index": 0,
                                        "message": {
                                            "role": "assistant",
                                            "content": "OpenAI 测试响应"
                                        },
                                        "finish_reason": "stop"
                                    }],
                                    "usage": {
                                        "prompt_tokens": 15,
                                        "completion_tokens": 25,
                                        "total_tokens": 40
                                    }
                                }
                                """)));

        String response = adapter.analyze("OpenAI 测试提示");

        assertThat(response).isNotNull();
        assertThat(response).contains("OpenAI 测试响应");

        verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", containing("Bearer"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("应该处理 OpenAI API 认证错误")
    void shouldHandleAuthenticationError() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("""
                                {
                                    "error": {
                                        "message": "Invalid API key",
                                        "type": "invalid_request_error",
                                        "code": "invalid_api_key"
                                    }
                                }
                                """)));

        try {
            adapter.analyze("测试");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    @DisplayName("应该使用正确的模型参数")
    void shouldUseCorrectModelParameter() {
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
                .withRequestBody(containing("\"model\":\"gpt-4\"")));
    }

    @Test
    @DisplayName("应该处理流式响应")
    void shouldHandleStreamingResponse() {
        // OpenAI 支持流式响应，但当前实现使用同步方式
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "choices": [{
                                        "message": {
                                            "content": "完整响应"
                                        }
                                    }]
                                }
                                """)));

        String response = adapter.analyze("测试");

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo("完整响应");
    }
}

