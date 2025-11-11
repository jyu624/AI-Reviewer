package top.yumbo.ai.refactor.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析任务领域模型
 * 代表一次完整的项目分析任务
 */
@Data
@Builder
public class AnalysisTask {

    private final String taskId;
    private final Project project;

    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private AnalysisConfiguration configuration;
    private AnalysisProgress progress;

    private String errorMessage;
    private Exception exception;

    /**
     * 开始任务
     */
    public void start() {
        if (status == AnalysisStatus.PENDING) {
            this.status = AnalysisStatus.RUNNING;
            this.startedAt = LocalDateTime.now();
            this.progress = new AnalysisProgress();
        }
    }

    /**
     * 完成任务
     */
    public void complete() {
        if (status == AnalysisStatus.RUNNING) {
            this.status = AnalysisStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            if (progress != null) {
                progress.complete();
            }
        }
    }

    /**
     * 任务失败
     */
    public void fail(String errorMessage, Exception exception) {
        this.status = AnalysisStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    /**
     * 取消任务
     */
    public void cancel() {
        if (status == AnalysisStatus.RUNNING || status == AnalysisStatus.PENDING) {
            this.status = AnalysisStatus.CANCELLED;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return status == AnalysisStatus.RUNNING;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return status == AnalysisStatus.COMPLETED;
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return status == AnalysisStatus.FAILED;
    }

    /**
     * 获取耗时（毫秒）
     */
    public long getDurationMillis() {
        if (startedAt == null) return 0;
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMillis();
    }

    /**
     * 分析状态枚举
     */
    public enum AnalysisStatus {
        PENDING,      // 待处理
        RUNNING,      // 运行中
        COMPLETED,    // 已完成
        FAILED,       // 失败
        CANCELLED     // 已取消
    }
}