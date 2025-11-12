package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 设计模式集合
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class DesignPatterns {

    @Builder.Default
    private List<DesignPattern> patterns = new ArrayList<>();

    /**
     * 添加设计模式
     */
    public void addPattern(DesignPattern pattern) {
        this.patterns.add(pattern);
    }

    /**
     * 获取指定类型的设计模式
     */
    public List<DesignPattern> getPatternsByType(DesignPattern.PatternType type) {
        return patterns.stream()
            .filter(p -> p.getType() == type)
            .toList();
    }

    /**
     * 检查是否包含指定类型的设计模式
     */
    public boolean hasPattern(DesignPattern.PatternType type) {
        return patterns.stream().anyMatch(p -> p.getType() == type);
    }

    /**
     * 获取设计模式总数
     */
    public int getTotalCount() {
        return patterns.size();
    }
}

