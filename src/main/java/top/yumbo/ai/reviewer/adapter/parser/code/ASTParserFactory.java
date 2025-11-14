package top.yumbo.ai.reviewer.adapter.parser.code;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.port.output.ASTParserPort;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ProjectType;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;

import java.util.ArrayList;
import java.util.List;

/**
 * AST解析器工厂
 *
 * 根据项目类型自动选择合适的解析器
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
@Slf4j
public class ASTParserFactory implements ASTParserPort {

    private final List<ASTParserPort> parsers;

    public ASTParserFactory() {
        this.parsers = new ArrayList<>();

        // 注册所有支持的解析器
        parsers.add(new JavaParserAdapter());
        parsers.add(new PythonParserAdapter());
        parsers.add(new JavaScriptParserAdapter());
        parsers.add(new GoParserAdapter());
        parsers.add(new CppParserAdapter());

        log.info("AST解析器工厂初始化完成，支持 {} 种语言", parsers.size());
    }

    /**
     * 根据项目类型自动选择解析器
     */
    @Override
    public CodeInsight parseProject(Project project) {
        // 查找支持该项目类型的解析器
        ASTParserPort parser = findParser(project.getType().name());

        if (parser == null) {
            log.warn("未找到支持 {} 类型的解析器，尝试通用解析", project.getType());
            throw new UnsupportedOperationException(
                "暂不支持 " + project.getType() + " 类型的AST解析");
        }

        log.info("使用 {} 解析项目: {}", parser.getParserName(), project.getName());
        return parser.parseProject(project);
    }

    /**
     * 查找支持指定类型的解析器
     */
    private ASTParserPort findParser(String projectType) {
        for (ASTParserPort parser : parsers) {
            if (parser.supports(projectType)) {
                return parser;
            }
        }
        return null;
    }

    @Override
    public boolean supports(String projectType) {
        return findParser(projectType) != null;
    }

    @Override
    public String getParserName() {
        return "ASTParserFactory";
    }

    /**
     * 获取所有支持的项目类型
     */
    public List<String> getSupportedTypes() {
        List<String> types = new ArrayList<>();
        for (ASTParserPort parser : parsers) {
            types.add(parser.getParserName());
        }
        return types;
    }

    /**
     * 添加自定义解析器
     */
    public void registerParser(ASTParserPort parser) {
        parsers.add(parser);
        log.info("注册新解析器: {}", parser.getParserName());
    }
}

