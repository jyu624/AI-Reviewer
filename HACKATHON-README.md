# 🏆 Hackathon AI 源码评分工具

> 专业的AI驱动黑客松评审系统，让源码评审更客观、更高效、更专业

[![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)](https://github.com/jinhua10/ai-reviewer)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)

## 🎯 产品简介

Hackathon AI 源码评分工具是一款专为黑客松比赛设计的专业AI评审系统。通过多维度AI分析，为参赛项目提供客观、全面、专业的评分和建议。

### ✨ 核心特性

- 🚀 **智能评审**: 基于AI的客观量化评估，减少主观偏差
- 📊 **多模式评审**: 快速评审、详细评审、专家评审，适应不同评审阶段
- ⚡ **高性能处理**: 异步并发处理，支持大规模项目同时评审
- 🏆 **专业排行榜**: 实时排名统计，多维度数据分析
- 📄 **结构化报告**: 专业的评审报告，包含详细技术分析
- 🎛️ **灵活配置**: 可配置评分规则，适应不同比赛主题

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Hackathon AI 评审工具                      │
├─────────────────────────────────────────────────────────────┤
│  🎯 评审模式层                                               │
│  ├─ 快速评审 (10秒) - 大规模初筛                            │
│  ├─ 详细评审 (30秒) - 复赛评审                              │
│  └─ 专家评审 (60秒) - 决赛评审                              │
├─────────────────────────────────────────────────────────────┤
│  📊 核心功能层                                               │
│  ├─ 智能评审引擎 - AI驱动的客观评估                         │
│  ├─ 批量处理系统 - 并发评审多个项目                         │
│  ├─ 排行榜系统 - 实时排名和统计                             │
│  ├─ 报告生成器 - 专业评审报告输出                           │
│  └─ 历史追踪器 - 完整的评审记录管理                         │
├─────────────────────────────────────────────────────────────┤
│  🔧 基础设施层                                               │
│  ├─ AI服务接口 - DeepSeek AI集成                            │
│  ├─ 缓存系统 - 智能缓存提升性能                             │
│  ├─ 配置管理 - 灵活的规则配置                               │
│  └─ 异常处理 - 健壮的错误处理机制                           │
└─────────────────────────────────────────────────────────────┘
```

## 📊 评审维度

### 技术维度 (40-50%)
- **架构设计** (15-20%): 分层架构、设计模式、技术选型
- **代码质量** (20%): 规范性、注释、异常处理、命名规范
- **技术债务** (10-15%): 过时技术、安全漏洞、维护难度

### 功能维度 (25-30%)
- **功能完整性** (25%): 需求实现、边界处理、错误处理、用户体验

### 商业维度 (15-20%)
- **商业价值** (15%): 市场潜力、创新程度、竞争优势

### 质量保证维度 (5-10%)
- **测试覆盖率** (5-10%): 单元测试、集成测试、测试质量

### 创新维度 (10%)
- **创新性** (专家模式): 技术创新、解决方案创新、用户体验创新

## 🚀 快速开始

### 环境要求
- **JDK**: 17+
- **内存**: 4GB+
- **网络**: 稳定的互联网连接
- **API**: DeepSeek API密钥

### 下载安装
```bash
# 下载最新版本
wget https://github.com/jinhua10/ai-reviewer/releases/download/v2.0/hackathon-reviewer-2.0.jar

# 或从源码构建
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer
mvn clean package
```

### 配置API密钥
```bash
# 设置环境变量
export DEEPSEEK_API_KEY="your-api-key-here"
```

### 基本使用
```bash
# 快速评审单个项目
java -jar hackathon-reviewer.jar review /path/to/project QUICK

# 批量评审多个项目
java -jar hackathon-reviewer.jar batch project1 project2 project3 DETAILED

# 查看排行榜
java -jar hackathon-reviewer.jar leaderboard

# 查看评审统计
java -jar hackathon-reviewer.jar stats
```

## 📋 命令行接口

### 评审命令
```bash
# 评审单个项目 (默认快速模式)
java -jar hackathon-reviewer.jar review <项目路径>

# 指定评审模式
java -jar hackathon-reviewer.jar review <项目路径> <模式>

# 批量评审
java -jar hackathon-reviewer.jar batch <路径1> <路径2> ... [模式]
```

### 数据命令
```bash
# 显示排行榜
java -jar hackathon-reviewer.jar leaderboard

# 显示评审统计
java -jar hackathon-reviewer.jar stats

# 运行功能演示
java -jar hackathon-reviewer.jar demo
```

### 评审模式
- **QUICK**: 快速评审 (10秒) - 适合大规模初筛
- **DETAILED**: 详细评审 (30秒) - 适合复赛评审
- **EXPERT**: 专家评审 (60秒) - 适合决赛评审

## 🎖️ 评审流程建议

### Phase 1: 初赛阶段 (快速评审)
```
参赛项目提交 → AI快速预审 → 基础筛选 → 进入复赛
     ↓              ↓              ↓          ↓
   源码上传      10秒评分      70分以上    20个项目
```

### Phase 2: 复赛阶段 (详细评审)
```
复赛项目 → AI详细评审 → 专家复审 → 晋级决赛
    ↓            ↓              ↓          ↓
 20个项目     30秒深度分析    人工确认    5个项目
```

### Phase 3: 决赛阶段 (专家评审)
```
决赛项目 → AI专家评审 → 评审团终审 → 获奖公布
    ↓            ↓                ↓          ↓
  5个项目     60秒专家分析      综合评分    获奖名单
```

## 📊 评分标准

### 优秀项目 (90-100分)
- 技术架构设计优秀，代码质量上乘
- 功能完整，创新性突出
- 商业价值高，市场前景良好
- 测试覆盖完善，文档齐全

### 良好项目 (75-89分)
- 技术实现扎实，架构合理
- 功能较为完整，有一定创新
- 商业模式可行，价值明确
- 基本测试覆盖，代码规范

### 及格项目 (60-74分)
- 技术实现基本完成，架构清晰
- 核心功能实现，有改进空间
- 商业价值一般，需进一步验证
- 测试覆盖不足，规范待提升

### 待改进项目 (<60分)
- 技术实现存在问题，架构不合理
- 功能不完整，体验差
- 商业价值不明，创新不足
- 缺乏测试，代码质量低

## 🔧 高级配置

### 自定义评分规则
```yaml
# hackathon-config.yaml
scoring:
  rules:
    - name: "custom-architecture-rule"
      type: "ARCHITECTURE"
      weight: 0.20
      config:
        keywords:
          positive:
            "微服务": 20
            "云原生": 15
          negative:
            "单体架构": -10
```

### 性能调优
```yaml
# 调整��发和超时设置
aiService:
  maxConcurrency: 10    # 增加并发数
  timeout: 20000       # 减少超时时间

analysis:
  batchSize: 5         # 减少批处理大小
  maxConcurrentBatches: 3  # 控制并发批次
```

## 📈 性能指标

| 评审模式 | 处理时间 | 并发能力 | 适用场景 |
|----------|----------|----------|----------|
| 快速评审 | 10秒 | 20项目/分钟 | 大规模初筛 |
| 详细评审 | 30秒 | 8项目/分钟 | 复赛评审 |
| 专家评审 | 60秒 | 4项目/分钟 | 决赛评审 |

## 🛠️ 开发指南

### 项目结构
```
hackathon-reviewer/
├── src/main/java/top/yumbo/ai/reviewer/
│   ├── HackathonReviewer.java      # 核心评审引擎
│   ├── HackathonCLI.java           # 命令行接口
│   ├── HackathonDemo.java          # 功能演示
│   └── HackathonValidation.java    # 功能验证
├── src/main/resources/
│   ├── hackathon-config.yaml       # 黑客松配置
│   └── templates/                  # 报告模板
│       ├── hackathon-quick-report.md
│       ├── hackathon-detailed-report.md
│       └── hackathon-expert-report.md
└── HACKATHON-REVIEW-GUIDE.md       # 评审指南
```

### 扩展开发
```java
// 自定义评审��式
public class CustomReviewMode extends HackathonReviewer {
    public HackathonScore customReview(String projectPath) {
        // 实现自定义评审逻辑
        return super.review(projectPath, ReviewMode.EXPERT);
    }
}
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📞 技术支持

- 📧 **邮箱**: support@hackathon-ai-reviewer.com
- 💬 **微信群**: Hackathon AI评审工具交流群
- 📚 **文档**: [评审指南](HACKATHON-REVIEW-GUIDE.md)
- 🐛 **问题反馈**: [GitHub Issues](https://github.com/jinhua10/ai-reviewer/issues)

## 📄 许可证

本项目采用 Apache 2.0 许可证 - 查看 [LICENSE](LICENSE-2.0.txt) 文件了解详情。

## 🙏 致谢

感谢所有为 Hackathon AI 评审工具做出贡献的开发者！

特别感谢：
- DeepSeek AI 提供强大的AI能力
- 开源社区的技术支持
- 黑客松组织者的宝贵建议

---

**让评审更客观，让比赛更公平！** 🎉

*Hackathon AI 源码评分工具 - 让AI为黑客松添彩* ✨</content>
<parameter name="filePath">D:\Jetbrains\hackathon\AI-Reviewer\HACKATHON-README.md
