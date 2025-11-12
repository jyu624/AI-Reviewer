package top.yumbo.ai.reviewer.application.port.output;

import top.yumbo.ai.reviewer.domain.model.ast.ClassStructure;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;
import top.yumbo.ai.reviewer.domain.model.ast.ComplexityMetrics;
import top.yumbo.ai.reviewer.domain.model.ast.DesignPattern;

import java.util.List;

/**
 * 代码分析端口
 *
 * 提供代码质量分析、设计模式识别等高级分析能力
 *
 * @author AI-Reviewer Team
 * @version 1.0
 * @since 2025-11-13
 */
public interface CodeAnalysisPort {

    /**
     * 计算复杂度指标
     *
     * @param insight 代码洞察
     * @return 复杂度指标
     */
    ComplexityMetrics calculateComplexityMetrics(CodeInsight insight);

    /**
     * 检测设计模式
     *
     * @param classes 类结构列表
     * @return 检测到的设计模式列表
     */
    List<DesignPattern> detectDesignPatterns(List<ClassStructure> classes);

    /**
     * 识别代码坏味道
     *
     * @param insight 代码洞察
     */
    void detectCodeSmells(CodeInsight insight);

    /**
     * 识别代码热点
     *
     * @param insight 代码洞察
     */
    void identifyHotSpots(CodeInsight insight);
}

