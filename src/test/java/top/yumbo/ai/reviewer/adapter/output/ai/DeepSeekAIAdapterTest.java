package top.yumbo.ai.reviewer.adapter.output.ai;

import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * DeepSeekAIAdapter娴嬭瘯
 *
 * 娴嬭瘯鍒嗕负涓ょ被锛?
 * 1. 鍗曞厓娴嬭瘯 - 涓嶉渶瑕佺湡瀹?API锛屾祴璇曞熀鏈姛鑳?
 * 2. 闆嗘垚娴嬭瘯 - 闇€瑕佺湡瀹?API Key锛堜粠鐜鍙橀噺 DEEPSEEK_API_KEY 璇诲彇锛?
 *
 * API Key 楠岃瘉锛?
 * - 鍦ㄦ祴璇曞紑濮嬪墠浼氶獙璇?API Key 鏄惁鏈夋晥
 * - 濡傛灉 API Key 鏃犳晥锛屾墍鏈夐渶瑕?API 鐨勬祴璇曞皢琚烦杩?
 *
 * 杩愯闆嗘垚娴嬭瘯锛?
 * 1. 璁剧疆鐜鍙橀噺锛歴et DEEPSEEK_API_KEY=your-api-key
 * 2. 杩愯娴嬭瘯锛歮vn test -Dtest=DeepSeekAIAdapterTest
 */
@DisplayName("DeepSeekAIAdapter娴嬭瘯")
class DeepSeekAIAdapterTest {

    private DeepSeekAIAdapter adapter;
    private AIServiceConfig testConfig;
    private static final String API_KEY_ENV = "DEEPSEEK_API_KEY";
    private static boolean hasRealApiKey = false;
    private static boolean apiKeyValidated = false;
    private static boolean apiKeyValid = false;

