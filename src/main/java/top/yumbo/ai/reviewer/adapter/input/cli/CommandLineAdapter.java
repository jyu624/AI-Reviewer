package top.yumbo.ai.reviewer.adapter.input.cli;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.adapter.output.ai.DeepSeekAIAdapter;
import top.yumbo.ai.reviewer.adapter.output.cache.FileCacheAdapter;
import top.yumbo.ai.reviewer.adapter.output.filesystem.LocalFileSystemAdapter;
import top.yumbo.ai.reviewer.application.port.input.ProjectAnalysisUseCase;
import top.yumbo.ai.reviewer.application.port.input.ReportGenerationUseCase;
import top.yumbo.ai.reviewer.application.service.ProjectAnalysisService;
import top.yumbo.ai.reviewer.application.service.ReportGenerationService;
import top.yumbo.ai.reviewer.domain.model.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 命令行适配器
 * 提供命令行接口，连接用户输入和应用服务
 */
@Slf4j
public class CommandLineAdapter {

    private final ProjectAnalysisUseCase analysisUseCase;
    private final ReportGenerationUseCase reportUseCase;

    public CommandLineAdapter() {
        // 初始化适配器
        DeepSeekAIAdapter aiAdapter = createAIAdapter();
        FileCacheAdapter cacheAdapter = new FileCacheAdapter();
        LocalFileSystemAdapter fileSystemAdapter = createFileSystemAdapter();

        // 初始化服务
        this.analysisUseCase = new ProjectAnalysisService(
                aiAdapter, cacheAdapter, fileSystemAdapter);
        this.reportUseCase = new ReportGenerationService(fileSystemAdapter);
    }

    /**
     * 主入口
     */
    public static void main(String[] args) {
        CommandLineAdapter cli = new CommandLineAdapter();

        try {
            CLIArguments cliArgs = cli.parseArguments(args);
            cli.execute(cliArgs);
        } catch (Exception e) {
            log.error("执行失败", e);
            System.err.println("错误: " + e.getMessage());
            cli.printUsage();
            System.exit(1);
        }
    }

    /**
     * 执行分析
     */
    public void execute(CLIArguments args) {
        log.info("开始项目分析: {}", args.projectPath());

        // 1. 扫描项目
        System.out.println("正在扫描项目...");
        LocalFileSystemAdapter fsAdapter = createFileSystemAdapter();
        Path projectRoot = Paths.get(args.projectPath());

        List<SourceFile> sourceFiles = fsAdapter.scanProjectFiles(projectRoot);
        String structureTree = fsAdapter.generateProjectStructure(projectRoot);

        // 2. 构建项目对象
        Project project = Project.builder()
                .name(projectRoot.getFileName().toString())
                .rootPath(projectRoot)
                .type(detectProjectType(sourceFiles))
                .sourceFiles(sourceFiles)
                .structureTree(structureTree)
                .build();

        System.out.println("项目信息:");
        System.out.println("  - 名称: " + project.getName());
        System.out.println("  - 类型: " + project.getType().getDisplayName());
        System.out.println("  - 文件数: " + project.getSourceFiles().size());
        System.out.println("  - 代码行数: " + project.getTotalLines());

        // 3. 执行分析
        System.out.println("\n正在分析项目...");
        AnalysisTask task;

        if (args.async()) {
            String taskId = analysisUseCase.analyzeProjectAsync(project);
            System.out.println("异步分析任务已启动: " + taskId);

            // 轮询任务状态
            do {
                task = analysisUseCase.getTaskStatus(taskId);
                if (task.getProgress() != null) {
                    System.out.printf("\r进度: %.1f%% - %s",
                            task.getProgress().getPercentage(),
                            task.getProgress().getCurrentTask());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } while (task.isRunning());

            System.out.println("\n");

        } else {
            task = analysisUseCase.analyzeProject(project);
        }

        // 4. 获取结果
        if (task.isCompleted()) {
            ReviewReport report = analysisUseCase.getAnalysisResult(task.getTaskId());

            System.out.println("分析完成！");
            System.out.println("\n=== 分析结果 ===");
            System.out.println("总体评分: " + report.getOverallScore() + "/100 (" + report.getGrade() + ")");
            System.out.println("\n各维度评分:");
            report.getDimensionScores().forEach((dimension, score) ->
                    System.out.println("  - " + dimension + ": " + score + "/100"));

            // 5. 保存报告
            if (args.outputPath() != null) {
                Path outputPath = Paths.get(args.outputPath());
                reportUseCase.saveReport(report, outputPath, args.format());
                System.out.println("\n报告已保存到: " + outputPath);
            } else {
                // 打印Markdown报告到控制台
                System.out.println("\n=== 详细报告 ===");
                String markdownReport = reportUseCase.generateMarkdownReport(report);
                System.out.println(markdownReport);
            }

            System.out.println("\n分析耗时: " + task.getDurationMillis() + " 毫秒");

        } else if (task.isFailed()) {
            System.err.println("分析失败: " + task.getErrorMessage());
            System.exit(1);
        }
    }

    /**
     * 解析命令行参数
     */
    private CLIArguments parseArguments(String[] args) {
        String projectPath = null;
        String outputPath = null;
        String format = "markdown";
        boolean async = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--project", "-p" -> projectPath = args[++i];
                case "--output", "-o" -> outputPath = args[++i];
                case "--format", "-f" -> format = args[++i];
                case "--async", "-a" -> async = true;
                case "--help", "-h" -> {
                    printUsage();
                    System.exit(0);
                }
                default -> throw new IllegalArgumentException("未知参数: " + args[i]);
            }
        }

