package top.yumbo.ai.reviewer.application.port.input;

import top.yumbo.ai.reviewer.domain.model.AnalysisTask;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ReviewReport;

/**
 * 项目分析用例接口（输入端口）
 * 定义应用层提供的核心业务能力
 */
public interface ProjectAnalysisUseCase {

    /**
     * 分析项目
     * @param project 项目对象
     * @return 分析任务
     */
    AnalysisTask analyzeProject(Project project);

    /**
     * 异步分析项目
     * @param project 项目对象
     * @return 分析任务ID
     */
    String analyzeProjectAsync(Project project);

    /**
     * 获取分析任务状态
     * @param taskId 任务ID
     * @return 分析任务
     */
    AnalysisTask getTaskStatus(String taskId);

    /**
     * 取消分析任务
     * @param taskId 任务ID
     */
    void cancelTask(String taskId);

    /**
     * 获取分析结果
     * @param taskId 任务ID
     * @return 评审报告
     */
    ReviewReport getAnalysisResult(String taskId);
}
