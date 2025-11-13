package top.yumbo.ai.reviewer.adapter.output.storage;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import top.yumbo.ai.reviewer.domain.model.S3DownloadResult;
import top.yumbo.ai.reviewer.domain.model.S3File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3 存储适配器测试
 * 注意：这些测试需要在 AWS 环境中运行，并且需要正确配置 IAM 角色
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3StorageAdapterTest {

    private S3StorageAdapter s3Adapter;
    private String testBucketName;
    private String testPrefix;

    @BeforeAll
    void setUp() {
        // 从环境变量读取测试配置
        testBucketName = System.getenv("TEST_S3_BUCKET");
        testPrefix = System.getenv("TEST_S3_PREFIX");

        if (testBucketName == null || testBucketName.isEmpty()) {
            log.warn("未设置 TEST_S3_BUCKET 环境变量，跳过 S3 测试");
            return;
        }

        if (testPrefix == null || testPrefix.isEmpty()) {
            testPrefix = "test-projects/sample-app/";
        }

        // 创建使用 IAM 角色的配置
        S3StorageConfig config = S3StorageConfig.builder()
                .region("us-east-1")
                .bucketName(testBucketName)
                .maxConcurrency(5)
                .connectTimeout(30000)
                .readTimeout(60000)
                .maxRetries(3)
                .retryDelay(1000)
                .build();

        s3Adapter = new S3StorageAdapter(config);
        log.info("S3 测试环境初始化完成 - Bucket: {}, Prefix: {}", testBucketName, testPrefix);
    }

    @AfterAll
    void tearDown() {
        if (s3Adapter != null) {
            s3Adapter.shutdown();
        }
    }

    @Test
    @DisplayName("测试列出 S3 文件")
    void testListFiles() {
        // 如果未配置测试环境，跳过测试
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty(),
                "需要配置 TEST_S3_BUCKET 环境变量");

        List<S3File> files = s3Adapter.listFiles(testBucketName, testPrefix);

        assertNotNull(files);
        log.info("找到 {} 个文件", files.size());

        if (!files.isEmpty()) {
            S3File firstFile = files.get(0);
            assertNotNull(firstFile.getKey());
            assertNotNull(firstFile.getFileName());
            assertTrue(firstFile.getSizeInBytes() >= 0);
            log.info("第一个文件: {}", firstFile);
        }
    }

    @Test
    @DisplayName("测试检查文件是否存在")
    void testFileExists() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        List<S3File> files = s3Adapter.listFiles(testBucketName, testPrefix);
        Assumptions.assumeTrue(!files.isEmpty(), "测试前缀下需要有文件");

        String existingKey = files.get(0).getKey();
        boolean exists = s3Adapter.fileExists(testBucketName, existingKey);
        assertTrue(exists, "文件应该存在: " + existingKey);

        boolean notExists = s3Adapter.fileExists(testBucketName, "non-existing-file.txt");
        assertFalse(notExists, "不存在的文件应该返回 false");
    }

    @Test
    @DisplayName("测试获取文件元数据")
    void testGetFileMetadata() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        List<S3File> files = s3Adapter.listFiles(testBucketName, testPrefix);
        Assumptions.assumeTrue(!files.isEmpty(), "测试前缀下需要有文件");

        String key = files.get(0).getKey();
        S3File metadata = s3Adapter.getFileMetadata(testBucketName, key);

        assertNotNull(metadata);
        assertEquals(key, metadata.getKey());
        assertNotNull(metadata.getFileName());
        assertTrue(metadata.getSizeInBytes() >= 0);
        assertNotNull(metadata.getLastModified());
        log.info("文件元数据: {}", metadata);
    }

    @Test
    @DisplayName("测试下载单个文件")
    void testDownloadFile() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        List<S3File> files = s3Adapter.listFiles(testBucketName, testPrefix);
        Assumptions.assumeTrue(!files.isEmpty(), "测试前缀下需要有文件");

        String key = files.get(0).getKey();
        S3File downloadedFile = s3Adapter.downloadFile(testBucketName, key);

        assertNotNull(downloadedFile);
        assertNotNull(downloadedFile.getContent());
        assertTrue(downloadedFile.getContent().length > 0);
        assertEquals(downloadedFile.getContent().length, downloadedFile.getSizeInBytes());
        log.info("下载文件成功: {}, 大小: {} bytes", key, downloadedFile.getContent().length);
    }

    @Test
    @DisplayName("测试下载文件夹")
    void testDownloadFolder() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        S3DownloadResult result = s3Adapter.downloadFolder(testBucketName, testPrefix);

        assertNotNull(result);
        assertTrue(result.getTotalFileCount() >= 0);
        assertEquals(result.getSuccessCount() + result.getFailureCount(), result.getTotalFileCount());

        log.info("下载文件夹结果: {}", result);
        log.info("成功率: {:.2f}%", result.getSuccessRate());
        log.info("下载速度: {:.2f} MB/s", result.getDownloadSpeedMBps());

        if (result.getFailureCount() > 0) {
            log.warn("部分文件下载失败:");
            result.getErrors().forEach(error -> log.warn("  - {}", error));
        }
    }

    @Test
    @DisplayName("测试下载文件夹到本地目录")
    void testDownloadFolderToDirectory() throws Exception {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        // 创建临时目录
        Path tempDir = Files.createTempDirectory("s3-download-test-");
        log.info("临时目录: {}", tempDir);

        try {
            S3DownloadResult result = s3Adapter.downloadFolderToDirectory(
                    testBucketName, testPrefix, tempDir);

            assertNotNull(result);
            assertTrue(result.getTotalFileCount() >= 0);

            log.info("下载文件夹到本地结果: {}", result);

            // 验证文件已下载
            if (result.getSuccessCount() > 0) {
                assertTrue(Files.list(tempDir).count() > 0, "本地目录应该有文件");
                log.info("本地目录文件数: {}", Files.list(tempDir).count());
            }

        } finally {
            // 清理临时目录
            deleteDirectory(tempDir);
        }
    }

    @Test
    @DisplayName("测试上传和删除文件")
    void testUploadAndDeleteFile() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        String testKey = testPrefix + "test-upload-" + System.currentTimeMillis() + ".txt";
        byte[] testContent = "This is a test file content.".getBytes();

        try {
            // 上传文件
            s3Adapter.uploadFile(testBucketName, testKey, testContent);
            log.info("文件上传成功: {}", testKey);

            // 验证文件存在
            boolean exists = s3Adapter.fileExists(testBucketName, testKey);
            assertTrue(exists, "上传的文件应该存在");

            // 下载并验证内容
            S3File downloadedFile = s3Adapter.downloadFile(testBucketName, testKey);
            assertArrayEquals(testContent, downloadedFile.getContent());

        } finally {
            // 清理：删除测试文件
            try {
                s3Adapter.deleteFile(testBucketName, testKey);
                log.info("测试文件已删除: {}", testKey);
            } catch (Exception e) {
                log.error("删除测试文件失败", e);
            }
        }
    }

    @Test
    @DisplayName("测试异步列出文件")
    void testListFilesAsync() throws Exception {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        List<S3File> files = s3Adapter.listFilesAsync(testBucketName, testPrefix).get();

        assertNotNull(files);
        log.info("异步找到 {} 个文件", files.size());
    }

    @Test
    @DisplayName("测试异步下载文件夹")
    void testDownloadFolderAsync() throws Exception {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        S3DownloadResult result = s3Adapter.downloadFolderAsync(testBucketName, testPrefix).get();

        assertNotNull(result);
        assertTrue(result.getTotalFileCount() >= 0);
        log.info("异步下载文件夹结果: {}", result);
    }

    @Test
    @DisplayName("测试适配器可用性")
    void testIsAvailable() {
        Assumptions.assumeTrue(testBucketName != null && !testBucketName.isEmpty());

        boolean available = s3Adapter.isAvailable();
        assertTrue(available, "S3 适配器应该可用");
    }

    @Test
    @DisplayName("测试提供者名称")
    void testGetProviderName() {
        String providerName = s3Adapter.getProviderName();
        assertNotNull(providerName);
        assertTrue(providerName.contains("AWS S3"));
        log.info("提供者名称: {}", providerName);
    }

    // 辅助方法：递归删除目录
    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .sorted((a, b) -> b.compareTo(a)) // 反向排序，先删除文件再删除目录
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                log.error("删除失败: {}", path, e);
                            }
                        });
            }
        } catch (Exception e) {
            log.error("删除目录失败: {}", directory, e);
        }
    }
}

