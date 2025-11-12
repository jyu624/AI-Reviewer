package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 设计模式
 *
 * 表示检测到的设计模式实例
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class DesignPattern {

    private PatternType type;
    private String name;

    @Builder.Default
    private List<String> instances = new ArrayList<>();  // 应用该模式的类

    @Builder.Default
    private double confidence = 0.0;  // 识别置信度 (0.0-1.0)

    private String description;

    /**
     * 设计模式类型
     */
    public enum PatternType {
        // 创建型模式
        SINGLETON("单例模式"),
        FACTORY("工厂模式"),
        ABSTRACT_FACTORY("抽象工厂模式"),
        BUILDER("建造者模式"),
        PROTOTYPE("原型模式"),

        // 结构型模式
        ADAPTER("适配器模式"),
        BRIDGE("桥接模式"),
        COMPOSITE("组合模式"),
        DECORATOR("装饰器模式"),
        FACADE("外观模式"),
        FLYWEIGHT("享元模式"),
        PROXY("代理模式"),

        // 行为型模式
        CHAIN_OF_RESPONSIBILITY("责任链模式"),
        COMMAND("命令模式"),
        INTERPRETER("解释器模式"),
        ITERATOR("迭代器模式"),
        MEDIATOR("中介者模式"),
        MEMENTO("备忘录模式"),
        OBSERVER("观察者模式"),
        STATE("状态模式"),
        STRATEGY("策略模式"),
        TEMPLATE_METHOD("模板方法模式"),
        VISITOR("访问者模式"),

        // 架构模式
        MVC("MVC模式"),
        MVVM("MVVM模式"),
        REPOSITORY("仓储模式"),
        SERVICE("服务模式"),
        DAO("DAO模式"),

        UNKNOWN("未知模式");

        private final String displayName;

        PatternType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 添加实例
     */
    public void addInstance(String className) {
        this.instances.add(className);
    }

    /**
     * 获取实例数量
     */
    public int getInstanceCount() {
        return instances.size();
    }

    @Override
    public String toString() {
        return String.format("%s: %d处 (置信度: %.0f%%)",
            name != null ? name : type.getDisplayName(),
            instances.size(),
            confidence * 100);
    }
}

