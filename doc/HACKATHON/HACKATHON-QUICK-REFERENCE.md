# 🎯 黑客松评审快速参考卡

## 🚀 5秒速查

```bash
# GitHub项目评审
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url <URL> --team "团队名" -o score.json -r report.md

# Gitee项目评审
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --gitee-url <URL> --team "团队名" -o score.json -r report.md

# 本地项目评审
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --directory <路径> --team "团队名" -o score.json -r report.md
```

---

## ⚙️ AI服务配置速查

### 环境变量（推荐）

| AI服务 | 配置命令 |
|--------|---------|
| **DeepSeek** | `export AI_PROVIDER=deepseek`<br>`export AI_API_KEY=sk-xxx` |
| **OpenAI** | `export AI_PROVIDER=openai`<br>`export AI_API_KEY=sk-xxx` |
| **Gemini** | `export AI_PROVIDER=gemini`<br>`export AI_API_KEY=xxx` |
| **Claude** | `export AI_PROVIDER=claude`<br>`export AI_API_KEY=sk-ant-xxx` |

### 配置文件

```yaml
# src/main/resources/config.yaml
aiService:
  provider: "deepseek"  # 或 openai, gemini, claude
  apiKey: "your-api-key"
  model: "deepseek-chat"  # 或 gpt-4, gemini-pro
```

---

## 📝 命令参数速查

| 参数 | 简写 | 说明 | 必需 | 示例 |
|------|------|------|------|------|
| `--github-url` | - | GitHub仓库URL | * | `--github-url https://github.com/user/repo` |
| `--gitee-url` | - | Gitee仓库URL | * | `--gitee-url https://gitee.com/user/repo` |
| `--directory` | `-d` | 本地项目目录 | * | `-d /path/to/project` |
| `--team` | `-t` | 团队名称 | ✓ | `--team "Team A"` |
| `--branch` | `-b` | Git分支 | - | `-b develop` |
| `--output` | `-o` | JSON输出文件 | - | `-o score.json` |
| `--report` | `-r` | Markdown报告 | - | `-r report.md` |

**注**: 标记 * 的参数三选一

---

## 📊 评分维度速查

| 维度 | 权重 | 评估要点 |
|------|------|---------|
| **代码质量** | 40% | 结构、规范、复杂度、设计模式 |
| **创新性** | 30% | 技术方案、用户体验、商业价值 |
| **完整性** | 20% | 功能实现、错误处理、可扩展性 |
| **文档质量** | 10% | README、注释、API文档 |

**等级划分**:
- **A+** (95-100): 卓越
- **A** (90-94): 优秀  
- **B+** (80-89): 良好
- **B** (70-79): 中等
- **C** (60-69): 及格
- **D** (<60): 不及格

---

## 🔧 常见问题速查

### Q: 如何切换AI服务？

```bash
# 方法1: 环境变量（无需重新编译）
export AI_PROVIDER=openai
export AI_API_KEY=sk-xxx

# 方法2: 修改config.yaml（需重新编译）
vim src/main/resources/config.yaml
mvn clean package -DskipTests
```

### Q: GitHub克隆失败？

```bash
# 解决方案1: 使用代理
export https_proxy=http://127.0.0.1:7890

# 解决方案2: 使用本地目录
git clone https://github.com/user/repo
--directory ./repo

# 解决方案3: 使用Gitee镜像
--gitee-url https://gitee.com/user/repo
```

### Q: 本地项目路径？

```bash
# 绝对路径（推荐）
--directory /home/user/projects/my-project  # Linux/macOS
--directory C:\Users\user\projects\my-project  # Windows

# 相对路径
--directory ./my-project  # 当前目录下
--directory ../another-project  # 父目录
```

### Q: API调用失败？

```yaml
# 降低并发数
aiService:
  maxConcurrency: 5  # 从20降到5
  
# 增加重试延迟
  retryDelay: 3000  # 从1秒增加到3秒
  
# 启用缓存
cache:
  enabled: true
```

---

## 📦 批量评审速查

### Linux/macOS

```bash
# 准备项目列表 projects.txt
# 格式: 团队名,URL
Team A,https://github.com/teamA/project-a
Team B,https://gitee.com/teamB/project-b

# 批量评审脚本
#!/bin/bash
while IFS=',' read -r team url; do
    java -cp target/ai-reviewer-2.0.jar \
      top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
      --github-url "$url" --team "$team" \
      -o "results/${team// /_}_score.json" \
      -r "results/${team// /_}_report.md"
done < projects.txt
```

### Windows

```batch
@echo off
for /F "tokens=1,2 delims=," %%A in (projects.txt) do (
    java -cp target\ai-reviewer-2.0.jar ^
      top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp ^
      --github-url "%%B" --team "%%A" ^
      -o "results\%%A_score.json" ^
      -r "results\%%A_report.md"
)
```

---

## 🏆 排行榜生成速查

```bash
# 使用jq生成排行榜
jq -s 'sort_by(-.overallScore) | to_entries | 
    .[] | [.key+1, .value.teamName, .value.overallScore, .value.grade] | 
    "| \(.[0]) | \(.[1]) | \(.[2]) | \(.[3]) |"' \
    results/*_score.json -r
```

---

## 🎓 最佳实践速查

### ✅ DO

- ✅ 使用环境变量管理API密钥
- ✅ 启用缓存避免重复分析
- ✅ 排除测试代码和第三方库
- ✅ 分批评审（每批10-20个项目）
- ✅ 定期备份评审结果
- ✅ 查看详细报告，不只看总分

### ❌ DON'T

- ❌ 不要在代码中硬编码API密钥
- ❌ 不要同时评审超过50个项目
- ❌ 不要使用过高的并发数（>20）
- ❌ 不要禁用缓存
- ❌ 不要评审超大项目（>100MB）
- ❌ 不要忽略报告中的警告

---

## 📚 文档导航

- 📘 **[完整指南](./HACKATHON-GUIDE.md)** - 详细教程
- 📖 **[README](../../README.md)** - 项目介绍和快速开始
- 🏗️ **[架构文档](../CLI-ARCHITECTURE.md)** - 架构设计
- 🔄 **[CLI重构](../CLI-REFACTORING.md)** - CLI设计说明

---

## 💡 一键命令模板

### 模板1: GitHub完整评审

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/USER/REPO \
  --team "TEAM_NAME" \
  --branch main \
  --output results/TEAM_NAME_score.json \
  --report results/TEAM_NAME_report.md
```

### 模板2: Gitee快速评审

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --gitee-url https://gitee.com/USER/REPO \
  -t "TEAM_NAME" \
  -o results/score.json
```

### 模板3: 本地项目评审

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  -d /PATH/TO/PROJECT \
  -t "TEAM_NAME" \
  -o results/score.json \
  -r results/report.md
```

---

**快速参考卡 v2.0**  
最后更新: 2025-11-13  
更多信息: [完整文档](./HACKATHON-GUIDE.md) | [项目主页](../../README.md)

