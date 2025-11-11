package top.yumbo.ai.refactor.application.port.output;

import java.util.Optional;

/**
 * 缓存端口（输出端口）
 * 定义缓存服务的接口
 */
public interface CachePort {

    /**
     * 存储缓存
     * @param key 键
     * @param value 值
     * @param ttlSeconds 过期时间（秒）
     */
    void put(String key, String value, long ttlSeconds);

    /**
     * 获取缓存
     * @param key 键
     * @return 缓存值
     */
    Optional<String> get(String key);

    /**
     * 删除缓存
     * @param key 键
     */
    void remove(String key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 获取缓存统计
     */
    CacheStats getStats();

    /**
     * 关闭缓存
     */
    void close();

    /**
     * 缓存统计
     */
    record CacheStats(long hits, long misses, long size) {
        public double getHitRatio() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }
    }
}

