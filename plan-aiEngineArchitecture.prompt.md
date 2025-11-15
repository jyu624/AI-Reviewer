# AI引擎模块化架构设计

## 概述

设计一个基于Maven多模块的AI引擎架构，通过适配器模式实现文件解析、AI服务调用和结果处理的解耦，支持多种文件类型、AI服务和自定义处理器的灵活扩展。

## 核心职责

AI引擎负责：
1. 读取目录的文件
2. 通过"解析器适配器"分析文件将文件进行解析得到AI能处理的"预处理数据"
3. 根据预处理数据通过"AI适配器"去调用对应的AI服务得到AI返回的数据
4. 收集这些数据通过"处理器适配器"根据用户定制化的处理器生成用户需要的结果

## 模块划分

### 1. ai-reviewer-api（接口定义模块）

**职责**: 定义所有核心接口和数据模型，作为整个系统的契约层

**核心接口**:
- `IFileParser` - 文件解析器接口
  - `boolean support(File file)` - 判断是否支持该文件类型
  - `PreProcessedData parse(File file)` - 解析文件为预处理数据
  
- `IAIService` - AI服务接口
  - `AIResponse invoke(PreProcessedData data, AIConfig config)` - 调用AI服务
  - `boolean isAvailable()` - 检查服务是否可用
  
- `IResultProcessor` - 结果处理器接口
  - `ProcessResult process(List<AIResponse> responses, ProcessorConfig config)` - 处理AI响应
  - `String getProcessorType()` - 获取处理器类型

**数据模型**:
- `PreProcessedData` - 预处理数据模型
  - 文件元信息（路径、类型、大小等）
  - 解析后的结构化内容
  - 上下文信息
  
- `AIResponse` - AI响应模型
  - 响应内容
  - 置信度
  - 元数据（模型、耗时等）
  
- `ProcessResult` - 处理结果模型
  - 结果类型
  - 结果内容
  - 输出格式

**依赖**: 无外部依赖（纯接口定义）

### 2. ai-reviewer-core（核心引擎模块）

**职责**: 实现AI引擎的核心编排逻辑，协调各个适配器完成处理流程

**核心类**:
- `AIEngine` - 主引擎类
  - 文件扫描和过滤
  - 解析器适配器选择和调用
  - AI适配器选择和调用
  - 结果处理器编排
  - 异常处理和重试机制
  
- `AdapterRegistry` - 适配器注册中心
  - 解析器注册和查找
  - AI服务注册和查找
  - 处理器注册和查找
  - 支持动态加载（SPI机制）
  
- `ExecutionContext` - 执行上下文
  - 存储执行过程中的状态
  - 管理配置信息
  - 提供日志和监控钩子

**设计模式**:
- 责任链模式：文件处理流程
- 策略模式：适配器选择
- 工厂模式：对象创建
- 观察者模式：执行监听

**依赖**: ai-reviewer-api

### 3. ai-reviewer-adaptor-parser（解析器适配器模块）

**职责**: 提供多种文件类型的解析器实现, 注意后期考虑支持office办公套件，媒体文件等复杂格式的解析器

**内置解析器**:
- `JavaFileParser` - Java源文件解析器
  - 基于JavaParser库
  - 提取类、方法、注释、依赖关系
- `AbstractASTParser` - 基于AST解析的抽象层
  - 提取函数、类、装饰器、文档字符串  

- `PythonFileParser` - Python文件解析器继承自`AbstractASTParser`
  - 基于AST解析
- `JavaScriptFileParser` - JavaScript文件解析器继承自`AbstractASTParser`
  - 基于AST解析
  
- `XMLFileParser` - XML配置文件解析器
  - DOM/SAX解析
  - 提取配置项和结构
  
- `MarkdownFileParser` - Markdown文档解析器
  - 提取标题、代码块、链接
  
- `PlainTextFileParser` - 通用文本解析器
  - 兜底解析器，支持所有文本文件

**扩展点**:
- 用户可通过实现 `IFileParser` 接口添加自定义解析器
- 通过SPI机制自动发现

**依赖**: ai-reviewer-api, ai-reviewer-core, JavaParser, Python AST工具库

### 4. ai-reviewer-adaptor-ai（AI适配器模块）

**职责**: 集成多种AI服务提供商，统一调用接口

**内置AI适配器**:
- `OpenAIAdapter` - OpenAI服务适配器
  - 支持GPT-3.5/GPT-4系列
  - 流式响应支持
  
- `ClaudeAdapter` - Anthropic Claude适配器
  - 支持Claude 2/3系列
  - 长文本处理优化
  
