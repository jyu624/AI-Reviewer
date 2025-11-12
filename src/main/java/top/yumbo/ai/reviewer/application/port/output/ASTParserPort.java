package top.yumbo.ai.reviewer.application.port.output;

import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;

/**
 * AST解析端口
 *
 * 定义AST解析的核心能力接口
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
public interface ASTParserPort {

    /**
     * 解析项目并生成代码洞察
     *
     * @param project 项目对象
     * @return 代码洞察结果
     */
    CodeInsight parseProject(Project project);

    /**
     * 检查是否支持指定的项目类型
     *
     * @param projectType 项目类型（如 JAVA, PYTHON等）
     * @return 是否支持
     */
    boolean supports(String projectType);

    /**
     * 获取解析器名称
     */
    String getParserName();
}

