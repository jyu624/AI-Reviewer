package top.yumbo.ai.reviewer.application.hackathon.service;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.adapter.output.ast.parser.ASTParserFactory;
import top.yumbo.ai.reviewer.application.port.output.ASTParserPort;
import top.yumbo.ai.reviewer.domain.hackathon.model.HackathonScore;
import top.yumbo.ai.reviewer.domain.model.Project;
import top.yumbo.ai.reviewer.domain.model.ReviewReport;
import top.yumbo.ai.reviewer.domain.model.SourceFile;
import top.yumbo.ai.reviewer.domain.model.ast.CodeInsight;
import top.yumbo.ai.reviewer.domain.model.ast.CodeSmell;
import top.yumbo.ai.reviewer.domain.model.ast.ComplexityMetrics;
import top.yumbo.ai.reviewer.domain.model.ast.DesignPattern;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 黑客松评分服务（AST增强版）
 *
 * 负责将核心框架的评审报告转换为黑客松专属的四维度评分
 *
 * v2.0 更新：集成AST解析器，基于实际代码结构进行精准评分
 *
 * @author AI-Reviewer Team
 * @version 2.0
 * @since 2025-11-12
 */
@Slf4j
public class HackathonScoringService {

    // AST解析器工厂
    private final ASTParserPort astParser;

    // 创新技术关键词
    private static final List<String> INNOVATION_KEYWORDS = List.of(
        "AI", "机器学习", "深度学习", "大模型", "区块链", "云原生",
        "微服务", "serverless", "GraphQL", "WebAssembly", "Rust",
        "Kubernetes", "Docker", "React", "Vue3", "Next.js"
    );

    /**
     * 构造函数
     */
    public HackathonScoringService() {
        this.astParser = new ASTParserFactory();
        log.info("黑客松评分服务初始化完成（AST增强版）");
    }

    /**
     * 构造函数（支持依赖注入）
     */
    public HackathonScoringService(ASTParserPort astParser) {
        this.astParser = astParser;
        log.info("黑客松评分服务初始化完成（自定义AST解析器）");
    }

