package top.yumbo.ai.reviewer.adapter.output.ast.parser;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.ASTParserPort;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;

/**
 * AST解析器抽象基类
 *
 * 提供通用的解析逻辑和模板方法
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
public abstract class AbstractASTParser implements ASTParserPort {

    @Override
    public CodeInsight parseProject(Project project) {
        log.info("开始使用 {} 解析项目: {}", getParserName(), project.getName());

        long startTime = System.currentTimeMillis();

        try {
            // 模板方法：由子类实现具体的解析逻辑
            CodeInsight insight = doParse(project);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("项目解析完成: {} (耗时: {}ms, 类数: {}, 方法数: {})",
                project.getName(),
                elapsed,
                insight.getClasses().size(),
                insight.getStatistics() != null ? insight.getStatistics().getTotalMethods() : 0
            );

            return insight;

        } catch (Exception e) {
            log.error("项目解析失败: {}", project.getName(), e);
            throw new RuntimeException("AST解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 具体的解析逻辑，由子类实现
     */
    protected abstract CodeInsight doParse(Project project);

    /**
     * 验证项目是否可以解析
     */
    protected void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }

        if (!project.isValid()) {
            throw new IllegalArgumentException("项目信息无效");
        }

        if (project.getSourceFiles().isEmpty()) {
            throw new IllegalArgumentException("项目没有源文件");
        }
    }
}