- `AzureOpenAIAdapter` - Azure OpenAI适配器
  - 企业级部署支持
  - 
- `AWSBedrockAdapter` - AWS Bedrock适配器
    - 企业级部署支持  

- `LocalModelAdapter` - 本地模型适配器
  - 支持Ollama等本地部署
  - 支持自定义模型端点
  
- `BaseHttpAIAdapter` - 自定义API适配器
  - 支持任意HTTP API
  - 可配置请求/响应映射

**功能特性**:
- 统一的错误处理和重试
- 速率限制和并发控制
- API密钥管理
- 成本追踪和监控

**依赖**: ai-reviewer-api, ai-reviewer-core, HTTP客户端库（OkHttp/HttpClient）

### 5. ai-reviewer-adaptor-processor（处理器适配器模块）

**职责**: 提供多种结果处理器，将AI响应转换为用户需要的最终结果

**内置处理器**:
- `CodeReviewProcessor` - 代码审查处理器
  - 生成代码审查报告
  - 问题分级（严重/警告/建议）
  - Markdown/HTML输出
  
- `DocumentGeneratorProcessor` - 文档生成处理器
  - API文档生成
  - 代码注释生成
  - 架构文档生成
  
- `QualityReportProcessor` - 质量报告处理器
  - 代码质量评分
  - 复杂度分析
  - 可维护性指标
  
- `RefactorSuggestionProcessor` - 重构建议处理器
  - 识别代码坏味道
  - 提供重构方案
  
- `SecurityAuditProcessor` - 安全审计处理器
  - 安全漏洞检测
  - 最佳实践检查

**输出格式**:
- JSON
- Markdown
- HTML
- PDF（通过模板引擎）
- 自定义格式

**依赖**: ai-reviewer-api, ai-reviewer-core, 模板引擎（Thymeleaf/Freemarker）

### 6. ai-reviewer-starter（启动器模块）

**职责**: 提供开箱即用的启动配置和命令行工具

**功能**:
- Spring Boot自动配置
  - 自动扫描和注册所有适配器
  - 配置文件加载
  - Bean装配
  
- 命令行工具
  - `ai-review --path ./src --parser java --ai openai --processor code-review`
  - 参数验证和帮助信息
  
- 配置模板
  - `application.yml` 默认配置
  - `custom-config.yml` 用户配置示例
  
- Web API接口（可选）
  - RESTful API
  - WebSocket实时推送
  - Swagger文档

**依赖**: 所有模块（聚合）, Spring Boot, CLI框架（Picocli）

### 7. ai-reviewer-common（工具模块-可选）

**职责**: 提供通用工具类和基础设施

**功能**:
- 日志工具（SLF4J封装）
- 异常体系（自定义异常类）
- 工具类（文件、字符串、集合等）
- 常量定义
- 配置加载器

**依赖**: SLF4J, Lombok（可选）

### 8. ai-reviewer-test（测试套件模块-可选）

**职责**: 提供集成测试和示例

**内容**:
- 单元测试基类
- Mock工具
- 集成测试用例
- 性能测试
- 示例代码

**依赖**: JUnit 5, Mockito, AssertJ

## 依赖关系图

```
ai-reviewer-starter
    ├── ai-reviewer-core
    │   └── ai-reviewer-api
    ├── ai-reviewer-adaptor-parser
    │   ├── ai-reviewer-api
    │   └── ai-reviewer-core
    ├── ai-reviewer-adaptor-ai
    │   ├── ai-reviewer-api
    │   └── ai-reviewer-core
    ├── ai-reviewer-adaptor-processor
    │   ├── ai-reviewer-api
    │   └── ai-reviewer-core
    └── ai-reviewer-common (可选)
        └── ai-reviewer-api
```

## 核心处理流程

```
1. 用户启动 → AIEngine.execute(config)
                ↓
2. 文件扫描 → FileScanner.scan(directory)
                ↓
3. 文件过滤 → FileFilter.filter(files, patterns)
                ↓
4. 解析适配 → AdapterRegistry.getParser(file)
                ↓
5. 文件解析 → IFileParser.parse(file) → PreProcessedData
                ↓
6. AI调用   → AdapterRegistry.getAIService(config)
                ↓
7. AI处理   → IAIService.invoke(data) → AIResponse
                ↓
8. 结果收集 → ResponseCollector.collect(responses)
                ↓
9. 处理适配 → AdapterRegistry.getProcessor(type)
                ↓
10. 结果处理 → IResultProcessor.process(responses) → ProcessResult
                ↓
11. 输出结果 → ResultWriter.write(result, format)
```

