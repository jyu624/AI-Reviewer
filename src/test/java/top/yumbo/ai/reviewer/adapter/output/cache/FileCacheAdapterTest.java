package top.yumbo.ai.reviewer.adapter.output.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.yumbo.ai.reviewer.application.port.output.CachePort;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * FileCacheAdapter单元测试
 */
@DisplayName("FileCacheAdapter单元测试")
class FileCacheAdapterTest {

    @TempDir
    Path tempDir;

    private FileCacheAdapter cache;

    @BeforeEach
    void setUp() {
        cache = new FileCacheAdapter(tempDir);
    }

    @Test
    @DisplayName("应该成功创建缓存适配器")
    void shouldCreateCacheAdapter() {
        assertThat(cache).isNotNull();
        assertThat(tempDir).exists();
    }

    @Test
    @DisplayName("应该成功存储和读取缓存")
    void shouldPutAndGetCache() {
        String key = "test-key";
        String value = "test-value";

        cache.put(key, value, 3600);
        Optional<String> result = cache.get(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(value);
    }

    @Test
    @DisplayName("获取不存在的键应该返回空")
    void shouldReturnEmptyForNonExistentKey() {
        Optional<String> result = cache.get("non-existent-key");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("应该支持更新已存在的键")
    void shouldUpdateExistingKey() {
        String key = "test-key";

        cache.put(key, "value1", 3600);
        cache.put(key, "value2", 3600);

        Optional<String> result = cache.get(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("value2");
    }

    @Test
    @DisplayName("应该正确处理TTL过期")
    void shouldHandleTTLExpiration() throws InterruptedException {
        String key = "test-key";
        String value = "test-value";

        // 设置1秒过期
        cache.put(key, value, 1);

        // 立即读取应该成功
        assertThat(cache.get(key)).isPresent();

        // 等待2秒后应该过期
        Thread.sleep(2000);

        assertThat(cache.get(key)).isEmpty();
    }

    @Test
    @DisplayName("应该成功删除缓存")
    void shouldRemoveCache() {
        String key = "test-key";

        cache.put(key, "value", 3600);
        assertThat(cache.get(key)).isPresent();

        cache.remove(key);
        assertThat(cache.get(key)).isEmpty();
    }

    @Test
    @DisplayName("删除不存在的键不应该抛出异常")
    void shouldNotThrowExceptionWhenRemovingNonExistentKey() {
        assertThatCode(() -> cache.remove("non-existent-key"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该成功清空所有缓存")
    void shouldClearAllCache() {
        cache.put("key1", "value1", 3600);
        cache.put("key2", "value2", 3600);
        cache.put("key3", "value3", 3600);

        cache.clear();

        assertThat(cache.get("key1")).isEmpty();
        assertThat(cache.get("key2")).isEmpty();
        assertThat(cache.get("key3")).isEmpty();
    }

    @Test
    @DisplayName("应该正确统计缓存命中率")
    void shouldTrackCacheHitRatio() {
        cache.put("key1", "value1", 3600);

        // 3次命中
        cache.get("key1");
        cache.get("key1");
        cache.get("key1");

        // 2次未命中
        cache.get("key2");
        cache.get("key3");

        CachePort.CacheStats stats = cache.getStats();

        assertThat(stats.hits()).isEqualTo(3);
        assertThat(stats.misses()).isEqualTo(2);
        assertThat(stats.getHitRatio()).isEqualTo(0.6); // 3/5 = 0.6
    }

    @Test
    @DisplayName("应该支持并发读取")
    void shouldSupportConcurrentReads() throws InterruptedException {
        String key = "test-key";
        String value = "test-value";
        cache.put(key, value, 3600);

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Optional<String> result = cache.get(key);
                    assertThat(result).isPresent();
                    assertThat(result.get()).isEqualTo(value);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        executor.shutdown();
    }

    @Test
    @DisplayName("应该支持并发写入")
    void shouldSupportConcurrentWrites() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    cache.put("key-" + index, "value-" + index, 3600);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // 验证所有键都被写入
        for (int i = 0; i < threadCount; i++) {
            Optional<String> result = cache.get("key-" + i);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("value-" + i);
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("应该正确关闭缓存")
    void shouldCloseCache() {
        cache.put("key", "value", 3600);

        assertThatCode(() -> cache.close()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该处理特殊字符的键")
    void shouldHandleSpecialCharactersInKey() {
        String key = "test:key/with\\special?chars";
        String value = "test-value";

        cache.put(key, value, 3600);
        Optional<String> result = cache.get(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(value);
    }

    @Test
    @DisplayName("应该处理大值存储")
    void shouldHandleLargeValues() {
        String key = "large-key";
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeValue.append("This is a test line ").append(i).append("\n");
        }

        cache.put(key, largeValue.toString(), 3600);
        Optional<String> result = cache.get(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(largeValue.toString());
    }

    @Test
    @DisplayName("清空后统计信息应该重置")
    void shouldResetStatsAfterClear() {
        cache.put("key1", "value1", 3600);
        cache.get("key1"); // 命中
        cache.get("key2"); // 未命中

        cache.clear();

        CachePort.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isZero();
        assertThat(stats.misses()).isZero();
        assertThat(stats.size()).isZero();
    }

    @Test
    @DisplayName("应该正确报告缓存大小")
    void shouldReportCacheSize() {
        assertThat(cache.getStats().size()).isZero();

        cache.put("key1", "value1", 3600);
        assertThat(cache.getStats().size()).isEqualTo(1);

        cache.put("key2", "value2", 3600);
        assertThat(cache.getStats().size()).isEqualTo(2);

        cache.remove("key1");
        assertThat(cache.getStats().size()).isEqualTo(1);
    }
}

