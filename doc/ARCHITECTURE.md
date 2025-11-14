# AI-Reviewer 项目架构文档

**版本**: 2.0  
**更新时间**: 2025-11-15  
**状态**: 生产就绪

---

## 📋 目录

1. [项目概述](#项目概述)
2. [架构设计理念](#架构设计理念)
3. [包结构说明](#包结构说明)
4. [核心模块详解](#核心模块详解)
5. [扩展指南](#扩展指南)
6. [最佳实践](#最佳实践)

---

## 🎯 项目概述

### 项目定位

**AI-Reviewer** 是一个**通用文件分析引擎**，而非单一用途的代码审查工具。

**核心能力**:
- 📁 读取文件夹中的各类文件（代码、文档、媒体等）
- 🤖 利用市面上的AI服务进行智能分析
- 📊 生成结构化的分析报告
- 🔌 支持多种AI服务提供商（AWS Bedrock、OpenAI、Claude等）

**应用场景** (示例):
- 代码质量审查（黑客松场景）
- 文档内容分析
- 数据集分析
- 合规性检查
- 技术债务评估

---

## 🏗️ 架构设计理念

### 核心原则

1. **六边形架构** (Hexagonal Architecture)
   - 领域逻辑与外部依赖解耦
   - 通过端口（Port）和适配器（Adapter）隔离外部系统
   - 易于测试和维护

2. **功能模块化**
   - 按功能领域组织包结构，而非技术层次
   - 每个模块职责清晰，边界明确
   - 降低模块间耦合

3. **开放-封闭原则**
   - 对扩展开放：新增AI服务、解析器无需修改核心代码
   - 对修改封闭：核心领域逻辑稳定

4. **依赖倒置**
   - 高层模块不依赖低层模块，都依赖于抽象
   - 使用Guice进行依赖注入

---

## 📦 包结构说明

### 整体结构

```
top.yumbo.ai.reviewer/
├── adapter/              # 适配器层（外部系统接入）
│   ├── storage/          # 存储适配器
│   ├── ai/               # AI服务适配器
│   ├── parser/           # 文件解析器
│   ├── repository/       # 代码仓库适配器
│   ├── input/            # 输入适配器（CLI、API等）
│   └── output/           # 输出适配器（CICD、可视化等）
│
├── application/          # 应用层（用例编排）
│   ├── service/          # 应用服务
│   ├── port/             # 端口定义
│   └── hackathon/        # 黑客松场景（示例）
│
├── domain/               # 领域层（核心业务逻辑）
│   ├── model/            # 领域模型
│   ├── service/          # 领域服务
│   └── hackathon/        # 黑客松领域模型
│
└── infrastructure/       # 基础设施层
    ├── config/           # 配置管理
    ├── di/               # 依赖注入
    └── factory/          # 工厂类
```

---

## 🔍 核心模块详解

### 1. 适配器层 (adapter)

#### 1.1 存储适配器 (adapter.storage)

**职责**: 统一管理所有存储相关的外部系统接入

```
adapter/storage/
├── s3/                   # AWS S3存储
│   ├── S3StorageAdapter.java
│   ├── S3StorageConfig.java
│   └── S3StorageExample.java
│
├── local/                # 本地文件系统
│   └── LocalFileSystemAdapter.java
│
├── cache/                # 缓存
│   └── FileCacheAdapter.java
│
└── archive/              # 压缩归档
    └── ZipArchiveAdapter.java
```

**扩展示例**:
```java
// 新增MinIO存储支持
adapter/storage/minio/
└── MinIOAdapter.java
```

---

#### 1.2 AI服务适配器 (adapter.ai)

**职责**: 统一管理所有AI服务提供商的接入

```
adapter/ai/
├── bedrock/              # AWS Bedrock
│   └── BedrockAdapter.java
│
├── config/               # AI服务配置
│   └── AIServiceConfig.java
│
├── http/                 # HTTP通用客户端
│   └── HttpBasedAIAdapter.java
│
├── decorator/            # 装饰器（日志、监控等）
│   └── LoggingAIServiceDecorator.java
│
└── AIAdapterFactory.java # AI适配器工厂
```

**支持的AI服务**:
- ✅ AWS Bedrock (Claude, Titan等)
- ✅ DeepSeek
- ✅ OpenAI (GPT系列)
- ✅ Claude (Anthropic)
- ✅ Gemini (Google)

**扩展示例**:
```java
// 新增Azure OpenAI支持
adapter/ai/azure/
└── AzureOpenAIAdapter.java
```

---

#### 1.3 解析器适配器 (adapter.parser)

**职责**: 统一管理所有文件解析功能

```
adapter/parser/
├── code/                 # 代码解析器
│   ├── java/
│   │   └── JavaParserAdapter.java
│   ├── python/
│   │   └── PythonParserAdapter.java
│   ├── javascript/
│   │   └── JavaScriptParserAdapter.java
│   ├── go/
│   │   └── GoParserAdapter.java
│   ├── cpp/
│   │   └── CppParserAdapter.java
│   ├── AbstractASTParser.java
│   └── ASTParserFactory.java
│
└── detector/             # 语言检测器
    ├── language/
    │   ├── GoLanguageDetector.java
    │   ├── CppLanguageDetector.java
    │   └── RustLanguageDetector.java
    ├── LanguageDetector.java
    ├── LanguageDetectorRegistry.java
    └── LanguageFeatures.java
```

**支持的编程语言**:
- ✅ Java
- ✅ Python
- ✅ JavaScript/TypeScript
- ✅ Go
- ✅ C/C++
- ✅ Rust (检测支持)

**扩展示例**:
```java
// 新增PDF文档解析器
adapter/parser/document/pdf/
└── PdfParserAdapter.java

// 新增图片分析器
adapter/parser/media/image/
└── ImageAnalyzerAdapter.java
```

---

#### 1.4 仓库适配器 (adapter.repository)

**职责**: 管理代码仓库的接入

```
adapter/repository/
└── git/
    └── GitRepositoryAdapter.java
```

**扩展示例**:
```java
// 新增SVN支持
adapter/repository/svn/
└── SVNRepositoryAdapter.java
```

---

#### 1.5 输入适配器 (adapter.input)

**职责**: 管理不同的输入方式

```
adapter/input/
├── cli/                  # 命令行接口
│   └── CommandLineAdapter.java
│
└── api/                  # REST API接口
    └── APIAdapter.java
```

---

#### 1.6 输出适配器 (adapter.output)

**职责**: 管理不同的输出方式

```
adapter/output/
├── cicd/                 # CI/CD集成
│   └── CICDIntegration.java
│
└── visualization/        # 可视化
    └── ChartGenerator.java
```

---

### 2. 应用层 (application)

**职责**: 编排用例，协调领域对象和适配器

```
application/
├── service/              # 应用服务
│   ├── ProjectAnalysisService.java
│   ├── ReportGenerationService.java
│   └── prompt/
│       └── AIPromptBuilder.java
│
├── port/                 # 端口定义
│   ├── input/            # 输入端口
│   └── output/           # 输出端口
│       ├── AIServicePort.java
│       ├── FileSystemPort.java
│       └── ASTParserPort.java
│
└── hackathon/            # 黑客松场景（示例）
    ├── service/
    │   ├── HackathonIntegrationService.java
    │   └── HackathonScoringService.java
    └── cli/
        ├── HackathonCommandLineApp.java
        └── HackathonInteractiveApp.java
```

---

### 3. 领域层 (domain)

**职责**: 核心业务逻辑，与外部系统无关

```
domain/
├── model/                # 领域模型
│   ├── Project.java
│   ├── SourceFile.java
│   ├── ReviewReport.java
│   ├── ProjectType.java
│   └── ast/              # AST相关模型
│       ├── CodeInsight.java
│       ├── ClassStructure.java
│       ├── MethodInfo.java
│       ├── ComplexityMetrics.java
│       ├── CodeSmell.java
│       └── DesignPattern.java
│
├── service/              # 领域服务
│   └── CodeQualityAnalyzer.java
│
└── hackathon/            # 黑客松领域
    └── model/
        ├── HackathonScore.java
        ├── HackathonScoringConfig.java
        └── DimensionScoringRegistry.java
```

---

### 4. 基础设施层 (infrastructure)

**职责**: 提供技术支持服务

```
infrastructure/
├── config/               # 配置管理
│   └── Configuration.java
│
├── di/                   # 依赖注入
│   └── ApplicationModule.java
│
└── factory/              # 工厂类
    └── AIServiceFactory.java
```

---

## 🚀 扩展指南

### 新增AI服务提供商

**场景**: 添加OpenAI支持

**步骤**:

1. **创建适配器类**
```java
// adapter/ai/openai/OpenAIAdapter.java
package top.yumbo.ai.reviewer.adapter.ai.openai;

public class OpenAIAdapter implements AIServicePort {
    // 实现接口方法
}
```

2. **更新工厂类**
```java
// infrastructure/factory/AIServiceFactory.java
public static AIServicePort create(Configuration config) {
    String provider = config.getAiProvider();
    return switch (provider) {
        case "openai" -> new OpenAIAdapter(config.getAIServiceConfig());
        // ... 其他case
    };
}
```

3. **更新配置文件**
```properties
ai.provider=openai
ai.api.key=sk-xxx
ai.model=gpt-4
```

---

### 新增文件解析器

**场景**: 添加PDF文档解析支持

**步骤**:

1. **创建解析器类**
```java
// adapter/parser/document/pdf/PdfParserAdapter.java
package top.yumbo.ai.reviewer.adapter.parser.document.pdf;

public class PdfParserAdapter implements DocumentParserPort {
    // 实现解析逻辑
}
```

2. **注册解析器**
```java
// infrastructure/di/ApplicationModule.java
@Provides
public DocumentParserPort providePdfParser() {
    return new PdfParserAdapter();
}
```

3. **使用解析器**
```java
DocumentParserPort parser = injector.getInstance(DocumentParserPort.class);
DocumentContent content = parser.parse(pdfFile);
```

---

### 新增存储适配器

**场景**: 添加MinIO对象存储支持

**步骤**:

1. **创建适配器类**
```java
// adapter/storage/minio/MinIOAdapter.java
package top.yumbo.ai.reviewer.adapter.storage.minio;

public class MinIOAdapter implements FileSystemPort {
    // 实现MinIO存储操作
}
```

2. **添加配置**
```properties
storage.type=minio
storage.minio.endpoint=http://localhost:9000
storage.minio.accessKey=minioadmin
storage.minio.secretKey=minioadmin
storage.minio.bucket=ai-reviewer
```

3. **注入使用**
```java
@Provides
public FileSystemPort provideStorage(Configuration config) {
    String type = config.getStorageType();
    return switch (type) {
        case "minio" -> new MinIOAdapter(config);
        case "s3" -> new S3StorageAdapter(config);
        // ...
    };
}
```

---

## 💡 最佳实践

### 1. 依赖方向

```
adapter → application → domain
  ↓
infrastructure
```

**规则**:
- ✅ 适配器可以依赖应用层和领域层
- ✅ 应用层可以依赖领域层
- ❌ 领域层不依赖任何外层
- ✅ 基础设施层可以依赖所有层（组装）

---

### 2. 接口隔离

**端口定义在应用层**:
```java
// application/port/output/AIServicePort.java
public interface AIServicePort {
    String analyzeCode(String code);
}
```

**适配器实现在适配器层**:
```java
// adapter/ai/bedrock/BedrockAdapter.java
public class BedrockAdapter implements AIServicePort {
    @Override
    public String analyzeCode(String code) {
        // AWS Bedrock实现
    }
}
```

---

### 3. 配置管理

**集中配置**:
```java
// infrastructure/config/Configuration.java
public class Configuration {
    private String aiProvider;
    private String aiApiKey;
    // ... getter/setter
}
```

**环境变量优先**:
```bash
export AI_PROVIDER=bedrock
export AI_API_KEY=xxx
```

---

### 4. 错误处理

**使用自定义异常**:
```java
// domain/exception/AnalysisException.java
public class AnalysisException extends RuntimeException {
    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**在边界处理**:
```java
try {
    AIServicePort aiService = factory.create(config);
    String result = aiService.analyzeCode(code);
} catch (Exception e) {
    throw new AnalysisException("AI分析失败", e);
}
```

---

### 5. 测试策略

**单元测试**:
- 领域层：纯POJO，易于测试
- 应用层：Mock端口接口

**集成测试**:
- 适配器层：使用真实或Mock外部系统

**端到端测试**:
- 完整流程验证

---

## 📊 架构演进

### 版本历史

**v1.0** (2025-11-12)
- ❌ 混合包结构
- ❌ 技术层次组织
- ❌ 高耦合

**v2.0** (2025-11-15) ✅ 当前版本
- ✅ 功能模块化包结构
- ✅ 六边形架构
- ✅ 低耦合，高内聚
- ✅ 易于扩展

---

## 🎯 未来规划

### 短期 (本月)
1. ✅ 包结构重组 - 已完成
2. ⏳ 添加文档解析器（PDF、Word）
3. ⏳ 添加更多AI服务（OpenAI、Azure）
4. ⏳ 完善单元测试

### 中期 (本季度)
5. 添加媒体分析能力（图片、视频）
6. 性能优化（并发、缓存）
7. 监控和可观测性
8. 分布式支持

### 长期 (本年度)
9. 插件化架构
10. Web界面
11. 多租户支持
12. 云原生部署

---

## 📚 相关文档

- [包重组执行报告](../md/refactor/20251115003100-PACKAGE-REORG-EXECUTION-REPORT.md)
- [AWS Bedrock快速开始](./AWS/AWS-BEDROCK-QUICKSTART.md)
- [S3集成指南](./AWS/AWS-S3-INTEGRATION-GUIDE.md)
- [黑客松实施指南](./HACKATHON/HACKATHON-IMPLEMENTATION-GUIDE.md)

---

## 🤝 贡献指南

### 添加新功能

1. 确定功能所属模块
2. 在对应包下创建类
3. 实现相应的Port接口
4. 更新DI配置
5. 添加单元测试
6. 更新文档

### 代码规范

- 遵循六边形架构原则
- 保持包结构清晰
- 编写充分的测试
- 添加必要的文档注释

---

**文档版本**: 2.0  
**最后更新**: 2025-11-15  
**维护者**: AI-Reviewer Team

