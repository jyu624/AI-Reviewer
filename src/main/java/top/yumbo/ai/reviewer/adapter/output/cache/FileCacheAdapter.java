package top.yumbo.ai.reviewer.adapter.output.cache;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文件缓存适配器
 * 实现CachePort接口，基于文件系统的缓存实现
 */
@Slf4j
public class FileCacheAdapter implements CachePort {

    private static final String DEFAULT_CACHE_DIR = ".ai-reviewer-cache";
    private static final String CACHE_INDEX_FILE = "cache-index.json";

    private final Path cacheDir;
    private final Map<String, CacheEntry> memoryIndex;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileCacheAdapter() {
        this(Paths.get(System.getProperty("user.home"), DEFAULT_CACHE_DIR));
    }

    public FileCacheAdapter(Path cacheDir) {
        this.cacheDir = cacheDir;
        this.memoryIndex = new ConcurrentHashMap<>();

        try {
            Files.createDirectories(cacheDir);
            loadCacheIndex();
            log.info("初始化文件缓存适配器: {}", cacheDir);
        } catch (IOException e) {
            log.error("创建缓存目录失败", e);
        }
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        lock.writeLock().lock();
        try {
            String fileName = generateFileName(key);
            Path cacheFile = cacheDir.resolve(fileName);

            CacheEntry entry = new CacheEntry(
                    key,
                    fileName,
                    System.currentTimeMillis(),
                    ttlSeconds * 1000
            );

            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put("key", key);
            jsonEntry.put("fileName", fileName);
            jsonEntry.put("timestamp", entry.timestamp);
            jsonEntry.put("ttlMillis", entry.ttlMillis);
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
            Files.list(cacheDir)
                    .filter(path -> !path.getFileName().toString().equals(CACHE_INDEX_FILE))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("删除缓存文件失败: {}", path, e);
                        }
                    });

            memoryIndex.clear();
            hits.set(0);
            misses.set(0);

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
        log.info("关闭文件缓存适配器");
        saveCacheIndex();
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
            long ttlMillis
    ) {
        long getExpiryTime() {
            return timestamp + ttlMillis;
        }
    }
}