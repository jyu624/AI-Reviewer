package top.yumbo.ai.reviewer.adapter.storage.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.CachePort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 文件缓存适配器
 * 实现CachePort接口，基于文件系统的缓存实现
 *
 * 功能特性：
 * - TTL支持：每个缓存条目支持独立的过期时间
 * - 自动清理：定期清理过期缓存（默认每10分钟）
 * - 类型策略：支持按文件类型设置不同的TTL
 * - 统计信息：提供缓存命中率、大小等统计
 */
@Slf4j
public class FileCacheAdapter implements CachePort {

    private static final String DEFAULT_CACHE_DIR = ".ai-reviewer-cache";
    private static final String CACHE_INDEX_FILE = "cache-index.json";

    // 默认TTL配置（单位：秒）
    private static final long DEFAULT_TTL_SECONDS = 3600; // 1小时
    private static final long MEDIA_TTL_SECONDS = 86400;  // 24小时
    private static final long DOCUMENT_TTL_SECONDS = 43200; // 12小时
    private static final long ANALYSIS_TTL_SECONDS = 21600; // 6小时

    // 清理任务间隔（单位：分钟）
    private static final long CLEANUP_INTERVAL_MINUTES = 10;

    private final Path cacheDir;
    private final Map<String, CacheEntry> memoryIndex;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0); // 过期清理次数
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService cleanupScheduler;

    public FileCacheAdapter() {
        this(Paths.get(System.getProperty("user.home"), DEFAULT_CACHE_DIR));
    }

    public FileCacheAdapter(Path cacheDir) {
        this.cacheDir = cacheDir;
        this.memoryIndex = new ConcurrentHashMap<>();
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleanup");
            thread.setDaemon(true);
            return thread;
        });

        try {
            Files.createDirectories(cacheDir);
            loadCacheIndex();
            startCleanupTask();
            log.info("文件缓存适配器已初始化: dir={}, cleanupInterval={}min",
                cacheDir, CLEANUP_INTERVAL_MINUTES);
        } catch (IOException e) {
            log.error("创建缓存目录失败", e);
        }
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        // 如果未指定TTL，根据key类型智能选择
        if (ttlSeconds <= 0) {
            ttlSeconds = getDefaultTTLForKey(key);
        }

        lock.writeLock().lock();
        try {
            String fileName = generateFileName(key);
            Path cacheFile = cacheDir.resolve(fileName);
            String fileType = detectFileType(key);

            CacheEntry entry = new CacheEntry(
                    key,
                    fileName,
                    System.currentTimeMillis(),
                    ttlSeconds * 1000,
                    fileType,
                    value.length()
            );

            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put("key", key);
            jsonEntry.put("fileName", fileName);
            jsonEntry.put("timestamp", entry.timestamp);
            jsonEntry.put("ttlMillis", entry.ttlMillis);
            jsonEntry.put("fileType", entry.fileType);
            jsonEntry.put("size", entry.size);
            jsonEntry.put("value", value);

            // 写入文件
            Files.writeString(cacheFile, jsonEntry.toJSONString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 更新内存索引
            memoryIndex.put(key, entry);

            log.debug("缓存写入: key={}, file={}", key, fileName);

        } catch (IOException e) {
            log.error("写入缓存失败: key={}", key, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<String> get(String key) {
        lock.readLock().lock();
        try {
            CacheEntry entry = memoryIndex.get(key);
            if (entry == null) {
                misses.incrementAndGet();
                return Optional.empty();
            }

            // 检查是否过期
            if (System.currentTimeMillis() > entry.getExpiryTime()) {
                lock.readLock().unlock();
                remove(key);
                lock.readLock().lock();
                misses.incrementAndGet();
                return Optional.empty();
            }

            Path cacheFile = cacheDir.resolve(entry.fileName);
            if (!Files.exists(cacheFile)) {
                memoryIndex.remove(key);
                misses.incrementAndGet();
                return Optional.empty();
            }

            String content = Files.readString(cacheFile);
            JSONObject jsonEntry = JSON.parseObject(content);
            String value = jsonEntry.getString("value");

            hits.incrementAndGet();
            log.debug("缓存命中: key={}", key);
            return Optional.of(value);

        } catch (IOException e) {
            log.error("读取缓存失败: key={}", key, e);
            memoryIndex.remove(key);
            misses.incrementAndGet();
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void remove(String key) {
        lock.writeLock().lock();
        try {
            CacheEntry entry = memoryIndex.remove(key);
            if (entry != null) {
                Path cacheFile = cacheDir.resolve(entry.fileName);
                Files.deleteIfExists(cacheFile);
                log.debug("缓存删除: key={}", key);
            }
        } catch (IOException e) {
            log.error("删除缓存文件失败: key={}", key, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            // 删除所有缓存文件
            try (var stream = Files.list(cacheDir)) {
                stream.filter(path -> !path.getFileName().toString().equals(CACHE_INDEX_FILE))
                      .forEach(path -> {
                          try {
                              Files.delete(path);
                          } catch (IOException e) {
                              log.error("删除缓存文件失败: {}", path, e);
                          }
                      });
            }

            memoryIndex.clear();
            hits.set(0);
            misses.set(0);
            evictions.set(0);

            log.info("缓存已清空");

        } catch (IOException e) {
            log.error("清空缓存失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CacheStats getStats() {
        return new CacheStats(hits.get(), misses.get(), memoryIndex.size());
    }

    @Override
    public void close() {
        log.info("关闭文件缓存适配器...");

        // 关闭清理任务
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 执行最后一次清理
        cleanupExpiredCache();

        // 保存索引
        saveCacheIndex();

        log.info("文件缓存适配器已关闭，最终统计: {}", getEnhancedStats());
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String key) {
        return "cache-" + Integer.toHexString(key.hashCode()) + ".json";
    }

    /**
     * 加载缓存索引
     */
    private void loadCacheIndex() {
        Path indexFile = cacheDir.resolve(CACHE_INDEX_FILE);
        if (!Files.exists(indexFile)) {
            return;
        }

        try {
            String content = Files.readString(indexFile);
            JSONObject json = JSON.parseObject(content);
            // 简化实现：仅加载基本信息
            log.debug("缓存索引已加载");
        } catch (IOException e) {
            log.warn("加载缓存索引失败", e);
        }
    }

    /**
     * 保存缓存索引
     */
    private void saveCacheIndex() {
        Path indexFile = cacheDir.resolve(CACHE_INDEX_FILE);
        try {
            JSONObject json = new JSONObject();
            json.put("size", memoryIndex.size());
            json.put("hits", hits.get());
            json.put("misses", misses.get());
            json.put("timestamp", System.currentTimeMillis());

            Files.writeString(indexFile, json.toJSONString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.debug("缓存索引已保存");
        } catch (IOException e) {
            log.warn("保存缓存索引失败", e);
        }
    }

    /**
     * 缓存条目
     */
    private record CacheEntry(
            String key,
            String fileName,
            long timestamp,
            long ttlMillis,
            String fileType,
            long size
    ) {
        long getExpiryTime() {
            return timestamp + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > getExpiryTime();
        }
    }

    /**
     * 启动定期清理任务
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(
            this::cleanupExpiredCache,
            CLEANUP_INTERVAL_MINUTES,
            CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
        log.debug("缓存清理任务已启动，间隔: {}分钟", CLEANUP_INTERVAL_MINUTES);
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        lock.writeLock().lock();
        try {
            int cleanupCount = 0;
            long reclaimedSpace = 0;

            var expiredEntries = memoryIndex.entrySet().stream()
                .filter(e -> e.getValue().isExpired())
                .toList();

            for (var entry : expiredEntries) {
                String key = entry.getKey();
                CacheEntry cacheEntry = entry.getValue();

                try {
                    Path cacheFile = cacheDir.resolve(cacheEntry.fileName);
                    Files.deleteIfExists(cacheFile);
                    memoryIndex.remove(key);

                    reclaimedSpace += cacheEntry.size;
                    cleanupCount++;
                    evictions.incrementAndGet();
                } catch (IOException e) {
                    log.warn("删除过期缓存失败: key={}", key, e);
                }
            }

            if (cleanupCount > 0) {
                log.info("清理过期缓存完成: count={}, reclaimedSpace={}KB",
                    cleanupCount, reclaimedSpace / 1024);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检测文件类型
     */
    private String detectFileType(String key) {
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("image") || lowerKey.contains("media") ||
            lowerKey.contains("video") || lowerKey.contains("audio")) {
            return "MEDIA";
        } else if (lowerKey.contains("document") || lowerKey.contains("pdf") ||
                   lowerKey.contains("doc") || lowerKey.contains("ppt")) {
            return "DOCUMENT";
        } else if (lowerKey.contains("ast") || lowerKey.contains("analysis") ||
                   lowerKey.contains("report")) {
            return "ANALYSIS";
        }
        return "GENERAL";
    }

    /**
     * 根据key类型获取默认TTL
     */
    private long getDefaultTTLForKey(String key) {
        String fileType = detectFileType(key);
        return switch (fileType) {
            case "MEDIA" -> MEDIA_TTL_SECONDS;
            case "DOCUMENT" -> DOCUMENT_TTL_SECONDS;
            case "ANALYSIS" -> ANALYSIS_TTL_SECONDS;
            default -> DEFAULT_TTL_SECONDS;
        };
    }

    /**
     * 获取增强的缓存统计信息
     */
    public EnhancedCacheStats getEnhancedStats() {
        lock.readLock().lock();
        try {
            long totalSize = memoryIndex.values().stream()
                .mapToLong(CacheEntry::size)
                .sum();

            Map<String, Integer> typeCount = memoryIndex.values().stream()
                .collect(Collectors.groupingBy(
                    CacheEntry::fileType,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

            long expiredCount = memoryIndex.values().stream()
                .filter(CacheEntry::isExpired)
                .count();

            double hitRate = (hits.get() + misses.get()) > 0
                ? (double) hits.get() / (hits.get() + misses.get()) * 100
                : 0;

            return new EnhancedCacheStats(
                memoryIndex.size(),
                totalSize,
                hits.get(),
                misses.get(),
                evictions.get(),
                hitRate,
                typeCount,
                expiredCount
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 增强的缓存统计信息
     */
    public record EnhancedCacheStats(
        int totalEntries,
        long totalSizeBytes,
        long hits,
        long misses,
        long evictions,
        double hitRate,
        Map<String, Integer> typeDistribution,
        long expiredCount
    ) {
        @Override
        public @org.jetbrains.annotations.NotNull String toString() {
            return String.format(
                "CacheStats{entries=%d, size=%dMB, hits=%d, misses=%d, " +
                "evictions=%d, hitRate=%.2f%%, expired=%d, types=%s}",
                totalEntries, totalSizeBytes / 1024 / 1024, hits, misses,
                evictions, hitRate, expiredCount, typeDistribution
            );
        }
    }
}