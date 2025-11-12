package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口结构
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class InterfaceStructure {

    private String interfaceName;
    private String packageName;
    private String fullQualifiedName;

    @Builder.Default
    private List<String> extendedInterfaces = new ArrayList<>();

    @Builder.Default
    private List<MethodInfo> methods = new ArrayList<>();

    @Builder.Default
    private List<String> annotations = new ArrayList<>();

    @Builder.Default
    private int methodCount = 0;

    /**
     * 添加方法
     */
    public void addMethod(MethodInfo method) {
        this.methods.add(method);
        this.methodCount = this.methods.size();
    }

    /**
     * 添加扩展的接口
     */
    public void addExtendedInterface(String interfaceName) {
        this.extendedInterfaces.add(interfaceName);
    }
}

