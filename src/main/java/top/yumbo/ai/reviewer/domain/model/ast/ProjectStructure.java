package top.yumbo.ai.reviewer.domain.model.ast;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目结构
 *
 * 表示项目的包/模块组织结构
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Data
@Builder
public class ProjectStructure {

    private String rootPackage;

    @Builder.Default
    private Map<String, PackageInfo> packages = new HashMap<>();

    @Builder.Default
    private List<String> layers = new ArrayList<>();  // 检测到的架构层次

    private String architectureStyle;  // 架构风格：分层、六边形、微服务等

    /**
     * 包信息
     */
    @Data
    @Builder
    public static class PackageInfo {
        private String name;

        @Builder.Default
        private int classCount = 0;

        @Builder.Default
        private int interfaceCount = 0;

        private String purpose;  // 包的用途描述

        @Builder.Default
        private List<String> subPackages = new ArrayList<>();
    }

    /**
     * 添加包信息
     */
    public void addPackage(String packageName, PackageInfo info) {
        this.packages.put(packageName, info);
    }

    /**
     * 生成树形结构字符串
     */
    public String toTreeString() {
        if (rootPackage == null) {
            return "项目结构未知";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(rootPackage).append("\n");

        // 按包名排序
        packages.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String pkg = entry.getKey();
                PackageInfo info = entry.getValue();

                // 计算缩进层级
                int level = pkg.split("\\.").length - rootPackage.split("\\.").length;
                String indent = "  ".repeat(Math.max(0, level));

                sb.append(indent).append("├── ")
                  .append(pkg.substring(pkg.lastIndexOf('.') + 1))
                  .append(" (").append(info.getClassCount()).append(" classes)");

                if (info.getPurpose() != null) {
                    sb.append(" - ").append(info.getPurpose());
                }

                sb.append("\n");
            });

        return sb.toString();
    }

    /**
     * 检测架构风格
     */
    public String detectArchitectureStyle() {
        // 简单的启发式检测
        List<String> packageNames = new ArrayList<>(packages.keySet());

        boolean hasController = packageNames.stream().anyMatch(p -> p.contains("controller"));
        boolean hasService = packageNames.stream().anyMatch(p -> p.contains("service"));
        boolean hasRepository = packageNames.stream().anyMatch(p -> p.contains("repository") || p.contains("dao"));
        boolean hasModel = packageNames.stream().anyMatch(p -> p.contains("model") || p.contains("entity"));

        boolean hasAdapter = packageNames.stream().anyMatch(p -> p.contains("adapter"));
        boolean hasPort = packageNames.stream().anyMatch(p -> p.contains("port"));
        boolean hasDomain = packageNames.stream().anyMatch(p -> p.contains("domain"));

        if (hasAdapter && hasPort && hasDomain) {
            this.architectureStyle = "六边形架构 (Hexagonal Architecture)";
            this.layers = List.of("Domain", "Port", "Adapter");
        } else if (hasController && hasService && hasRepository) {
            this.architectureStyle = "分层架构 (Layered Architecture)";
            this.layers = List.of("Controller", "Service", "Repository", "Model");
        } else if (hasModel) {
            this.architectureStyle = "简单分层";
            this.layers = List.of("Model");
        } else {
            this.architectureStyle = "未识别的架构风格";
        }

        return this.architectureStyle;
    }
}

