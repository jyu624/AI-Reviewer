# 命令行接口重构说明

## 🏗️ 架构重构概述

本次重构将命令行接口按照六边形架构原则进行了清晰的职责划分，使通用代码审查和黑客松评审功能各司其职。

## 📁 新的目录结构

```
src/main/java/top/yumbo/ai/reviewer/
│
├── adapter/input/cli/                          # 适配器层 - CLI输入端口
│   ├── CommandLineAdapter.java                # ✅ 通用代码审查命令行适配器
│   └── CommandLineInterface.java              # ✅ 通用代码审查交互式CLI
│
└── application/hackathon/                      # 应用层 - 黑客松领域
    ├── service/                                # 黑客松业务服务
    │   ├── HackathonAnalysisService.java
    │   ├── HackathonScoringService.java
    │   ├── TeamManagementService.java
    │   ├── LeaderboardService.java
    │   └── HackathonIntegrationService.java
    │
    └── cli/                                    # 🆕 黑客松CLI入口（应用层）
        ├── HackathonCommandLineApp.java       # 🆕 黑客松命令行应用（主入口）
        └── HackathonInteractiveApp.java       # 🆕 黑客松交互式应用
```

## 🎯 职责划分

### 1. **通用代码审查工具** （Adapter层）

位于 `adapter.input.cli` 包，专注于通用的代码质量分析：

#### `CommandLineAdapter.java`
- **类型**: 适配器
- **职责**: 提供命令行参数驱动的代码审查
- **入口**: `main()` 方法
- **使用场景**: 一键式项目分析、CI/CD集成
- **特点**: 
  - 参数化配置
  - 支持异步分析
  - 多格式报告输出（Markdown/HTML/JSON）

**使用方式**:
```bash
java -cp ai-reviewer.jar top.yumbo.ai.reviewer.adapter.input.cli.CommandLineAdapter \
  --project /path/to/project \
  --output report.md \
  --format markdown
```

#### `CommandLineInterface.java`
- **类型**: 适配器
- **职责**: 提供交互式命令行界面
- **入口**: `main()` 方法
- **使用场景**: 开发人员手动分析、探索式测试
- **特点**:
  - 菜单驱动
  - 用户友好提示
  - 实时进度展示

**使用方式**:
```bash
java -cp ai-reviewer.jar top.yumbo.ai.reviewer.adapter.input.cli.CommandLineInterface
```

---

### 2. **黑客松评审工具** （Application层）

位于 `application.hackathon.cli` 包，专注于黑客松项目的评分和管理：

#### `HackathonCommandLineApp.java`
- **类型**: 应用服务
- **职责**: 黑客松项目评审的命令行入口
- **入口**: `main()` 方法
- **使用场景**: 批量评审、自动化评分、CI集成
- **特点**:
  - 支持GitHub/Gitee URL直接克隆
  - 多维度评分（代码质量、创新性、完整性、文档）
  - 自动生成评分报告

**使用方式**:
```bash
# 使用GitHub URL
java -jar hackathon-reviewer.jar \
  --github-url https://github.com/user/project \
  --team "Team Awesome" \
  --output score.json \
  --report report.md

# 使用Gitee URL
java -jar hackathon-reviewer.jar \
  --gitee-url https://gitee.com/user/project \
  --team "Team Awesome" \
  --output score.json

# 使用本地目录
java -jar hackathon-reviewer.jar \
  --directory /path/to/project \
  --team "Team Awesome" \
  --output score.json
```

#### `HackathonInteractiveApp.java`
- **类型**: 应用服务
- **职责**: 提供交互式黑客松评审界面
- **入口**: 通过 DI 容器注入使用
- **使用场景**: 评委现场评审、团队管理
- **特点**:
  - 评审单个/批量项目
  - 团队管理
  - 排行榜查看
  - 结果导出

---

## 🔄 重构对比

### 重构前 ❌
```
adapter/input/cli/
├── CommandLineAdapter.java        # 混杂了通用审查和黑客松逻辑
└── CommandLineInterface.java      # 类似混杂
```