    // README 质量评分正则
    private static final Pattern README_SECTIONS = Pattern.compile(
        "(简介|Introduction|功能|Features|安装|Installation|使用|Usage|API|文档|Documentation)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 计算黑客松综合评分（AST增强版）
     *
     * @param reviewReport 核心评审报告
     * @param project 项目信息
     * @return 黑客松评分
     */
    public HackathonScore calculateScore(ReviewReport reviewReport, Project project) {
        if (reviewReport == null || project == null) {
            throw new IllegalArgumentException("评审报告和项目信息不能为空");
        }

        log.info("开始计算黑客松评分: {}", project.getName());

        // 尝试使用AST解析获取代码洞察
        CodeInsight codeInsight = null;
        try {
            if (astParser.supports(project.getType().name())) {
                log.info("使用AST解析器分析项目: {}", project.getType());
                codeInsight = astParser.parseProject(project);
                log.info("AST解析完成: 类数={}, 方法数={}",
                    codeInsight.getClasses().size(),
                    codeInsight.getStatistics() != null ? codeInsight.getStatistics().getTotalMethods() : 0);
            } else {
                log.info("项目类型 {} 不支持AST解析，使用基础评分", project.getType());
            }
        } catch (Exception e) {
            log.warn("AST解析失败，降级到基础评分: {}", e.getMessage());
            codeInsight = null;
        }

        // 使用AST增强的评分逻辑
        int codeQuality = calculateCodeQualityWithAST(reviewReport, codeInsight);
        int innovation = calculateInnovationWithAST(reviewReport, project, codeInsight);
        int completeness = calculateCompletenessWithAST(reviewReport, project, codeInsight);
        int documentation = calculateDocumentation(project);

        HackathonScore score = HackathonScore.builder()
            .codeQuality(codeQuality)
            .innovation(innovation)
            .completeness(completeness)
            .documentation(documentation)
            .build();

        log.info("评分完成: 代码质量={}, 创新性={}, 完成度={}, 文档={}, 总分={}",
            codeQuality, innovation, completeness, documentation, score.getTotalScore());

        return score;
    }

    /**
     * 计算代码质量分数 (0-100) - AST增强版
     *
     * 评分维度：
     * 1. 基础质量（核心框架评分）40%
     * 2. 复杂度控制 30%
     * 3. 代码坏味道 20%
     * 4. 架构设计 10%
     */
    private int calculateCodeQualityWithAST(ReviewReport reviewReport, CodeInsight codeInsight) {
        // 基础分数（来自核心框架）
        int baseScore = reviewReport.getOverallScore();

        // 如果没有AST分析，直接返回基础分数
        if (codeInsight == null) {
            return baseScore;
        }

        // 1. 基础质量 (40%)
        int baseQualityScore = (int) (baseScore * 0.4);

        // 2. 复杂度控制 (30%)
        int complexityScore = calculateComplexityScore(codeInsight);

        // 3. 代码坏味道 (20%)
        int codeSmellScore = calculateCodeSmellScore(codeInsight);

        // 4. 架构设计 (10%)
        int architectureScore = calculateArchitectureScore(codeInsight);

        int totalScore = baseQualityScore + complexityScore + codeSmellScore + architectureScore;

        log.debug("代码质量评分明细: 基础={}, 复杂度={}, 坏味道={}, 架构={}, 总计={}",
            baseQualityScore, complexityScore, codeSmellScore, architectureScore, totalScore);

        return Math.min(100, totalScore);
    }

    /**
     * 计算复杂度得分 (0-30)
     */
    private int calculateComplexityScore(CodeInsight codeInsight) {
        ComplexityMetrics metrics = codeInsight.getComplexityMetrics();
        if (metrics == null) {
            return 15; // 默认中等分数
        }

        int score = 30; // 满分

        // 平均圈复杂度评分
        double avgComplexity = metrics.getAvgCyclomaticComplexity();
        if (avgComplexity > 15) {
            score -= 15; // 很差
        } else if (avgComplexity > 10) {
            score -= 10; // 较差
        } else if (avgComplexity > 7) {
            score -= 5;  // 中等
        } else if (avgComplexity > 5) {
            score -= 2;  // 良好
        }
        // 否则保持满分（优秀）

        // 高复杂度方法数量扣分
        int highComplexityCount = metrics.getHighComplexityMethodCount();
        int totalMethods = metrics.getTotalMethods();
        if (totalMethods > 0) {
            double highComplexityRatio = (double) highComplexityCount / totalMethods;
            if (highComplexityRatio > 0.3) {
                score -= 10; // 超过30%的方法复杂度高
            } else if (highComplexityRatio > 0.15) {
                score -= 5;  // 超过15%的方法复杂度高
            }
        }

        return Math.max(0, score);
    }

    /**
     * 计算代码坏味道得分 (0-20)
     */
    private int calculateCodeSmellScore(CodeInsight codeInsight) {
        List<CodeSmell> smells = codeInsight.getCodeSmells();
        if (smells == null || smells.isEmpty()) {
            return 20; // 无坏味道，满分
        }

        double score = 20.0;

        // 根据严重程度扣分
        for (CodeSmell smell : smells) {
            switch (smell.getSeverity()) {
                case CRITICAL -> score -= 3;
                case HIGH -> score -= 2;
                case MEDIUM -> score -= 1;
                case LOW -> score -= 0.5;
            }
        }

        return Math.max(0, (int) Math.round(score));
    }

    /**
     * 计算架构设计得分 (0-10)
     */
    private int calculateArchitectureScore(CodeInsight codeInsight) {
        if (codeInsight.getStructure() == null) {
            return 5; // 默认中等分数
        }

        String architecture = codeInsight.getStructure().getArchitectureStyle();

        // 根据架构风格评分
        if (architecture != null) {
            if (architecture.contains("六边形") || architecture.contains("Hexagonal")) {
                return 10; // 六边形架构，满分
            } else if (architecture.contains("分层") || architecture.contains("Layered")) {
                return 8; // 分层架构，良好
            } else if (architecture.contains("微服务") || architecture.contains("Microservice")) {
                return 9; // 微服务架构，优秀
            }
        }

        // 检查设计模式使用
        if (codeInsight.getDesignPatterns() != null &&
            !codeInsight.getDesignPatterns().getPatterns().isEmpty()) {
            return 7; // 使用了设计模式，较好
        }

        return 5; // 默认分数
    }

    /**
     * 计算创新性分数 (0-100) - AST增强版
     *
     * 评估维度：
     * 1. 使用的新技术栈 30%
     * 2. 设计模式创新性 30%
     * 3. AI 分析的创新性评价 25%
     * 4. 项目的独特性 15%
     */
    private int calculateInnovationWithAST(ReviewReport reviewReport, Project project, CodeInsight codeInsight) {
        // 1. 技术栈创新性评分 (0-30)
        int techStackScore = calculateTechStackInnovation(project);
        techStackScore = (int) (techStackScore * 0.75); // 调整为30分制

        // 2. 设计模式创新性 (0-30)
        int designPatternScore = calculateDesignPatternInnovation(codeInsight);

        // 3. AI 评价创新性 (0-25)
        int aiInnovationScore = extractInnovationFromFindings(reviewReport);
        aiInnovationScore = (int) (aiInnovationScore * 0.625); // 调整为25分制

        // 4. 项目独特性 (0-15)
        int uniquenessScore = calculateUniqueness(project);
        uniquenessScore = (int) (uniquenessScore * 0.75); // 调整为15分制

        int totalScore = techStackScore + designPatternScore + aiInnovationScore + uniquenessScore;

        log.debug("创新性评分明细: 技术栈={}, 设计模式={}, AI评价={}, 独特性={}, 总计={}",
            techStackScore, designPatternScore, aiInnovationScore, uniquenessScore, totalScore);

        return Math.min(100, totalScore);
    }

    /**
     * 计算设计模式创新性 (0-30)
     */
    private int calculateDesignPatternInnovation(CodeInsight codeInsight) {
        if (codeInsight == null || codeInsight.getDesignPatterns() == null) {
            return 10; // 默认分数
        }

        List<DesignPattern> patterns = codeInsight.getDesignPatterns().getPatterns();
        if (patterns.isEmpty()) {
            return 5; // 没有使用设计模式
        }

        int score = 10; // 基础分

        // 每种设计模式加分
        for (DesignPattern pattern : patterns) {
            switch (pattern.getType()) {
                case SINGLETON, FACTORY, BUILDER -> score += 2; // 常见模式
                case ADAPTER, DECORATOR, PROXY, FACADE -> score += 3; // 结构型模式
                case STRATEGY, OBSERVER, COMMAND, TEMPLATE_METHOD -> score += 3; // 行为型模式
                case MVC, MVVM, REPOSITORY -> score += 4; // 架构模式
                default -> score += 1;
            }
        }

        // 多种模式组合使用额外加分
        if (patterns.size() >= 3) {
            score += 5;
        }

        return Math.min(30, score);
    }

    /**
     * 计算技术栈创新性
     */
    private int calculateTechStackInnovation(Project project) {
        String projectContent = collectProjectContent(project);

        long matchCount = INNOVATION_KEYWORDS.stream()
            .filter(keyword -> projectContent.toLowerCase().contains(keyword.toLowerCase()))
            .count();

        // 每个创新关键词 5 分，最高 40 分
        return Math.min(40, (int) (matchCount * 5));
    }

    /**
     * 从评审发现中提取创新性评分
     */
    private int extractInnovationFromFindings(ReviewReport reviewReport) {
        List<String> findings = reviewReport.getKeyFindings();
        if (findings == null || findings.isEmpty()) {
            return 20; // 默认分数
        }

        // 检查是否提到创新、新颖、独特等词汇
        String allFindings = String.join(" ", findings).toLowerCase();

        int score = 20; // 基础分

        if (allFindings.contains("创新") || allFindings.contains("innovative")) {
            score += 10;
        }
        if (allFindings.contains("新颖") || allFindings.contains("novel")) {
            score += 5;
        }
        if (allFindings.contains("独特") || allFindings.contains("unique")) {
            score += 5;
        }

        return Math.min(40, score);
    }

    /**
     * 计算项目独特性
     */
    private int calculateUniqueness(Project project) {
        // 基于项目名称和结构的独特性
        int score = 10; // 基础分

        // 如果项目有多种语言混合，加分
        if (project.getSourceFiles().stream()
            .map(SourceFile::getProjectType)
            .distinct()
            .count() > 2) {
            score += 5;
        }

        // 如果代码规模适中（500-3000行），加分
        int totalLines = project.getTotalLines();
        if (totalLines >= 500 && totalLines <= 3000) {
            score += 5;
        }

        return score;
    }

    /**
     * 计算完成度分数 (0-100) - AST增强版
     *
     * 评估维度：
     * 1. 代码结构完整性 40%
     * 2. 功能实现度 30%
     * 3. 测试覆盖率 20%
     * 4. 文档完整性 10%
     */
    private int calculateCompletenessWithAST(ReviewReport reviewReport, Project project, CodeInsight codeInsight) {
        // 1. 代码结构完整性 (0-40)
        int structureScore = calculateStructureCompleteness(codeInsight);

        // 2. 功能实现度 (0-30)
        int functionalityScore = calculateFunctionalityWithAST(project, codeInsight);

        // 3. 测试覆盖率 (0-20)
        int testScore = calculateTestCoverage(project);

        // 4. 文档完整性 (0-10)
        int docScore = (int) (calculateDocumentation(project) * 0.1);

        int totalScore = structureScore + functionalityScore + testScore + docScore;

        log.debug("完成度评分明细: 结构={}, 功能={}, 测试={}, 文档={}, 总计={}",
            structureScore, functionalityScore, testScore, docScore, totalScore);

        return Math.min(100, totalScore);
    }

    /**
     * 计算代码结构完整性 (0-40)
     */
    private int calculateStructureCompleteness(CodeInsight codeInsight) {
        if (codeInsight == null) {
            return 15; // 默认分数
        }

        int score = 0;

        // 类数量评分 (0-15)
        int classCount = codeInsight.getClasses().size();
        if (classCount >= 20) {
            score += 15;
        } else if (classCount >= 10) {
            score += 12;
        } else if (classCount >= 5) {
            score += 9;
        } else if (classCount >= 3) {
            score += 6;
        } else {
            score += 3;
        }

        // 方法数量评分 (0-10)
        if (codeInsight.getStatistics() != null) {
            int methodCount = codeInsight.getStatistics().getTotalMethods();
            if (methodCount >= 50) {
                score += 10;
            } else if (methodCount >= 30) {
                score += 8;
            } else if (methodCount >= 15) {
                score += 6;
            } else if (methodCount >= 5) {
                score += 4;
            }
        }

        // 架构清晰度 (0-10)
        if (codeInsight.getStructure() != null &&
            codeInsight.getStructure().getArchitectureStyle() != null) {
            score += 10; // 有明确的架构风格
        } else {
            score += 5;
        }

        // 接口使用 (0-5)
        if (codeInsight.getInterfaces() != null && !codeInsight.getInterfaces().isEmpty()) {
            score += 5;
        }

        return Math.min(40, score);
    }

    /**
     * 计算功能实现度 (0-30) - AST增强版
     */
    private int calculateFunctionalityWithAST(Project project, CodeInsight codeInsight) {
        int score = 0;

        // 基于文件数量评估 (0-10)
        int fileCount = project.getSourceFiles().size();
        if (fileCount >= 20) score += 10;
        else if (fileCount >= 10) score += 8;
        else if (fileCount >= 5) score += 6;
        else score += 3;

        // 基于代码行数评估 (0-10)
        int totalLines = project.getTotalLines();
        if (totalLines >= 2000) score += 10;
        else if (totalLines >= 1000) score += 8;
        else if (totalLines >= 500) score += 6;
        else if (totalLines >= 200) score += 4;
        else score += 2;

        // 基于AST分析的功能完整性 (0-10)
        if (codeInsight != null) {
            // 有多层架构
            if (codeInsight.getStructure() != null &&
                codeInsight.getStructure().getLayers().size() >= 3) {
                score += 5;
            }

            // 方法平均长度合理（不要太短也不要太长）
            if (codeInsight.getComplexityMetrics() != null) {
                double avgLength = codeInsight.getComplexityMetrics().getAvgMethodLength();
                if (avgLength >= 10 && avgLength <= 50) {
                    score += 5; // 方法长度合理
                } else {
                    score += 2;
                }
            }
        }

        return Math.min(30, score);
    }

    /**
     * 计算功能完整性（已废弃，使用calculateFunctionalityWithAST代替）
     * @deprecated 使用 {@link #calculateFunctionalityWithAST(Project, CodeInsight)} 代替
     */
    @Deprecated
    private int calculateFunctionality(Project project) {
        int score = 0;

        // 基于文件数量评估
        int fileCount = project.getSourceFiles().size();
        if (fileCount >= 5) score += 15;
        if (fileCount >= 10) score += 10;
        if (fileCount >= 20) score += 10;

        // 基于代码行数评估
        int totalLines = project.getTotalLines();
        if (totalLines >= 200) score += 5;
        if (totalLines >= 500) score += 5;
        if (totalLines >= 1000) score += 5;

        return score;
    }

    /**
     * 计算测试覆盖率分数
     */
    private int calculateTestCoverage(Project project) {
        long testFileCount = project.getSourceFiles().stream()
            .filter(file -> file.getPath().toString().toLowerCase().contains("test"))
            .count();

        long totalFiles = project.getSourceFiles().size();
        if (totalFiles == 0) return 0;

        double testRatio = (double) testFileCount / totalFiles;

        // 测试文件占比 20% 以上给满分
        return (int) Math.min(20, testRatio * 100);
    }

    /**
     * 计算文档质量分数 (0-100)
     *
     * 评估维度：
     * 1. README 完善度 (60%)
     * 2. 代码注释率 (30%)
     * 3. API 文档 (10%)
     */
    private int calculateDocumentation(Project project) {
        // 1. README 质量 (0-60)
        int readmeScore = calculateReadmeQuality(project);

        // 2. 代码注释率 (0-30)
        int commentScore = calculateCommentRatio(project);

        // 3. API 文档 (0-10)
        int apiDocScore = hasApiDoc(project) ? 10 : 0;

        return Math.min(100, readmeScore + commentScore + apiDocScore);
    }

    /**
     * 计算 README 质量
     */
    private int calculateReadmeQuality(Project project) {
        SourceFile readme = project.getSourceFiles().stream()
            .filter(file -> file.getPath().toString().toLowerCase().contains("readme"))
            .findFirst()
            .orElse(null);

        if (readme == null || readme.getContent() == null) {
            return 0; // 没有 README
        }

        String content = readme.getContent();
        int score = 20; // 有 README 基础分

        // 检查各个章节
        java.util.regex.Matcher matcher = README_SECTIONS.matcher(content);
        int sectionCount = 0;
        while (matcher.find()) {
            sectionCount++;
        }

        // 每个标准章节 8 分
        score += Math.min(40, sectionCount * 8);

        return Math.min(60, score);
    }

    /**
     * 计算代码注释率
     */
    private int calculateCommentRatio(Project project) {
        // 简化实现：假设有注释的给 20 分
        // 实际应该统计注释行数占比
        boolean hasComments = project.getSourceFiles().stream()
            .anyMatch(file -> file.getContent() != null &&
                            (file.getContent().contains("//") ||
                             file.getContent().contains("/*")));

        return hasComments ? 20 : 10;
    }

    /**
     * 检查是否有 API 文档
     */
    private boolean hasApiDoc(Project project) {
        return project.getSourceFiles().stream()
            .anyMatch(file -> file.getPath().toString().toLowerCase().contains("api") ||
                            file.getPath().toString().toLowerCase().contains("swagger") ||
                            file.getPath().toString().toLowerCase().contains("openapi"));
    }

    /**
     * 收集项目内容（用于关键词匹配）
     */
    private String collectProjectContent(Project project) {
        StringBuilder content = new StringBuilder();

        // 添加项目名称
        content.append(project.getName()).append(" ");

        // 添加所有源文件内容
        project.getSourceFiles().forEach(file -> {
            if (file.getContent() != null) {
                content.append(file.getContent()).append(" ");
            }
        });

        return content.toString();
    }

    /**
     * 获取评分详细说明
     *
     * @param score 黑客松评分
     * @return 详细说明
     */
    public String getScoreExplanation(HackathonScore score) {
        return String.format(
            "【黑客松评分详情】\n\n" +
            "总分: %d / 100 (%s)\n" +
            "%s\n\n" +
            "【各维度分析】\n" +
            "1. 代码质量 (40%%): %d 分\n" +
            "   - 代码规范性、架构设计、测试覆盖率等\n\n" +
            "2. 创新性 (30%%): %d 分\n" +
            "   - 技术栈创新、解决方案独特性、功能创新点\n\n" +
            "3. 完成度 (20%%): %d 分\n" +
            "   - 核心功能完整性、代码量、测试覆盖\n\n" +
            "4. 文档质量 (10%%): %d 分\n" +
            "   - README 完善度、代码注释、API 文档\n\n" +
            "【总体评价】\n" +
            "最强项: %s\n" +
            "待提升: %s",
            score.calculateTotalScore(),
            score.getGrade(),
            score.getGradeDescription(),
            score.getCodeQuality(),
            score.getInnovation(),
            score.getCompleteness(),
            score.getDocumentation(),
            score.getStrongestDimension(),
            score.getWeakestDimension()
        );
    }
}