        if (projectPath == null) {
            throw new IllegalArgumentException("必须指定项目路径 (--project)");
        }

        return new CLIArguments(projectPath, outputPath, format, async);
    }

    /**
     * 打印使用说明
     */
    private void printUsage() {
        System.out.println("AI Reviewer - 六边形架构重构版");
        System.out.println("\n用法: java -jar ai-reviewer-reviewer.jar [选项]");
        System.out.println("\n选项:");
        System.out.println("  -p, --project <路径>    要分析的项目根目录路径 (必需)");
        System.out.println("  -o, --output <文件>     输出报告的文件路径 (可选)");
        System.out.println("  -f, --format <格式>     报告格式: markdown/html/json (默认: markdown)");
        System.out.println("  -a, --async             异步执行分析");
        System.out.println("  -h, --help              显示此帮助信息");
        System.out.println("\n示例:");
        System.out.println("  java -jar ai-reviewer-reviewer.jar --project /path/to/project");
        System.out.println("  java -jar ai-reviewer-reviewer.jar -p . -o report.md -f markdown");
        System.out.println("  java -jar ai-reviewer-reviewer.jar -p /project -a -o report.html -f html");
    }

    /**
     * 检测项目类型
     */
    private ProjectType detectProjectType(List<SourceFile> files) {
        // 统计各语言文件数
        int javaCount = 0, pythonCount = 0, jsCount = 0;

        for (SourceFile file : files) {
            ProjectType type = file.getProjectType();
            switch (type) {
                case JAVA -> javaCount++;
                case PYTHON -> pythonCount++;
                case JAVASCRIPT, TYPESCRIPT -> jsCount++;
            }
        }

        // 返回主要语言
        int max = Math.max(javaCount, Math.max(pythonCount, jsCount));
        if (max == javaCount) return ProjectType.JAVA;
        if (max == pythonCount) return ProjectType.PYTHON;
        if (max == jsCount) return ProjectType.JAVASCRIPT;

        return ProjectType.UNKNOWN;
    }

    /**
     * 创建AI适配器
     */
    private DeepSeekAIAdapter createAIAdapter() {
        // 从环境变量或配置文件读取
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null) {
            apiKey = "sk-1c780a5ae9c94d63861d90d5d1930481"; // 默认密钥
        }

        return new DeepSeekAIAdapter(new DeepSeekAIAdapter.AIServiceConfig(
                apiKey,
                "https://api.deepseek.com/v1",
                "deepseek-chat",
                4000,
                0.3,
                3,
                3,
                1000,
                30000,
                60000
        ));
    }

    /**
     * 创建文件系统适配器
     */
    private LocalFileSystemAdapter createFileSystemAdapter() {
        return new LocalFileSystemAdapter(
                new LocalFileSystemAdapter.FileSystemConfig(
                        List.of("*.java", "*.py", "*.js", "*.ts", "*.xml", "*.yaml", "*.md"),
                        List.of("*test*", "*.class", "*.jar", "node_modules", "target", "build"),
                        1024,
                        4
                )
        );
    }

    /**
     * CLI参数记录
     */
    private record CLIArguments(
            String projectPath,
            String outputPath,
            String format,
            boolean async
    ) {}
}