## 配置管理策略

### 配置层级（优先级从低到高）
1. **默认配置**（代码内置）- 框架提供的合理默认值
2. **全局配置文件**（application.yml）- 项目级通用配置
3. **用户自定义配置**（custom-config.yml）- 用户个性化配置
4. **环境变量** - 部署环境特定配置
5. **命令行参数** - 运行时临时覆盖

### 配置示例

```yaml
# application.yml
ai-reviewer:
  # 文件扫描配置
  scanner:
    include-patterns:
      - "**/*.java"
      - "**/*.py"
    exclude-patterns:
      - "**/target/**"
      - "**/build/**"
    max-file-size: 10MB
  
  # 解析器配置
  parser:
    enabled-parsers:
      - java
      - python
      - xml
    custom-parsers: []
  
  # AI服务配置
  ai:
    provider: openai
    model: gpt-4
    api-key: ${OPENAI_API_KEY}
    timeout: 30s
    max-retries: 3
    temperature: 0.7
  
  # 处理器配置
  processor:
    type: code-review
    output-format: markdown
    output-path: ./reports
    
  # 并发配置
  executor:
    thread-pool-size: 10
    max-queue-size: 100
```

## 扩展机制

### SPI（Service Provider Interface）
- 在 `META-INF/services/` 下定义服务提供者
- 自动发现和加载实现类
- 支持热插拔

### 自定义解析器示例
```java
public class CustomFileParser implements IFileParser {
    @Override
    public boolean support(File file) {
        return file.getName().endsWith(".custom");
    }
    
    @Override
    public PreProcessedData parse(File file) {
        // 自定义解析逻辑
    }
}
```

在 `META-INF/services/top.yumbo.ai.api.IFileParser` 中注册：
```
com.example.CustomFileParser
```

## 技术栈建议

### 核心框架
- Java 17+
- Spring Boot 3.x
- Maven 3.8+

### 解析库
- JavaParser（Java代码解析）
- ANTLR（通用语法解析）
- Jackson/Gson（JSON处理）

### HTTP客户端
- OkHttp 4.x
- Spring WebClient

### 模板引擎
- Thymeleaf
- Freemarker

### 测试框架
- JUnit 5
- Mockito
- AssertJ

### 工具库
- Lombok（减少样板代码）
- Guava（工具类）
- Apache Commons

## 扩展性考虑

### 1. 适配器发现机制
- 使用Java SPI或Spring的自动装配
- 支持通过配置文件启用/禁用适配器
- 支持优先级排序

### 2. 插件系统（未来扩展）
- 支持外部JAR包动态加载
- 插件生命周期管理
- 插件隔离和沙箱

### 3. 分布式支持（未来扩展）
- 任务队列（RabbitMQ/Kafka）
- 分布式缓存（Redis）
- 负载均衡

### 4. 监控和观测
- Metrics采集（Micrometer）
- 链路追踪（OpenTelemetry）
- 日志聚合

## 实施步骤

### Phase 1: 基础架构（Week 1-2）
1. 创建Maven多模块结构
2. 实现 ai-reviewer-api 模块（接口定义）
3. 实现 ai-reviewer-core 模块（核心引擎）
4. 实现 ai-reviewer-common 模块（工具类）

### Phase 2: 适配器实现（Week 3-4）
1. 实现 ai-reviewer-adaptor-parser（至少2个解析器）
2. 实现 ai-reviewer-adaptor-ai（至少1个AI服务）
3. 实现 ai-reviewer-adaptor-processor（至少1个处理器）

### Phase 3: 集成和启动器（Week 5）
1. 实现 ai-reviewer-starter
2. Spring Boot自动配置
3. 命令行工具
4. 配置文件模板

### Phase 4: 测试和文档（Week 6）
1. 单元测试覆盖
2. 集成测试
3. 用户文档
4. API文档

## 关键设计原则

1. **开闭原则**: 对扩展开放，对修改关闭
2. **依赖倒置**: 依赖抽象而非具体实现
3. **单一职责**: 每个模块只负责一个功能领域
4. **接口隔离**: 接口细粒度划分
5. **最小知识**: 模块间低耦合

## 成功指标

- ✅ 添加新解析器无需修改核心代码
- ✅ 切换AI服务只需修改配置
- ✅ 自定义处理器可独立开发和部署
- ✅ 单元测试覆盖率 > 80%
- ✅ 完整的使用文档和示例

