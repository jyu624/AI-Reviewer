# AI-Reviewer

> 一个基于六边形架构（Hexagonal Architecture）的智能代码评审框架

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/jinhua10/ai-reviewer)
[![Tests](https://img.shields.io/badge/tests-337%20passed-brightgreen.svg)](https://github.com/jinhua10/ai-reviewer)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE-2.0.txt)

## 📖 项目简介

AI-Reviewer 是一个采用六边形架构设计的智能代码评审框架，支持多种编程语言和AI服务提供商。该框架提供了清晰的领域边界、灵活的适配器设计和强大的扩展能力，可以轻松集成到各种应用场景中。

### 核心特性

- ✨ **六边形架构设计** - 清晰的领域边界，业务逻辑与技术实现完全解耦
- 🤖 **多AI服务支持** - 支持 DeepSeek、Gemini 等多种AI服务提供商
- 📊 **多维度分析** - 代码质量、架构设计、性能优化、安全性等全方位评估
- 📝 **多格式报告** - 支持 Markdown、HTML、JSON 等多种报告格式
- 🔄 **异步处理** - 支持同步和异步分析，提供实时进度反馈
- 💾 **智能缓存** - 基于文件的缓存系统，提升分析效率
- 🌐 **多语言支持** - 支持 Java、Python、JavaScript、TypeScript、Go 等主流语言
- 🔌 **易于扩展** - 通过适配器模式轻松添加新的AI服务或存储方式

## 📁 项目结构

```
AI-Reviewer/
├── src/
│   ├── main/
│   │   ├── java/top/yumbo/ai/reviewer/
│   │   │   ├── domain/              # 领域层 - 核心业务逻辑
│   │   │   │   ├── model/           # 领域模型
│   │   │   │   ├── port/            # 端口定义
│   │   │   │   └── service/         # 领域服务
│   │   │   ├── application/         # 应用层 - 业务用例编排
│   │   │   │   └── service/         # 应用服务
│   │   │   └── adapter/             # 适配器层 - 技术实现
│   │   │       ├── input/           # 输入适配器
│   │   │       │   └── cli/         # 命令行界面
│   │   │       └── output/          # 输出适配器
│   │   │           ├── ai/          # AI服务适配器
│   │   │           ├── cache/       # 缓存适配器
│   │   │           └── filesystem/  # 文件系统适配器
│   │   └── resources/               # 配置文件和模板
│   └── test/                        # 测试代码
│       ├── java/                    # 单元测试和集成测试
│       └── resources/               # 测试资源
├── md/                              # 项目文档（带时间戳前缀）
├── pom.xml                          # Maven 配置
└── README.md                        # 本文件
```

## 🚀 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- Git

### 安装与构建

```bash
# 克隆项目
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn package
```

### 配置 AI 服务

在 `src/main/resources/config.yaml` 中配置您的 AI 服务：

```yaml
ai:
  deepseek:
    apiKey: "your-deepseek-api-key"
    baseUrl: "https://api.deepseek.com/v1"
    model: "deepseek-chat"
    maxConcurrency: 3
  
  gemini:
    apiKey: "your-gemini-api-key"
    baseUrl: "https://generativelanguage.googleapis.com"
    model: "gemini-pro"
```

### 命令行使用

```bash
# 分析单个项目
java -jar target/ai-reviewer-2.0.jar analyze /path/to/project

# 指定报告格式
java -jar target/ai-reviewer-2.0.jar analyze /path/to/project --format markdown

# 指定输出目录
java -jar target/ai-reviewer-2.0.jar analyze /path/to/project --output ./reports

# 多格式输出
java -jar target/ai-reviewer-2.0.jar analyze /path/to/project --format markdown,html,json
```

## 📚 核心概念

### 六边形架构

本项目采用六边形架构（Hexagonal Architecture，也称为端口与适配器架构），将系统分为三层：

```
┌─────────────────────────────────────────────────────┐
│                   Adapter Layer                      │
│  ┌────────────┐                    ┌──────────────┐ │
│  │   Input    │                    │   Output     │ │
│  │  Adapters  │                    │   Adapters   │ │
│  │  (CLI)     │                    │  (AI, Cache) │ │
│  └─────┬──────┘                    └──────▲───────┘ │
│        │                                  │         │
│        ▼                                  │         │
│  ┌─────────────────────────────────────────────┐   │
│  │         Application Layer                   │   │
│  │  ┌─────────────────────────────────────┐   │   │
│  │  │   Domain Layer (Core Business)      │   │   │
│  │  │   - Models                          │   │   │
│  │  │   - Ports (Interfaces)              │   │   │
│  │  │   - Domain Services                 │   │   │
│  │  └─────────────────────────────────────┘   │   │
│  │         Application Services                │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

**优势：**
- 业务逻辑独立于技术实现
- 易于测试和维护
- 技术栈可替换
- 清晰的依赖方向（向内依赖）

### 领域模型

核心领域模型包括：

- **Project** - 项目实体，包含项目元数据和源文件
- **SourceFile** - 源文件实体，包含文件内容和元信息
- **AnalysisTask** - 分析任务，跟踪分析状态和进度
- **ReviewReport** - 评审报告，包含分析结果和建议
- **AnalysisProgress** - 分析进度，提供实时进度反馈

### 端口与适配器

**输入端口（Input Ports）：**
- `AIServicePort` - AI分析服务
- `CachePort` - 缓存服务
- `FileSystemPort` - 文件系统服务

**输出适配器（Output Adapters）：**
- `DeepSeekAIAdapter` - DeepSeek AI 服务实现
- `GeminiAdapter` - Google Gemini AI 服务实现
- `FileCacheAdapter` - 基于文件的缓存实现
- `LocalFileSystemAdapter` - 本地文件系统实现

## 🔧 使用示例

### 编程方式使用

```java
// 创建适配器
AIServicePort aiService = new DeepSeekAIAdapter(apiKey, baseUrl, model);
FileSystemPort fileSystem = new LocalFileSystemAdapter();
CachePort cache = new FileCacheAdapter("./cache");

// 创建应用服务
ProjectAnalysisService analysisService = new ProjectAnalysisService(
    aiService, fileSystem, cache
);

// 分析项目
AnalysisTask task = analysisService.analyzeProject(
    "my-project",
    "/path/to/project"
);

// 获取结果
ReviewReport report = task.getResult();
System.out.println("Overall Score: " + report.getOverallScore());
System.out.println("Grade: " + report.getGrade());
```

### 异步分析

```java
// 异步分析
CompletableFuture<AnalysisTask> futureTask = 
    analysisService.analyzeProjectAsync("my-project", "/path/to/project");

// 获取进度
futureTask.thenAccept(task -> {
    AnalysisProgress progress = task.getProgress();
    System.out.println("Progress: " + progress.getPercentage() + "%");
    System.out.println("Current Phase: " + progress.getCurrentPhase());
});
```

### 生成报告

```java
ReportGenerationService reportService = new ReportGenerationService();

// 生成 Markdown 报告
String markdown = reportService.generateMarkdownReport(report);
reportService.saveReport(report, "./report.md", "markdown");

// 生成 HTML 报告
String html = reportService.generateHtmlReport(report);
reportService.saveReport(report, "./report.html", "html");

// 生成 JSON 报告
String json = reportService.generateJsonReport(report);
reportService.saveReport(report, "./report.json", "json");
```

## 🧪 测试

项目包含全面的测试套件：

```bash
# 运行所有测试（337个测试）
mvn test

# 运行特定测试类
mvn test -Dtest=ProjectAnalysisServiceTest

# 运行集成测试
mvn test -Dtest=*IntegrationTest
```

**测试覆盖：**
- ✅ 单元测试 - 测试单个组件的功能
- ✅ 集成测试 - 测试组件间的交互
- ✅ 端到端测试 - 测试完整的使用场景
- ✅ 边界测试 - 测试边界条件和异常情况
- ✅ 性能测试 - 测试系统性能

## 📊 报告示例

### Markdown 报告

```markdown
# 代码评审报告

**项目**: my-java-project  
**评审时间**: 2025-01-12 15:30:00  
**总体评分**: 85/100  
**等级**: B+

## 评分详情

- 代码质量: 88/100
- 架构设计: 82/100
- 性能优化: 85/100
- 安全性: 90/100
- 可维护性: 83/100

## 主要发现

1. **代码结构清晰** - 采用了良好的分层架构
2. **测试覆盖不足** - 部分模块缺少单元测试
3. **文档完善** - API 文档齐全

## 改进建议

- 增加单元测试覆盖率至80%以上
- 优化数据库查询性能
- 添加日志记录
```

## 🗺️ 扩展指南

### 添加新的 AI 服务

1. 实现 `AIServicePort` 接口
2. 添加配置项
3. 注册适配器

```java
public class NewAIAdapter implements AIServicePort {
    @Override
    public CompletableFuture<String> analyzeAsync(String prompt, String context) {
        // 实现AI分析逻辑
    }
    
    // 实现其他方法...
}
```

### 添加新的报告格式

在 `ReportGenerationService` 中添加新方法：

```java
public String generatePdfReport(ReviewReport report) {
    // 生成PDF报告
}
```

### 支持新的编程语言

在 `SourceFile` 中添加语言识别逻辑，并在分析模板中添加对应的提示词。

## 📖 文档归档

所有项目相关的文档都存放在 `md/` 目录下，文件名格式为：`YYYYMMDDHHmmss-文档名称.md`

**重要文档：**
- 架构分析报告
- 六边形重构指南
- 实施策略对比分析
- 测试执行报告
- 功能完成总结

## 🤝 贡献指南

欢迎贡献！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

**代码规范：**
- 遵循 Java 编码规范
- 编写单元测试
- 更新相关文档
- 确保所有测试通过

## 📝 版本历史

### v2.0.0 (2025-01-12)
- ✨ 重构为六边形架构
- ✨ 支持多AI服务提供商
- ✨ 添加异步分析能力
- ✨ 增强缓存机制
- ✨ 完善测试覆盖（337个测试）

### v1.0.0 (2024-12-01)
- 🎉 首次发布
- 基础代码分析功能
- Markdown 报告生成

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 详见 [LICENSE-2.0.txt](LICENSE-2.0.txt)

## 👥 作者与维护者

- **主要开发者** - [@jinhua10](https://github.com/jinhua10)

## 🙏 致谢

- DeepSeek AI - 提供强大的AI分析能力
- Google Gemini - 多模态AI支持
- JetBrains - 优秀的开发工具

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 📧 Email: 1015770492@qq.com
- 🐛 Issues: [GitHub Issues](https://github.com/jinhua10/ai-reviewer/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/jinhua10/ai-reviewer/discussions)

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！

