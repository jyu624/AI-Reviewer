package top.yumbo.ai.reviewer.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分析进度
 */
@Data
public class AnalysisProgress {

    private int totalSteps = 0;
    private int completedSteps = 0;

    private String currentPhase = "初始化";
    private String currentTask = "";

    private List<String> completedPhases = new ArrayList<>();

    /**
     * 设置总步骤数
     */
    public void setTotalSteps(int total) {
        this.totalSteps = total;
    }

    /**
     * 增加完成步骤
     */
    public void incrementCompleted() {
        if (totalSteps > 0 && completedSteps < totalSteps) {
            completedSteps++;
        }
    }

    /**
     * 更新当前阶段
     */
    public void updatePhase(String phase) {
        if (currentPhase != null && !currentPhase.equals(phase)) {
            completedPhases.add(currentPhase);
        }
        this.currentPhase = phase;
    }

    /**
     * 更新当前任务
     */
    public void updateTask(String task) {
        this.currentTask = task;
    }

    /**
     * 获取完成百分比
     */
    public double getPercentage() {
        if (totalSteps == 0) return 0.0;
        return (double) completedSteps / totalSteps * 100.0;
    }

    /**
     * 标记完成
     */
    public void complete() {
        this.completedSteps = this.totalSteps;
        if (currentPhase != null) {
            completedPhases.add(currentPhase);
        }
        this.currentPhase = "已完成";
    }

    /**
     * 是否完成
     */
    public boolean isCompleted() {
        if (totalSteps == 0) return true;
        return completedSteps >= totalSteps;
    }
}