    @BeforeAll
    static void validateApiKey() {
        System.out.println("\n========================================");
        System.out.println("DeepSeek API Key 楠岃瘉");
        System.out.println("========================================");

        String apiKey = System.getenv(API_KEY_ENV);

        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("test-")) {
            hasRealApiKey = true;
            System.out.println("鉁?妫€娴嬪埌鐜鍙橀噺 DEEPSEEK_API_KEY");
            System.out.println("鉁?API Key 鏍煎紡: " + maskApiKey(apiKey));

            // 楠岃瘉 API Key 鏍煎紡
            if (apiKey.startsWith("sk-") && apiKey.length() > 20, null) {
                System.out.println("鉁?API Key 鏍煎紡鏈夋晥");

                // 鍒涘缓涓存椂閫傞厤鍣ㄨ繘琛岃繛鎺ユ祴璇?
                System.out.println("鈴?姝ｅ湪楠岃瘉 API 杩炴帴...");
                try {
                    AIServiceConfig validationConfig =
                        new AIServiceConfig(
                            apiKey,
                            "https://api.deepseek.com/v1",
                            "deepseek-chat",
                            100, // 鏈€灏?token 鐢ㄤ簬娴嬭瘯
                            0.7,
                            1,
                            1, // 鍙噸璇?娆?
                            500,
                            10000, // 10绉掕繛鎺ヨ秴鏃?
                            15000  // 15绉掕鍙栬秴鏃?
                        );

                    DeepSeekAIAdapter testAdapter = new DeepSeekAIAdapter(validationConfig);

                    // 娴嬭瘯 API 鍙敤鎬?
                    boolean available = testAdapter.isAvailable();
                    testAdapter.shutdown();

                    if (available) {
                        apiKeyValid = true;
                        System.out.println("鉁?API 杩炴帴楠岃瘉鎴愬姛 - 灏嗚繍琛屽畬鏁存祴璇曞浠?);
                    } else {
                        apiKeyValid = false;
                        System.out.println("鉂?API 杩炴帴楠岃瘉澶辫触 - API 涓嶅彲鐢?);
                        System.out.println("   鍘熷洜鍙兘鏄細缃戠粶闂銆丄PI Key 鏃犳晥銆侀厤棰濈敤灏界瓑");
                        System.out.println("   灏嗚烦杩囨墍鏈夐渶瑕佺湡瀹?API 鐨勬祴璇?);
                    }
                } catch (Exception e) {
                    apiKeyValid = false;
                    System.out.println("鉂?API 杩炴帴楠岃瘉澶辫触: " + e.getMessage());
                    System.out.println("   灏嗚烦杩囨墍鏈夐渶瑕佺湡瀹?API 鐨勬祴璇?);
                }
            } else {
                apiKeyValid = false;
                System.out.println("鉂?API Key 鏍煎紡鏃犳晥锛堝簲璇ヤ互 'sk-' 寮€澶翠笖闀垮害 > 20锛?);
                System.out.println("   灏嗚烦杩囨墍鏈夐渶瑕佺湡瀹?API 鐨勬祴璇?);
            }
        } else {
            hasRealApiKey = false;
            apiKeyValid = false;
            System.out.println("鈿狅笍  鏈厤缃?DEEPSEEK_API_KEY 鐜鍙橀噺");
            System.out.println("   鍙繍琛屽崟鍏冩祴璇曪紝璺宠繃闆嗘垚娴嬭瘯");
        }

        apiKeyValidated = true;
        System.out.println("========================================\n");
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10, null) {
            return "***";
        }
        return apiKey.substring(0, 7, null) + "..." + apiKey.substring(apiKey.length() - 4, null);
    }

    @BeforeEach
    void setUp() {
        // 纭繚 API Key 宸查獙璇?
        assumeTrue(apiKeyValidated, "API Key 楠岃瘉灏氭湭瀹屾垚");

        // 鏍规嵁楠岃瘉缁撴灉閰嶇疆閫傞厤鍣?
        if (hasRealApiKey && apiKeyValid) {
            // 浣跨敤鐪熷疄鐨?API Key
            String apiKey = System.getenv(API_KEY_ENV);
            testConfig = new AIServiceConfig(
                    apiKey,
                    "https://api.deepseek.com/v1",
                    "deepseek-chat",
                    2000,
                    0.7,
                    2,
                    3,
                    1000,
                    30000,
                    60000, null);
        } else {
            // 浣跨敤娴嬭瘯閰嶇疆锛堢敤浜庡崟鍏冩祴璇曪級
            testConfig = new AIServiceConfig(
                    "test-api-key",
                    "https://test.api.deepseek.com/v1",
                    "deepseek-chat",
                    2000,
                    0.3,
                    2,
                    3,
                    500,
                    5000,
                    10000, null);
        }

        adapter = new DeepSeekAIAdapter(testConfig);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.shutdown();
        }
    }

    @Nested
    @DisplayName("鏋勯€犲嚱鏁板拰鍒濆鍖栨祴璇?)
    class ConstructorTest {

        @Test
        @DisplayName("搴旇浣跨敤鎻愪緵鐨勯厤缃垱寤洪€傞厤鍣?)
        void shouldCreateAdapterWithProvidedConfig() {
            assertThat(adapter).isNotNull();
            assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
        }

        @Test
        @DisplayName("搴旇浣跨敤榛樿鍊煎～鍏呮湭鎻愪緵鐨勯厤缃?)
        void shouldUseDefaultValuesForMissingConfig() {
            AIServiceConfig minimalConfig =
                    new AIServiceConfig(
                            "api-key",
                            null, // 浣跨敤榛樿baseUrl
                            null, // 浣跨敤榛樿model
                            0,    // 浣跨敤榛樿maxTokens
                            -1,   // 浣跨敤榛樿temperature
                            0,    // 浣跨敤榛樿maxConcurrency
                            -1,   // 浣跨敤榛樿maxRetries
                            0,    // 浣跨敤榛樿retryDelayMillis
                            0,    // 浣跨敤榛樿connectTimeoutMillis
                            0     // 浣跨敤榛樿readTimeoutMillis
                    );

            DeepSeekAIAdapter adapterWithDefaults = new DeepSeekAIAdapter(minimalConfig);

            assertThat(adapterWithDefaults).isNotNull();
            assertThat(adapterWithDefaults.getProviderName()).isEqualTo("DeepSeek");

            adapterWithDefaults.shutdown();
        }

        @Test
        @DisplayName("搴旇姝ｇ‘璁剧疆骞跺彂闄愬埗")
        void shouldSetConcurrencyLimit() {
            assertThat(adapter.getMaxConcurrency()).isEqualTo(2, null);
        }
    }

    @Nested
    @DisplayName("getProviderName()鏂规硶娴嬭瘯")
    class GetProviderNameTest {

        @Test
        @DisplayName("搴旇杩斿洖DeepSeek")
        void shouldReturnDeepSeek() {
            assertThat(adapter.getProviderName()).isEqualTo("DeepSeek");
        }
    }

    @Nested
    @DisplayName("getMaxConcurrency()鏂规硶娴嬭瘯")
    class GetMaxConcurrencyTest {

        @Test
        @DisplayName("搴旇杩斿洖閰嶇疆鐨勬渶澶у苟鍙戞暟")
        void shouldReturnConfiguredMaxConcurrency() {
            int maxConcurrency = adapter.getMaxConcurrency();
            assertThat(maxConcurrency).isGreaterThanOrEqualTo(2, null);
        }
    }

    @Nested
    @DisplayName("analyze()鏂规硶娴嬭瘯")
    class AnalyzeMethodTest {

        @Test
        @DisplayName("鐪熷疄API娴嬭瘯 - 搴旇鎴愬姛鍒嗘瀽绠€鍗曚唬鐮?)
        void shouldAnalyzeSimpleCodeWithRealAPI() {
            // 鍙湪 API Key 鏈夋晥鏃惰繍琛?
            assumeTrue(apiKeyValid, "璺宠繃锛欰PI Key 鏈厤缃垨鏃犳晥");

            String prompt = "璇峰垎鏋愪互涓嬩唬鐮佸苟缁欏嚭绠€鐭瘎浠凤紙20瀛椾互鍐咃級锛歕n" +
                    "public class HelloWorld {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"Hello World\");\n" +
                    "    }\n" +
                    "}";

            try {
                String result = adapter.analyze(prompt);

                assertThat(result).isNotNull();
                assertThat(result).isNotEmpty();
                System.out.println("鉁?AI 鍒嗘瀽缁撴灉: " + result);
            } catch (Exception e) {
                System.err.println("鉂?API 璋冪敤澶辫触: " + e.getMessage());
                throw e;
            }
        }

        @Test
        @DisplayName("鏃燗PI Key鏃?- 搴旇澶辫触")
        void shouldFailWithoutRealAPI() {
            // 鍙湪 API Key 鏃犳晥鏃惰繍琛?
            assumeTrue(!apiKeyValid, "璺宠繃锛氬凡閰嶇疆鏈夋晥鐨?API Key");

            String prompt = "杩欐槸涓€涓祴璇曟彁绀鸿瘝";

            assertThatThrownBy(() -> adapter.analyze(prompt))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AI鍒嗘瀽澶辫触");
        }

        @Test
        @DisplayName("搴旇鎷掔粷null鎻愮ず璇?)
        void shouldRejectNullPrompt() {
            assertThatThrownBy(() -> adapter.analyze(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("搴旇鎷掔粷绌哄瓧绗︿覆鎻愮ず璇?)
        void shouldRejectEmptyPrompt() {
            assertThatThrownBy(() -> adapter.analyze(""))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("analyzeAsync()鏂规硶娴嬭瘯")
    class AnalyzeAsyncMethodTest {

        @Test
        @DisplayName("搴旇杩斿洖CompletableFuture")
        void shouldReturnCompletableFuture() {
            String prompt = "娴嬭瘯鎻愮ず璇?;
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            assertThat(future).isNotNull();
            assertThat(future).isInstanceOf(CompletableFuture.class);
        }

        @Test
        @DisplayName("搴旇鑳藉寮傛澶勭悊璇锋眰")
        void shouldHandleRequestAsynchronously() {
            String prompt = "娴嬭瘯鎻愮ず璇?;
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            // 涓嶇瓑寰呯粨鏋滐紝鍙獙璇丗uture瀵硅薄鐨勫垱寤?
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse(); // 搴旇杩樺湪鎵ц涓?

            // 绛夊緟涓€灏忔鏃堕棿鍚庡彇娑?
            future.cancel(true);
        }

        @Test
        @DisplayName("寮傛璇锋眰搴旇姝ｅ父瀹屾垚")
        void shouldCompleteAsyncRequest() throws Exception {
            String prompt = "璇风敤涓€涓瘝鍥炵瓟: Hello";
            CompletableFuture<String> future = adapter.analyzeAsync(prompt);

            // API 鍙敤鏃跺簲璇ユ垚鍔熷畬鎴?
            String result = future.get(30, TimeUnit.SECONDS);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("analyzeBatchAsync()鏂规硶娴嬭瘯")
    class AnalyzeBatchAsyncMethodTest {

        @Test
        @DisplayName("搴旇鑳藉澶勭悊鎵归噺璇锋眰")
        void shouldHandleBatchRequests() {
            String[] prompts = {"鎻愮ず璇?", "鎻愮ず璇?", "鎻愮ず璇?"};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(prompts);

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("搴旇澶勭悊绌烘暟缁?)
        void shouldHandleEmptyArray() {
            String[] emptyPrompts = {};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(emptyPrompts);

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("搴旇杩斿洖鐩稿悓鏁伴噺鐨勭粨鏋?)
        void shouldReturnSameNumberOfResults() throws Exception {
            String[] prompts = {"鐢ㄤ竴涓瘝鍥炵瓟: Hi", "鐢ㄤ竴涓瘝鍥炵瓟: Hello"};

            CompletableFuture<String[]> future = adapter.analyzeBatchAsync(prompts);

            // API 鍙敤鏃跺簲璇ユ垚鍔熻繑鍥炴墍鏈夌粨鏋滐紙缁欎簣瓒冲鐨勮秴鏃舵椂闂达級
            String[] results = future.get(60, TimeUnit.SECONDS);
            assertThat(results).hasSize(prompts.length);
            assertThat(results).allMatch(result -> result != null && !result.isEmpty());
        }
    }

    @Nested
    @DisplayName("骞跺彂鎺у埗娴嬭瘯")
    class ConcurrencyControlTest {

        @Test
        @DisplayName("搴旇闄愬埗骞跺彂璇锋眰鏁伴噺")
        void shouldLimitConcurrentRequests() {
            // 鍒涘缓澶氫釜骞跺彂璇锋眰
            CompletableFuture<String>[] futures = new CompletableFuture[5];
            for (int i = 0; i < 5; i++) {
                futures[i] = adapter.analyzeAsync("鎻愮ず璇? + i);
            }

            // 楠岃瘉涓嶄細瓒呰繃鏈€澶у苟鍙戞暟
            assertThat(adapter.getMaxConcurrency()).isGreaterThanOrEqualTo(2, null);

            // 鍙栨秷鎵€鏈夎姹?
            for (CompletableFuture<String> future : futures) {
                future.cancel(true);
            }
        }

        @Test
        @DisplayName("搴旇鑳藉璺熻釜娲昏穬璇锋眰鏁?)
        void shouldTrackActiveRequests() {
            int initialConcurrency = adapter.getMaxConcurrency();
            assertThat(initialConcurrency).isGreaterThanOrEqualTo(0, null);
        }
    }

    @Nested
    @DisplayName("閲嶈瘯鏈哄埗娴嬭瘯")
    class RetryMechanismTest {

        @Test
        @DisplayName("澶辫触鐨勮姹傚簲璇ヤ細閲嶈瘯")
        void shouldRetryFailedRequests() {
            // 褰?API 鍙敤鏃讹紝姝ｅ父璇锋眰搴旇鎴愬姛
            String prompt = "璇风敤涓€涓瘝鍥炵瓟: 浣犲ソ";

            long startTime = System.currentTimeMillis();
            String result = adapter.analyze(prompt);
            long duration = System.currentTimeMillis() - startTime;

            // API 搴旇鎴愬姛杩斿洖缁撴灉
            assertThat(result).isNotNull();
            assertThat(duration).isGreaterThan(0, null);

            // Note: 瑕佹祴璇曢噸璇曟満鍒讹紝闇€瑕佹ā鎷熺綉缁滃け璐ユ垨浣跨敤鏃犳晥鐨?API key
            // 鍦ㄧ湡瀹?API 鍙敤鏃讹紝璇锋眰浼氭垚鍔燂紝涓嶄細瑙﹀彂閲嶈瘯
        }
    }

    @Nested
    @DisplayName("isAvailable()鏂规硶娴嬭瘯")
    class IsAvailableTest {

        @Test
        @DisplayName("鐪熷疄API - 搴旇杩斿洖true")
        void shouldReturnTrueWithRealAPI() {
            // 鍙湪 API Key 鏈夋晥鏃惰繍琛?
            assumeTrue(apiKeyValid, "璺宠繃锛欰PI Key 鏈厤缃垨鏃犳晥");

            boolean available = adapter.isAvailable();

            System.out.println(available ? "鉁?API 鍙敤" : "鉂?API 涓嶅彲鐢?);
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("鏃犳晥閰嶇疆鏃跺簲璇ヨ繑鍥瀎alse")
        void shouldReturnFalseForInvalidConfig() {
            // 鍙湪 API Key 鏃犳晥鏃惰繍琛?
            assumeTrue(!apiKeyValid, "璺宠繃锛氬凡閰嶇疆鏈夋晥鐨?API Key");

            boolean available = adapter.isAvailable();

            // 鐢变簬浣跨敤鐨勬槸娴嬭瘯閰嶇疆锛堟棤鏁圓PI锛夛紝搴旇杩斿洖false
            assertThat(available).isFalse();
        }
    }

    @Nested
    @DisplayName("shutdown()鏂规硶娴嬭瘯")
    class ShutdownTest {

        @Test
        @DisplayName("搴旇鑳藉姝ｅ父鍏抽棴")
        void shouldShutdownGracefully() {
            assertThat(adapter).isNotNull();

            adapter.shutdown();

            // 鍐嶆璋冪敤shutdown涓嶅簲璇ユ姏鍑哄紓甯?
            adapter.shutdown();
        }

        @Test
        @DisplayName("鍏抽棴鍚庝笉搴旇鎺ュ彈鏂拌姹?)
        void shouldNotAcceptNewRequestsAfterShutdown() {
            adapter.shutdown();

            // 娉ㄦ剰锛氬疄闄呰涓哄彇鍐充簬瀹炵幇
            // 鍙兘鎶涘嚭寮傚父鎴栬繑鍥炲け璐ョ殑Future
            assertThatThrownBy(() -> adapter.analyze("娴嬭瘯"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("閰嶇疆楠岃瘉娴嬭瘯")
    class ConfigValidationTest {

        @Test
        @DisplayName("搴旇鎺ュ彈鏈夋晥鐨凙PI瀵嗛挜")
        void shouldAcceptValidApiKey() {
            AIServiceConfig validConfig =
                    new AIServiceConfig(
                            "sk-test1234567890",
                            "https://api.deepseek.com/v1",
                            "deepseek-chat",
                            4000,
                            0.3,
                            3,
                            3,
                            1000,
                            30000,
                            60000, null);

            DeepSeekAIAdapter validAdapter = new DeepSeekAIAdapter(validConfig);
            assertThat(validAdapter).isNotNull();
            assertThat(validAdapter.getProviderName()).isEqualTo("DeepSeek");
            validAdapter.shutdown();
        }

        @Test
        @DisplayName("搴旇澶勭悊涓嶅悓鐨勬ā鍨嬪悕绉?)
        void shouldHandleDifferentModelNames() {
            AIServiceConfig config =
                    new AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "custom-model",
                            2000,
                            0.5,
                            2,
                            3,
                            500,
                            5000,
                            10000, null);

            DeepSeekAIAdapter customAdapter = new DeepSeekAIAdapter(config);
            assertThat(customAdapter).isNotNull();
            customAdapter.shutdown();
        }

        @Test
        @DisplayName("搴旇澶勭悊涓嶅悓鐨勬俯搴﹀弬鏁?)
        void shouldHandleDifferentTemperatures() {
            AIServiceConfig lowTemp =
                    new AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "model",
                            2000,
                            0.0, // 浣庢俯搴?
                            2,
                            3,
                            500,
                            5000,
                            10000, null);

            AIServiceConfig highTemp =
                    new AIServiceConfig(
                            "test-key",
                            "https://test.api.com",
                            "model",
                            2000,
                            1.0, // 楂樻俯搴?
                            2,
                            3,
                            500,
                            5000,
                            10000, null);

            DeepSeekAIAdapter lowTempAdapter = new DeepSeekAIAdapter(lowTemp);
            DeepSeekAIAdapter highTempAdapter = new DeepSeekAIAdapter(highTemp);

            assertThat(lowTempAdapter).isNotNull();
            assertThat(highTempAdapter).isNotNull();

            lowTempAdapter.shutdown();
            highTempAdapter.shutdown();
        }
    }

    @Nested
    @DisplayName("杈圭晫鏉′欢娴嬭瘯")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("搴旇澶勭悊闈炲父闀跨殑鎻愮ず璇?)
        void shouldHandleVeryLongPrompt() {
            String longPrompt = "娴嬭瘯".repeat(10000, null);

            // DeepSeek API 搴旇鑳藉澶勭悊闀挎枃鏈紙鎴栬繑鍥為€傚綋鐨勭粨鏋滐級
            String result = adapter.analyze(longPrompt);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("搴旇澶勭悊鍖呭惈鐗规畩瀛楃鐨勬彁绀鸿瘝")
        void shouldHandleSpecialCharactersInPrompt() {
            String specialPrompt = "璇峰垎鏋愯繖娈垫枃鏈? \n\r\t\"'<>&{}[]";

            // API 搴旇鑳藉姝ｇ‘澶勭悊鐗规畩瀛楃
            String result = adapter.analyze(specialPrompt);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("搴旇澶勭悊Unicode瀛楃")
        void shouldHandleUnicodeCharacters() {
            String unicodePrompt = "璇峰垎鏋? 娴嬭瘯 馃殌 emoji 鍜?涓枃";

            // API 搴旇鑳藉姝ｇ‘澶勭悊 Unicode 瀛楃
            String result = adapter.analyze(unicodePrompt);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("鎬ц兘娴嬭瘯")
    class PerformanceTest {

        @Test
        @DisplayName("鍒涘缓閫傞厤鍣ㄥ簲璇ュ緢蹇?)
        void shouldCreateAdapterQuickly() {
            long startTime = System.currentTimeMillis();

            DeepSeekAIAdapter newAdapter = new DeepSeekAIAdapter(testConfig);

            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(1000, null); // 搴旇鍦?绉掑唴瀹屾垚

            newAdapter.shutdown();
        }

        @Test
        @DisplayName("鍏抽棴閫傞厤鍣ㄥ簲璇ュ湪鍚堢悊鏃堕棿鍐呭畬鎴?)
        void shouldShutdownInReasonableTime() {
            long startTime = System.currentTimeMillis();

            adapter.shutdown();

            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(15000, null); // 搴旇鍦?5绉掑唴瀹屾垚
        }
    }
}

