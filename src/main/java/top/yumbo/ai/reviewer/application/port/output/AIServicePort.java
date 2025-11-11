package top.yumbo.ai.reviewer.application.port.output;

import java.util.concurrent.CompletableFuture;

/**
 * AI服务端口（输出端口）
 * 定义与外部AI服务交互的接口
 */
public interface AIServicePort {

    /**
     * 同步分析
     * @param prompt 提示词
     * @return AI分析结果
     */
    String analyze(String prompt);

    /**
     * 异步分析
     * @param prompt 提示词
     * @return 异步分析结果
     */
    CompletableFuture<String> analyzeAsync(String prompt);

    /**
     * 批量异步分析
     * @param prompts 提示词数组
     * @return 异步分析结果数组
     */
    CompletableFuture<String[]> analyzeBatchAsync(String[] prompts);

    /**
     * 获取服务提供商名称
     */
    String getProviderName();

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();

    /**
     * 获取最大并发数
     */
    int getMaxConcurrency();

    /**
     * 关闭服务并释放资源
     */
    void shutdown();
}

