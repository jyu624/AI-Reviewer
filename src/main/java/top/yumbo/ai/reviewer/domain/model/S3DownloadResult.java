package top.yumbo.ai.reviewer.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * S3 下载结果领域模型
 * 表示从 S3 下载文件夹的结果
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@Builder
public class S3DownloadResult {

    /**
     * S3 存储桶名称
     */
    private final String bucketName;

    /**
     * 下载的文件夹前缀
     */
    private final String folderPrefix;

    /**
     * 下载的文件列表
     */
    private final List<S3File> files;

    /**
     * 下载开始时间
     */
    private final Instant startTime;

    /**
     * 下载结束时间
     */
    private final Instant endTime;

    /**
     * 总文件数
     */
    private final int totalFileCount;

    /**
     * 成功下载的文件数
     */
    private final int successCount;

    /**
     * 失败的文件数
     */
    private final int failureCount;

    /**
     * 总下载大小（字节）
     */
    private final long totalSizeInBytes;

    /**
     * 错误信息列表
     */
    private final List<String> errors;

    /**
     * 是否全部成功
     */
    public boolean isSuccess() {
        return failureCount == 0 && successCount == totalFileCount;
    }

    /**
     * 获取耗时（毫秒）
     */
    public long getDurationMillis() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }

    /**
     * 获取耗时（秒）
     */
    public double getDurationSeconds() {
        return getDurationMillis() / 1000.0;
    }

    /**
     * 获取成功率（百分比）
     */
    public double getSuccessRate() {
        if (totalFileCount == 0) {
            return 0.0;
        }
        return (successCount * 100.0) / totalFileCount;
    }

    /**
     * 获取总下载大小（MB）
     */
    public double getTotalSizeInMB() {
        return totalSizeInBytes / (1024.0 * 1024.0);
    }

    /**
     * 获取下载速度（MB/s）
     */
    public double getDownloadSpeedMBps() {
        double durationSeconds = getDurationSeconds();
        if (durationSeconds == 0) {
            return 0.0;
        }
        return getTotalSizeInMB() / durationSeconds;
    }

    @Override
    public String toString() {
        return String.format(
                "S3DownloadResult{bucket='%s', prefix='%s', total=%d, success=%d, failed=%d, " +
                "size=%.2f MB, duration=%.2f s, speed=%.2f MB/s}",
                bucketName, folderPrefix, totalFileCount, successCount, failureCount,
                getTotalSizeInMB(), getDurationSeconds(), getDownloadSpeedMBps()
        );
    }
}

