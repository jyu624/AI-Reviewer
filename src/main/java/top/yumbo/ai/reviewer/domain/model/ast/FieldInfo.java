package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

/**
 * 字段信息
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class FieldInfo {

    private String fieldName;
    private String type;

    private ClassStructure.AccessModifier accessModifier;

    @Builder.Default
    private boolean isStatic = false;

    @Builder.Default
    private boolean isFinal = false;

    @Builder.Default
    private boolean isVolatile = false;

    @Builder.Default
    private boolean isTransient = false;

    private String initializer;  // 初始化表达式

    /**
     * 获取字段声明字符串
     */
    public String getDeclaration() {
        StringBuilder sb = new StringBuilder();

        if (accessModifier != null) {
            sb.append(accessModifier.name().toLowerCase()).append(" ");
        }

        if (isStatic) {
            sb.append("static ");
        }

        if (isFinal) {
            sb.append("final ");
        }

        if (isVolatile) {
            sb.append("volatile ");
        }

        if (isTransient) {
            sb.append("transient ");
        }

        sb.append(type).append(" ").append(fieldName);

        if (initializer != null) {
            sb.append(" = ").append(initializer);
        }

        return sb.toString();
    }
}

