package top.yumbo.ai.reviewer.application.port.output;

import top.yumbo.ai.reviewer.domain.model.S3DownloadResult;
import top.yumbo.ai.reviewer.domain.model.S3File;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * S3 存储端口（输出端口）
 * 定义与 AWS S3 交互的接口，遵循六边形架构
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-14
 */
public interface S3StoragePort {

    /**
     * 列出 S3 存储桶中指定前缀下的所有文件
     *
     * @param bucketName S3 存储桶名称
     * @param prefix     文件夹前缀（例如："projects/myapp/"）
     * @return S3 文件列表
     */
    List<S3File> listFiles(String bucketName, String prefix);

    /**
     * 列出 S3 存储桶中指定前缀下的所有文件（异步）
     *
     * @param bucketName S3 存储桶名称
     * @param prefix     文件夹前缀
     * @return 异步返回的 S3 文件列表
     */
    CompletableFuture<List<S3File>> listFilesAsync(String bucketName, String prefix);

    /**
     * 下载单个文件
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件的 S3 键
     * @return S3 文件对象（包含内容）
     */
    S3File downloadFile(String bucketName, String key);

    /**
     * 下载单个文件到本地路径
     *
     * @param bucketName   S3 存储桶名称
     * @param key          文件的 S3 键
     * @param localPath    本地保存路径
     */
    void downloadFileToPath(String bucketName, String key, Path localPath);

    /**
     * 下载文件夹下的所有文件
     *
     * @param bucketName S3 存储桶名称
     * @param prefix     文件夹前缀
     * @return 下载结果（包含所有文件内容）
     */
    S3DownloadResult downloadFolder(String bucketName, String prefix);

    /**
     * 下载文件夹下的所有文件到本地目录
     *
     * @param bucketName       S3 存储桶名称
     * @param prefix           文件夹前缀
     * @param localDirectory   本地目录路径
     * @return 下载结果
     */
    S3DownloadResult downloadFolderToDirectory(String bucketName, String prefix, Path localDirectory);

    /**
     * 异步下载文件夹下的所有文件
     *
     * @param bucketName S3 存储桶名称
     * @param prefix     文件夹前缀
     * @return 异步返回的下载结果
     */
    CompletableFuture<S3DownloadResult> downloadFolderAsync(String bucketName, String prefix);

    /**
     * 异步下载文件夹下的所有文件到本地目录
     *
     * @param bucketName       S3 存储桶名称
     * @param prefix           文件夹前缀
     * @param localDirectory   本地目录路径
     * @return 异步返回的下载结果
     */
    CompletableFuture<S3DownloadResult> downloadFolderToDirectoryAsync(
            String bucketName, String prefix, Path localDirectory);

    /**
     * 批量下载文件
     *
     * @param bucketName S3 存储桶名称
     * @param keys       文件键列表
     * @return 下载的文件列表
     */
    List<S3File> downloadFiles(String bucketName, List<String> keys);

    /**
     * 批量下载文件（异步）
     *
     * @param bucketName S3 存储桶名称
     * @param keys       文件键列表
     * @return 异步返回的文件列表
     */
    CompletableFuture<List<S3File>> downloadFilesAsync(String bucketName, List<String> keys);

    /**
     * 检查文件是否存在
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件键
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String key);

    /**
     * 获取文件元数据（不下载内容）
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件键
     * @return S3 文件对象（不含内容）
     */
    S3File getFileMetadata(String bucketName, String key);

    /**
     * 上传文件
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件键
     * @param content    文件内容
     */
    void uploadFile(String bucketName, String key, byte[] content);

    /**
     * 上传文件从本地路径
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件键
     * @param localPath  本地文件路径
     */
    void uploadFileFromPath(String bucketName, String key, Path localPath);

    /**
     * 删除文件
     *
     * @param bucketName S3 存储桶名称
     * @param key        文件键
     */
    void deleteFile(String bucketName, String key);

    /**
     * 获取提供者名称
     *
     * @return 提供者名称
     */
    String getProviderName();

    /**
     * 检查服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取最大并发数
     *
     * @return 最大并发数
     */
    int getMaxConcurrency();

    /**
     * 关闭资源
     */
    void shutdown();
}

