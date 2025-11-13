package top.yumbo.ai.reviewer.application.service;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.S3StoragePort;
import top.yumbo.ai.reviewer.domain.model.S3DownloadResult;
import top.yumbo.ai.reviewer.domain.model.S3File;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * S3 存储服务
 * 应用层服务，编排 S3 存储操作的业务逻辑
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
public class S3StorageService {

    private final S3StoragePort s3StoragePort;

    public S3StorageService(S3StoragePort s3StoragePort) {
        this.s3StoragePort = s3StoragePort;
    }

    /**
     * 下载项目代码到本地进行分析
     *
     * @param bucketName     S3 存储桶名称
     * @param projectPrefix  项目在 S3 中的前缀
     * @param localDirectory 本地目录
     * @return 下载结果
     */
    public S3DownloadResult downloadProjectForReview(String bucketName, String projectPrefix, Path localDirectory) {
        log.info("开始下载项目进行审查 - Bucket: {}, Prefix: {}, LocalDir: {}",
                bucketName, projectPrefix, localDirectory);

        S3DownloadResult result = s3StoragePort.downloadFolderToDirectory(bucketName, projectPrefix, localDirectory);

        if (result.isSuccess()) {
            log.info("项目下载成功 - 共 {} 个文件, 大小: {:.2f} MB, 耗时: {:.2f} 秒",
                    result.getSuccessCount(), result.getTotalSizeInMB(), result.getDurationSeconds());
        } else {
            log.warn("项目下载部分失败 - 成功: {}, 失败: {}, 错误: {}",
                    result.getSuccessCount(), result.getFailureCount(), result.getErrors());
        }

        return result;
    }

    /**
     * 下载项目代码到内存进行分析
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 下载结果
     */
    public S3DownloadResult downloadProjectToMemory(String bucketName, String projectPrefix) {
        log.info("开始下载项目到内存 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);

        S3DownloadResult result = s3StoragePort.downloadFolder(bucketName, projectPrefix);

        if (result.isSuccess()) {
            log.info("项目下载到内存成功 - 共 {} 个文件, 大小: {:.2f} MB",
                    result.getSuccessCount(), result.getTotalSizeInMB());
        } else {
            log.warn("项目下载到内存部分失败 - 成功: {}, 失败: {}",
                    result.getSuccessCount(), result.getFailureCount());
        }

        return result;
    }

    /**
     * 列出项目中的源代码文件
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 源代码文件列表
     */
    public List<S3File> listSourceCodeFiles(String bucketName, String projectPrefix) {
        log.info("列出源代码文件 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);

        List<S3File> allFiles = s3StoragePort.listFiles(bucketName, projectPrefix);

        // 过滤出源代码文件
        List<S3File> sourceFiles = allFiles.stream()
                .filter(S3File::isSourceCode)
                .collect(Collectors.toList());

        log.info("找到 {} 个源代码文件（总共 {} 个文件）", sourceFiles.size(), allFiles.size());
        return sourceFiles;
    }

    /**
     * 列出项目中的配置文件
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 配置文件列表
     */
    public List<S3File> listConfigFiles(String bucketName, String projectPrefix) {
        log.info("列出配置文件 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);

        List<S3File> allFiles = s3StoragePort.listFiles(bucketName, projectPrefix);

        // 过滤出配置文件
        List<S3File> configFiles = allFiles.stream()
                .filter(S3File::isConfigFile)
                .collect(Collectors.toList());

        log.info("找到 {} 个配置文件（总共 {} 个文件）", configFiles.size(), allFiles.size());
        return configFiles;
    }

    /**
     * 批量下载源代码文件
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 下载的源代码文件列表
     */
    public List<S3File> downloadSourceCodeFiles(String bucketName, String projectPrefix) {
        log.info("批量下载源代码文件 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);

        // 先列出所有源代码文件
        List<S3File> sourceFiles = listSourceCodeFiles(bucketName, projectPrefix);

        // 提取文件键
        List<String> keys = sourceFiles.stream()
                .map(S3File::getKey)
                .collect(Collectors.toList());

        // 批量下载
        return s3StoragePort.downloadFiles(bucketName, keys);
    }

    /**
     * 异步下载项目进行审查
     *
     * @param bucketName     S3 存储桶名称
     * @param projectPrefix  项目在 S3 中的前缀
     * @param localDirectory 本地目录
     * @return 异步下载结果
     */
    public CompletableFuture<S3DownloadResult> downloadProjectForReviewAsync(
            String bucketName, String projectPrefix, Path localDirectory) {
        log.info("开始异步下载项目进行审查 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);
        return s3StoragePort.downloadFolderToDirectoryAsync(bucketName, projectPrefix, localDirectory);
    }

    /**
     * 获取项目统计信息
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 项目统计信息字符串
     */
    public String getProjectStatistics(String bucketName, String projectPrefix) {
        log.info("获取项目统计信息 - Bucket: {}, Prefix: {}", bucketName, projectPrefix);

        List<S3File> allFiles = s3StoragePort.listFiles(bucketName, projectPrefix);

        long totalSize = allFiles.stream().mapToLong(S3File::getSizeInBytes).sum();
        long sourceCodeCount = allFiles.stream().filter(S3File::isSourceCode).count();
        long configFileCount = allFiles.stream().filter(S3File::isConfigFile).count();

        return String.format(
                "项目统计 [Bucket: %s, Prefix: %s]\n" +
                "总文件数: %d\n" +
                "源代码文件: %d\n" +
                "配置文件: %d\n" +
                "总大小: %.2f MB",
                bucketName, projectPrefix,
                allFiles.size(),
                sourceCodeCount,
                configFileCount,
                totalSize / (1024.0 * 1024.0)
        );
    }

    /**
     * 检查项目是否存在
     *
     * @param bucketName    S3 存储桶名称
     * @param projectPrefix 项目在 S3 中的前缀
     * @return 是否存在
     */
    public boolean projectExists(String bucketName, String projectPrefix) {
        try {
            List<S3File> files = s3StoragePort.listFiles(bucketName, projectPrefix);
            return !files.isEmpty();
        } catch (Exception e) {
            log.error("检查项目是否存在失败", e);
            return false;
        }
    }

    /**
     * 上传审查报告到 S3
     *
     * @param bucketName S3 存储桶名称
     * @param reportKey  报告的 S3 键
     * @param content    报告内容
     */
    public void uploadReviewReport(String bucketName, String reportKey, byte[] content) {
        log.info("上传审查报告到 S3 - Bucket: {}, Key: {}, Size: {} bytes",
                bucketName, reportKey, content.length);

        s3StoragePort.uploadFile(bucketName, reportKey, content);

        log.info("审查报告上传成功 - Key: {}", reportKey);
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        log.info("关闭 S3 存储服务");
        s3StoragePort.shutdown();
    }
}

