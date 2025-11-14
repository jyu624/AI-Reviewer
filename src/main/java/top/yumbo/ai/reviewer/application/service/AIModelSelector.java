package top.yumbo.ai.reviewer.application.service;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.AIServicePort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI模型选择器
 * 负责选择最合适的AI模型并提供故障转移和负载均衡
 */
@Slf4j
public class AIModelSelector {

    private final Map<String, AIServicePort> models = new ConcurrentHashMap<>();
    private final List<String> fallbackOrder = new ArrayList<>();
    private String primaryModel;
    private String loadBalancingStrategy = "round-robin";
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    /**
     * 注册AI模型
     */
    public void registerModel(String name, AIServicePort model) {
        models.put(name, model);
        log.info("AI模型已注册: {}", name);
    }

    /**
     * 设置主要模型
     */
    public void setPrimaryModel(String primaryModel) {
        this.primaryModel = primaryModel;
        log.info("主要AI模型设置为: {}", primaryModel);
    }

    /**
     * 设置故障转移顺序
     */
    public void setFallbackOrder(List<String> order) {
        this.fallbackOrder.clear();
        this.fallbackOrder.addAll(order);
        log.info("故障转移顺序设置为: {}", order);
    }

    /**
     * 设置成本优化
     */
    public void setCostOptimization(boolean enabled) {
        log.info("成本优化{}", enabled ? "已启用" : "已禁用");
    }

    /**
     * 设置负载均衡策略
     */
    public void setLoadBalancingStrategy(String strategy) {
        this.loadBalancingStrategy = strategy;
        log.info("负载均衡策略设置为: {}", strategy);
    }

    /**
     * 选择模型
     */
    public AIServicePort selectModel() {
        return selectModel(null);
    }

    /**
     * 根据任务类型选择模型
     */
    public AIServicePort selectModel(String taskType) {
        // 1. 如果指定了任务类型，使用最佳模型
        if (taskType != null) {
            AIServicePort bestModel = getBestModelForTask(taskType);
            if (bestModel != null && bestModel.isAvailable()) {
                log.info("为任务类型 {} 选择模型: {}", taskType, bestModel.getProviderName());
                return bestModel;
            }
        }

        // 2. 使用主要模型
        if (primaryModel != null && models.containsKey(primaryModel)) {
            AIServicePort model = models.get(primaryModel);
            if (model.isAvailable()) {
                log.info("使用主要模型: {}", model.getProviderName());
                return model;
            }
            log.warn("主要模型不可用: {}", primaryModel);
        }

        // 3. 尝试故障转移
        for (String modelName : fallbackOrder) {
            if (models.containsKey(modelName)) {
                AIServicePort model = models.get(modelName);
                if (model.isAvailable()) {
                    log.info("故障转移到模型: {}", model.getProviderName());
                    return model;
                }
            }
        }

        // 4. 最后尝试任何可用的模型
        for (AIServicePort model : models.values()) {
            if (model.isAvailable()) {
                log.warn("使用备用模型: {}", model.getProviderName());
                return model;
            }
        }

        throw new RuntimeException("没有可用的AI模型");
    }

    /**
     * 根据负载均衡策略选择模型
     */
    public AIServicePort selectModelWithLoadBalancing() {
        List<AIServicePort> available = getAvailableModels();

        if (available.isEmpty()) {
            throw new RuntimeException("没有可用的AI模型");
        }

        return switch (loadBalancingStrategy) {
            case "round-robin" -> {
                int index = roundRobinCounter.getAndIncrement() % available.size();
                yield available.get(index);
            }
            case "random" -> {
                Random random = new Random();
                yield available.get(random.nextInt(available.size()));
            }
            case "least-cost" ->
                // 选择成本最低的模型（简化实现）
                    available.stream()
                            .min(Comparator.comparing(m -> estimateModelCost(m.getProviderName())))
                            .orElse(available.get(0));
            default -> available.get(0);
        };
    }

    /**
     * 获取所有可用的模型
     */
    public List<AIServicePort> getAvailableModels() {
        List<AIServicePort> available = new ArrayList<>();
        for (AIServicePort model : models.values()) {
            if (model.isAvailable()) {
                available.add(model);
            }
        }
        return available;
    }

    /**
     * 根据任务类型获取最佳模型
     */
    public AIServicePort getBestModelForTask(String taskType) {
        // 根据任务类型推荐模型
        return switch (taskType) {
            case "large-context" ->
                // Claude适合处理大上下文
                    models.get("claude");
            case "code-generation" ->
                // GPT-4适合代码生成
                    models.get("openai");
            case "quick-analysis" ->
                // DeepSeek或Gemini适合快速分析
                    models.getOrDefault("gemini", models.get("deepseek"));
            case "cost-sensitive" ->
                // Gemini免费或DeepSeek成本较低
                    models.getOrDefault("gemini", models.get("deepseek"));
            default -> selectModel();
        };
    }

    /**
     * 估算模型成本（简化实现）
     */
    public double estimateModelCost(String modelName) {
        // 返回相对成本分数（0-100）
        return switch (modelName.toLowerCase()) {
            case "gemini" -> 0.0; // 免费
            case "deepseek" -> 10.0; // 很便宜
            case "openai" -> 50.0; // 中等
            case "claude" -> 70.0; // 较贵
            default -> 50.0;
        };
    }

    /**
     * 估算任务成本
     */
    public double estimateCost(String modelName, int estimatedTokens) {
        AIServicePort model = models.get(modelName);
        if (model == null) {
            return 0.0;
        }

        // 不同模型的成本估算逻辑不同
        // 这里返回一个粗略估算
        double baseCost = estimateModelCost(modelName);
        return baseCost * estimatedTokens / 1000.0;
    }

    /**
     * 获取模型统计信息
     */
    public Map<String, Object> getModelStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_models", models.size());
        stats.put("available_models", getAvailableModels().size());
        stats.put("primary_model", primaryModel);
        stats.put("fallback_order", fallbackOrder);
        stats.put("load_balancing_strategy", loadBalancingStrategy);

        Map<String, Boolean> availability = new HashMap<>();
        for (Map.Entry<String, AIServicePort> entry : models.entrySet()) {
            availability.put(entry.getKey(), entry.getValue().isAvailable());
        }
        stats.put("model_availability", availability);

        return stats;
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        return !getAvailableModels().isEmpty();
    }

    /**
     * 获取已注册的模型名称
     */
    public Set<String> getRegisteredModels() {
        return models.keySet();
    }

    /**
     * 获取指定名称的模型
     */
    public AIServicePort getModel(String name) {
        return models.get(name);
    }
}