**问题**:
- ❌ 违反单一职责原则
- ❌ 通用功能和黑客松功能耦合
- ❌ 难以独立扩展
- ❌ 黑客松功能位置不符合六边形架构

### 重构后 ✅
```
adapter/input/cli/                 # 通用代码审查（适配器层）
├── CommandLineAdapter.java        ✅ 纯粹的通用审查工具
└── CommandLineInterface.java      ✅ 纯粹的通用交互式CLI

application/hackathon/cli/         # 黑客松专用（应用层）
├── HackathonCommandLineApp.java   ✅ 黑客松命令行入口
└── HackathonInteractiveApp.java   ✅ 黑客松交互式应用
```

**优势**:
- ✅ 职责清晰分离
- ✅ 符合六边形架构的分层原则
- ✅ 易于独立扩展和维护
- ✅ 未来可以轻松添加其他应用（如代码竞赛、代码认证等）

---

## 📊 评分维度对比

### 通用代码审查
- 代码质量
- 可维护性
- 安全性
- 性能
- 测试覆盖率

### 黑客松评审
- 代码质量（40%）
- 创新性（30%）
- 完整性（20%）
- 文档质量（10%）

---

## 🚀 快速开始

### 场景1：审查普通项目
```bash
# 使用命令行适配器
java -cp ai-reviewer.jar top.yumbo.ai.reviewer.adapter.input.cli.CommandLineAdapter \
  -p ./my-project -o report.md

# 或使用交互式界面
java -cp ai-reviewer.jar top.yumbo.ai.reviewer.adapter.input.cli.CommandLineInterface
```

### 场景2：黑客松评审
```bash
# 使用命令行应用
java -jar hackathon-reviewer.jar \
  --github-url https://github.com/team/project \
  --team "Team A" \
  -o score.json
```

---

## 🎨 架构设计原则

### 1. **六边形架构分层**
- **Adapter层** (`adapter.input.cli`): 外部接口适配，处理输入输出
- **Application层** (`application.hackathon`): 业务逻辑编排，领域服务

### 2. **单一职责原则**
- 通用审查 ≠ 黑客松评审
- 每个类专注于一个明确的业务场景

### 3. **开闭原则**
- 对扩展开放：未来可添加 `application.contest`、`application.certification` 等
- 对修改封闭：现有通用审查和黑客松功能互不影响

### 4. **依赖倒置原则**
- 高层业务逻辑（Application）不依赖低层细节
- 通过端口（Port）接口解耦

---

## 🔮 未来扩展示例

基于当前架构，可以轻松添加新的应用场景：

```
application/
├── hackathon/               # ✅ 黑客松评审（已实现）
│   ├── cli/
│   └── service/
│
├── contest/                 # 🆕 编程竞赛评审（未来扩展）
│   ├── cli/
│   │   ├── ContestCommandLineApp.java
│   │   └── ContestInteractiveApp.java
│   └── service/
│       ├── ContestScoringService.java
│       └── RankingService.java
│
└── certification/           # 🆕 代码认证（未来扩展）
    ├── cli/
    │   └── CertificationCommandLineApp.java
    └── service/
        ├── CertificationService.java
        └── BadgeService.java
```

---

## 📚 相关文档

- [六边形架构快速指南](../../../md/20251111234200-HEXAGONAL-QUICKSTART-GUIDE.md)
- [架构对比分析](../../../md/20251111234500-ARCHITECTURE-COMPARISON.md)
- [CLI用户指南](../../../md/20251112030000-CLI-USER-GUIDE.md)

---

## ✅ 重构检查清单

- [x] 分离通用审查和黑客松功能
- [x] 将黑客松CLI移至application层
- [x] 保持adapter层的纯粹性
- [x] 更新类注释和文档
- [x] 修复编译错误
- [x] 更新使用说明

---

**重构完成日期**: 2025-11-13  
**架构师**: AI-Reviewer Team  
**版本**: 2.0 (六边形架构重构版)

