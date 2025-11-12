package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

/**
 * 代码热点
 *
 * 标识需要重点关注的代码区域（高复杂度、高耦合等）
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class HotSpot {

    private String location;  // 位置：类名.方法名
    private HotSpotType type;
    private int score;        // 热度分数
    private String reason;    // 原因描述

    /**
     * 热点类型
     */
    public enum HotSpotType {
        HIGH_COMPLEXITY("高复杂度"),
        HIGH_COUPLING("高耦合"),
        LONG_METHOD("长方法"),
        GOD_CLASS("上帝类"),
        FREQUENTLY_CHANGED("频繁变更"),
        PERFORMANCE_CRITICAL("性能关键");

        private final String displayName;

        HotSpotType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public String toString() {
        return String.format("[热点] %s - %s (热度: %d): %s",
            type.getDisplayName(),
            location,
            score,
            reason);
    }
}

