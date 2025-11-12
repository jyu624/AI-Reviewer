package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

/**
 * 代码坏味道
 *
 * 识别代码中的不良实践和潜在问题
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class CodeSmell {

    private SmellType type;
    private Severity severity;
    private String location;      // 位置：类名.方法名
    private String message;       // 详细描述
    private String suggestion;    // 改进建议

    /**
     * 代码坏味道类型
     */
    public enum SmellType {
        LONG_METHOD("长方法"),
        LONG_CLASS("超大类"),
        HIGH_COMPLEXITY("高复杂度"),
        TOO_MANY_PARAMETERS("参数过多"),
        DUPLICATE_CODE("重复代码"),
        DEEP_NESTING("深层嵌套"),
        MAGIC_NUMBER("魔法数字"),
        DEAD_CODE("死代码"),
        GOD_CLASS("上帝类"),
        FEATURE_ENVY("依恋情结"),
        DATA_CLUMPS("数据泥团"),
        PRIMITIVE_OBSESSION("基本类型偏执"),
        SWITCH_STATEMENTS("过度使用switch"),
        LAZY_CLASS("冗余类"),
        SPECULATIVE_GENERALITY("夸夸其谈未来性"),
        MESSAGE_CHAINS("过长的消息链");

        private final String displayName;

        SmellType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 严重程度
     */
    public enum Severity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");

        private final String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s): %s",
            severity.getDisplayName(),
            type.getDisplayName(),
            location,
            message);
    }
}

