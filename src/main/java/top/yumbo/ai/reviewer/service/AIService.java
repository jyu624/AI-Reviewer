package top.yumbo.ai.reviewer.service;

import top.yumbo.ai.reviewer.exception.AnalysisException;

/**
 * AI服务接口
 */
public interface AIService {

    /**
     * 分析代码/文本
     * @param prompt 分析提示词
     * @return 分析结果
     */
    String analyze(String prompt) throws AnalysisException;

    /**
     * 批量分析
     * @param prompts 提示词列表
     * @return 分析结果列表
     */
    String[] analyzeBatch(String[] prompts) throws AnalysisException;

    /**
     * 获取服务提供商名称
     */
    String getProviderName();

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();
}
