package top.yumbo.ai.refactor.domain.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目领域模型
 * 核心业务实体，包含项目的所有信息和业务逻辑
 */
@Data
@Builder
public class Project {

    private final String name;
    private final Path rootPath;
    private final ProjectType type;

    @Builder.Default
    private List<SourceFile> sourceFiles = new ArrayList<>();

    @Builder.Default
    private ProjectMetadata metadata = new ProjectMetadata();

    private String structureTree;

    /**
     * 添加源文件
     */
    public void addSourceFile(SourceFile file) {
        if (file != null && !sourceFiles.contains(file)) {
            sourceFiles.add(file);
            metadata.incrementFileCount();
        }
    }

    /**
     * 获取核心文件列表
     */
    public List<SourceFile> getCoreFiles() {
        return sourceFiles.stream()
                .filter(SourceFile::isCore)
                .toList();
    }

    /**
     * 计算项目总代码行数
     */
    public int getTotalLines() {
        return sourceFiles.stream()
                .mapToInt(SourceFile::getLineCount)
                .sum();
    }

    /**
     * 检查项目是否有效
     */
    public boolean isValid() {
        return name != null && !name.isBlank()
                && rootPath != null
                && type != null;
    }

    /**
     * 获取项目语言
     */
    public String getLanguage() {
        return type.getPrimaryLanguage();
    }
}