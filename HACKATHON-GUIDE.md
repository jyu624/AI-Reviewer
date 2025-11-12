# 黑客松项目评审完整指南

<div align="center">

**AI-Reviewer 黑客松模式使用手册**

支持 GitHub 和 Gitee | 自动评分 | 实时排行榜 | 详细报告

[快速开始](#-快速开始) · [完整流程](#-完整流程) · [配置详解](#-配置详解) · [评分说明](#-评分说明)

</div>

---

## 📖 目录

- [快速开始](#-快速开始)
- [完整流程](#-完整流程)
  - [阶段1: 环境准备](#阶段1-环境准备)
  - [阶段2: 项目配置](#阶段2-项目配置)
  - [阶段3: 参赛项目收集](#阶段3-参赛项目收集)
  - [阶段4: 自动评审](#阶段4-自动评审)
  - [阶段5: 结果导出](#阶段5-结果导出)
- [配置详解](#-配置详解)
- [评分标准](#-评分标准)
- [故障排查](#-故障排查)
- [最佳实践](#-最佳实践)

---

## 🚀 快速开始

### 一键评审（5分钟快速上手）

```bash
# 1. 克隆项目
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer

# 2. 编译
mvn clean compile -DskipTests

# 3. 评审单个项目（示例）

# 评审 GitHub 项目
java -jar target/ai-reviewer.jar hackathon \
  --github-url https://github.com/user/hackathon-project \
  --team "Team Awesome" \
  --output score.json

# 评审 Gitee 项目
java -jar target/ai-reviewer.jar hackathon \
  --gitee-url https://gitee.com/user/hackathon-project \
  --team "Team Awesome" \
  --output score.json

# 4. 查看结果
cat score.json
```

### 快速评分示例

```json
{
  "projectName": "hackathon-project",
  "teamName": "Team Awesome",
  "totalScore": 85,
  "dimensions": {
    "innovation": 90,
    "practicality": 85,
    "codeQuality": 80,
    "completeness": 85,
    "documentation": 85
  },
  "ranking": 1,
  "comments": "创新性强，代码质量高..."
}
```

---

## 📋 完整流程

## 阶段1: 环境准备

### 1.1 系统要求

| 组件 | 最低要求 | 推荐配置 |
|------|---------|---------|
| **Java** | 17+ | 21+ |
| **Maven** | 3.8+ | 3.9+ |
| **内存** | 4GB | 8GB+ |
| **磁盘** | 10GB | 20GB+ |
| **网络** | 能访问 GitHub/Gitee | 稳定网络 |

### 1.2 安装 Java

```bash
# 检查 Java 版本
java -version

# 如果未安装，下载 Java 17+
# Windows: https://www.oracle.com/java/technologies/downloads/
# Mac: brew install openjdk@17
# Linux: sudo apt-get install openjdk-17-jdk
```

### 1.3 安装 Maven

```bash
# 检查 Maven 版本
mvn -version

# 如果未安装
# Windows: 下载并配置环境变量
# Mac: brew install maven
# Linux: sudo apt-get install maven
```

### 1.4 克隆项目

```bash
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer
```

### 1.5 编译项目

```bash
# 编译
mvn clean compile -DskipTests

# 验证编译成功
# 输出应显示: [INFO] BUILD SUCCESS
```

---

## 阶段2: 项目配置

### 2.1 配置 AI 服务

AI-Reviewer 支持多种 AI 服务商，至少配置一个：

#### 选项 1: DeepSeek（推荐，性价比高）

编辑 `src/main/resources/config.yaml`:

```yaml
aiService:
  provider: "deepseek"
  apiKey: "sk-your-deepseek-api-key"
  baseUrl: "https://api.deepseek.com/v1/chat/completions"
  model: "deepseek-chat"
  maxTokens: 8000
  temperature: 0.3
  maxConcurrency: 20  # 黑客松模式推荐高并发
```

#### 选项 2: OpenAI

```yaml
aiService:
  provider: "openai"
  apiKey: "sk-your-openai-api-key"
  baseUrl: "https://api.openai.com/v1/chat/completions"
  model: "gpt-4"
  maxTokens: 8000
  temperature: 0.3
  maxConcurrency: 10
```

#### 选项 3: AWS Bedrock

```yaml
aiService:
  provider: "bedrock"
  apiKey: "ACCESS_KEY:SECRET_KEY"  # 或使用环境变量
  region: "us-east-1"
  model: "anthropic.claude-v2"
  maxTokens: 4000
  temperature: 0.3
  maxConcurrency: 5
```

### 2.2 配置黑客松参数

编辑 `src/main/resources/hackathon-config.yaml`:

```yaml
hackathon:
  # 快速模式（适合大量项目）
  fastMode: true
  
  # 评分维度权重
  dimensionWeights:
    innovation: 0.25      # 创新性 25%
    practicality: 0.20    # 实用性 20%
    codeQuality: 0.20     # 代码质量 20%
    completeness: 0.15    # 完成度 15%
    documentation: 0.10   # 文档质量 10%
    presentation: 0.10    # 展示效果 10%
  
  # 自动评分阈值
  autoJudging:
    enabled: true
    thresholds:
      excellent: 85   # 优秀
      good: 70        # 良好
      fair: 50        # 及格
  
  # 批量处理配置
  batchProcessing:
    enabled: true
    maxConcurrentProjects: 5
    projectTimeout: 300000  # 5分钟超时
```

### 2.3 配置缓存（可选，提升效率）

```yaml
cache:
  enabled: true
  type: "file"
  ttlHours: 24
  maxSize: 1000
  
  fileCache:
    baseDir: "${user.home}/.ai-reviewer-cache"
    compression: true
```

---

## 阶段3: 参赛项目收集

### 3.1 收集项目信息

创建项目列表文件 `projects.txt`:

```
https://github.com/team1/project1|Team Alpha|张三,李四,王五
https://github.com/team2/project2|Team Beta|赵六,孙七
https://gitee.com/team3/project3|Team Gamma|周八,吴九
```

格式: `仓库URL|团队名|成员列表`

### 3.2 验证项目可访问性

```bash
# 测试单个项目是否可访问
git ls-remote https://github.com/team1/project1

# 批量验证（使用脚本）
while IFS='|' read -r url team members; do
  echo "验证: $team - $url"
  git ls-remote "$url" > /dev/null 2>&1 && echo "✅ OK" || echo "❌ FAILED"
done < projects.txt
```

---

## 阶段4: 自动评审

### 4.1 单项目评审

#### 方式 1: 命令行（推荐）

**评审 GitHub 项目**:
```bash
java -jar ai-reviewer.jar hackathon \
  --github-url https://github.com/team1/project1 \
  --team "Team Alpha" \
  --members "张三,李四,王五" \
  --output team1-score.json \
  --report team1-report.md
```

**评审 Gitee 项目**:
```bash
java -jar ai-reviewer.jar hackathon \
  --gitee-url https://gitee.com/team1/project1 \
  --team "Team Alpha" \
  --members "张三,李四,王五" \
  --output team1-score.json \
  --report team1-report.md
```

**参数说明**:
- `--github-url`: GitHub 仓库 URL（与 `--gitee-url` 二选一）
- `--gitee-url`: Gitee 仓库 URL（与 `--github-url` 二选一）
- `--team`: 团队名称
- `--members`: 成员列表（逗号分隔）
- `--output`: 评分结果输出文件（JSON 格式）
- `--report`: 详细报告输出文件（Markdown 格式）
- `--fast-mode`: 启用快速模式（可选）

#### 方式 2: Java 代码

```java
import top.yumbo.ai.reviewer.adapter.input.hackathon.adapter.output.github.GitHubAdapter;
import top.yumbo.ai.reviewer.adapter.output.filesystem.LocalFileSystemAdapter;
import top.yumbo.ai.reviewer.application.hackathon.service.*;

// 1. 初始化适配器
GitHubAdapter githubAdapter = new GitHubAdapter(workDir);
LocalFileSystemAdapter fileSystemAdapter = new LocalFileSystemAdapter(config);

// 2. 克隆项目
String repoUrl = "https://github.com/team1/project1";
Path localPath = githubAdapter.cloneRepository(repoUrl, "main");

// 3. 扫描项目文件
Project project = fileSystemAdapter.scanProject(localPath);

// 4. 创建黑客松项目
Team team = Team.builder()
    .name("Team Alpha")
    .members(List.of(
        new Participant("张三", "zhangsan@example.com"),
        new Participant("李四", "lisi@example.com")
    ))
    .build();

HackathonProject hackathonProject = HackathonProject.builder()
    .name("project1")
    .team(team)
    .repositoryUrl(repoUrl)
    .build();

// 5. 分析和评分
HackathonAnalysisService analysisService = new HackathonAnalysisService(
    aiService, fileSystemAdapter
);
HackathonProject result = analysisService.analyzeProject(
    hackathonProject, project
);

// 6. 获取评分
HackathonScore score = result.getLatestSubmission().getScore();
System.out.println("总分: " + score.getTotalScore());
System.out.println("创新性: " + score.getInnovationScore());
System.out.println("实用性: " + score.getPracticalityScore());
System.out.println("代码质量: " + score.getCodeQualityScore());
```

### 4.2 批量评审

#### 方式 1: 批处理脚本（推荐）

创建 `batch-review.sh`:

```bash
#!/bin/bash

# 读取项目列表
while IFS='|' read -r url team members; do
  echo "========================================="
  echo "评审项目: $team"
  echo "仓库: $url"
  echo "========================================="
  
  # 提取项目名
  project_name=$(basename "$url" .git)
  
  # 判断是 GitHub 还是 Gitee
  if [[ "$url" == *"github.com"* ]]; then
    url_param="--github-url"
  elif [[ "$url" == *"gitee.com"* ]]; then
    url_param="--gitee-url"
  else
    echo "❌ 不支持的仓库平台: $url"
    continue
  fi
  
  # 执行评审
  java -jar ai-reviewer.jar hackathon \
    $url_param "$url" \
    --team "$team" \
    --members "$members" \
    --output "scores/${project_name}-score.json" \
    --report "reports/${project_name}-report.md" \
    --fast-mode
  
  # 检查结果
  if [ $? -eq 0 ]; then
    echo "✅ $team 评审完成"
  else
    echo "❌ $team 评审失败"
  fi
  
  echo ""
done < projects.txt

echo "所有项目评审完成！"
```

运行批量评审:

```bash
chmod +x batch-review.sh
./batch-review.sh
```

#### 方式 2: Java 批量处理

```java
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class BatchReview {
    public static void main(String[] args) throws Exception {
        // 读取项目列表
        List<String> lines = Files.readAllLines(
            Paths.get("projects.txt")
        );
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<HackathonScore>> futures = new ArrayList<>();
        
        for (String line : lines) {
            String[] parts = line.split("\\|");
            String url = parts[0];
            String teamName = parts[1];
            String members = parts[2];
            
            // 异步评审
            CompletableFuture<HackathonScore> future = 
                CompletableFuture.supplyAsync(() -> {
                    return reviewProject(url, teamName, members);
                }, executor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();
        
        // 收集结果
        List<HackathonScore> scores = new ArrayList<>();
        for (CompletableFuture<HackathonScore> future : futures) {
            scores.add(future.get());
        }
        
        // 生成排行榜
        generateLeaderboard(scores);
        
        executor.shutdown();
    }
    
    private static HackathonScore reviewProject(
        String url, String teamName, String members
    ) {
        // 实现评审逻辑
        // ...
        return score;
    }
    
    private static void generateLeaderboard(List<HackathonScore> scores) {
        // 排序
        scores.sort((a, b) -> 
            Integer.compare(b.getTotalScore(), a.getTotalScore())
        );
        
        // 生成排行榜
        System.out.println("========== 黑客松排行榜 ==========");
        int rank = 1;
        for (HackathonScore score : scores) {
            System.out.printf("%d. %s - %d分\n", 
                rank++, score.getTeamName(), score.getTotalScore()
            );
        }
    }
}
```

### 4.3 实时监控进度

```bash
# 监控评审进度
watch -n 5 'ls scores/*.json | wc -l'

# 查看最新评分
tail -f logs/hackathon-review.log

# 查看失败项目
grep "ERROR" logs/hackathon-review.log
```

---

## 阶段5: 结果导出

### 5.1 生成排行榜

#### 自动生成（Java）

```java
// 使用 LeaderboardService
LeaderboardService leaderboardService = new LeaderboardService();
List<HackathonProject> projects = loadAllProjects();

Leaderboard leaderboard = leaderboardService.generateLeaderboard(
    projects,
    List.of("totalScore", "innovationScore", "codeQualityScore")
);

// 导出为 Markdown
String markdown = leaderboardService.exportToMarkdown(leaderboard);
Files.writeString(Paths.get("leaderboard.md"), markdown);

// 导出为 JSON
String json = leaderboardService.exportToJson(leaderboard);
Files.writeString(Paths.get("leaderboard.json"), json);

// 导出为 HTML
String html = leaderboardService.exportToHtml(leaderboard);
Files.writeString(Paths.get("leaderboard.html"), html);
```

#### 手动生成（Python 脚本）

创建 `generate_leaderboard.py`:

```python
import json
import glob
from operator import itemgetter

# 读取所有评分文件
scores = []
for score_file in glob.glob('scores/*.json'):
    with open(score_file, 'r', encoding='utf-8') as f:
        score = json.load(f)
        scores.append(score)

# 按总分排序
scores.sort(key=itemgetter('totalScore'), reverse=True)

# 生成 Markdown 排行榜
with open('leaderboard.md', 'w', encoding='utf-8') as f:
    f.write('# 黑客松排行榜\n\n')
    f.write('| 排名 | 团队名 | 总分 | 创新性 | 实用性 | 代码质量 |\n')
    f.write('|------|--------|------|--------|--------|----------|\n')
    
    for rank, score in enumerate(scores, 1):
        f.write(f"| {rank} | {score['teamName']} | "
                f"{score['totalScore']} | "
                f"{score['dimensions']['innovation']} | "
                f"{score['dimensions']['practicality']} | "
                f"{score['dimensions']['codeQuality']} |\n")

print('排行榜已生成: leaderboard.md')
```

运行:

```bash
python generate_leaderboard.py
```

### 5.2 生成详细报告

每个项目会生成详细的评审报告：

**示例报告结构**:

```markdown
# Team Alpha - 项目评审报告

## 基本信息
- **项目名称**: project1
- **团队**: Team Alpha
- **成员**: 张三、李四、王五
- **仓库**: https://github.com/team1/project1
- **评审时间**: 2025-11-12 14:30:00

## 评分总览
- **总分**: 85/100
- **排名**: #1

## 各维度评分
| 维度 | 得分 | 权重 | 加权分 |
|------|------|------|--------|
| 创新性 | 90 | 25% | 22.5 |
| 实用性 | 85 | 20% | 17.0 |
| 代码质量 | 80 | 20% | 16.0 |
| 完成度 | 85 | 15% | 12.75 |
| 文档质量 | 85 | 10% | 8.5 |
| 展示效果 | 80 | 10% | 8.0 |

## 详细分析

### 创新性（90分）⭐⭐⭐⭐⭐
**优点**:
- 采用了创新的算法设计
- 解决方案独特，具有原创性
- 技术选型新颖

**改进建议**:
- 可以考虑更多边界情况

### 代码质量（80分）⭐⭐⭐⭐
**优点**:
- 代码结构清晰
- 遵循最佳实践
- 单元测试覆盖率高

**问题**:
- 部分函数复杂度较高
- 缺少错误处理

### 架构设计
**评价**: 采用微服务架构，模块化设计合理...

## 关键发现
1. 项目整体完成度高
2. 技术栈选择合理
3. 文档完善

## 改进建议
1. 增加异常处理
2. 优化性能
3. 补充集成测试

## 总体评价
优秀的黑客松项目，创新性强，代码质量高...
```

### 5.3 导出 Excel 报表

创建 `export_excel.py`:

```python
import pandas as pd
import json
import glob

# 读取所有评分
data = []
for score_file in glob.glob('scores/*.json'):
    with open(score_file, 'r', encoding='utf-8') as f:
        score = json.load(f)
        data.append({
            '排名': 0,  # 稍后计算
            '团队名': score['teamName'],
            '项目名': score['projectName'],
            '总分': score['totalScore'],
            '创新性': score['dimensions']['innovation'],
            '实用性': score['dimensions']['practicality'],
            '代码质量': score['dimensions']['codeQuality'],
            '完成度': score['dimensions']['completeness'],
            '文档质量': score['dimensions']['documentation'],
        })

# 创建 DataFrame
df = pd.DataFrame(data)

# 排序并计算排名
df = df.sort_values('总分', ascending=False)
df['排名'] = range(1, len(df) + 1)

# 导出 Excel
df.to_excel('hackathon_results.xlsx', index=False)
print('Excel 报表已生成: hackathon_results.xlsx')
```

---

## 🔧 配置详解

### AI 服务配置参数

| 参数 | 说明 | 默认值 | 推荐值（黑客松） |
|------|------|--------|------------------|
| `provider` | AI 提供商 | - | `deepseek` |
| `apiKey` | API 密钥 | - | 必填 |
| `model` | 模型名称 | - | `deepseek-chat` |
| `maxTokens` | 最大 Token 数 | 4000 | 8000 |
| `temperature` | 温度（创造性） | 0.7 | 0.3（更确定） |
| `maxConcurrency` | 最大并发数 | 3 | 20（快速评审） |
| `connectTimeout` | 连接超时（毫秒） | 30000 | 300000 |
| `readTimeout` | 读取超时（毫秒） | 60000 | 60000 |

### 黑客松配置参数

| 参数 | 说明 | 默认值 | 建议 |
|------|------|--------|------|
| `fastMode` | 快速模式 | false | true（大量项目） |
| `dimensionWeights.*` | 各维度权重 | 均衡 | 按需调整 |
| `autoJudging.enabled` | 自动评级 | true | true |
| `batchProcessing.maxConcurrentProjects` | 批量并发数 | 3 | 5-10 |
| `projectTimeout` | 单项目超时 | 300000 | 300000-600000 |

### 评分维度权重调整

根据黑客松主题调整权重：

#### 技术创新主题
```yaml
dimensionWeights:
  innovation: 0.40      # 强调创新
  codeQuality: 0.25     
  practicality: 0.15    
  completeness: 0.10
  documentation: 0.05
  presentation: 0.05
```

#### 商业应用主题
```yaml
dimensionWeights:
  practicality: 0.35    # 强调实用
  innovation: 0.20
  completeness: 0.20
  codeQuality: 0.15
  presentation: 0.10
  documentation: 0.00
```

#### 教育培训主题
```yaml
dimensionWeights:
  documentation: 0.30   # 强调文档
  codeQuality: 0.25
  practicality: 0.20
  completeness: 0.15
  innovation: 0.10
  presentation: 0.00
```

---

## 📊 评分标准

### 评分维度详解

#### 1. 创新性（Innovation）
**评分要点**:
- 解决方案的原创性（40%）
- 技术选型的新颖性（30%）
- 问题解决的独特性（30%）

**评分标准**:
- 90-100分: 突破性创新，技术方案独特
- 80-89分: 有明显创新点，技术应用新颖
- 70-79分: 有一定创新，但不够突出
- 60-69分: 创新性一般，多为常规实现
- <60分: 缺乏创新，基本照搬现有方案

#### 2. 实用性（Practicality）
**评分要点**:
- 解决实际问题的能力（50%）
- 用户体验设计（30%）
- 商业价值潜力（20%）

**评分标准**:
- 90-100分: 解决重大实际问题，商业价值高
- 80-89分: 实用性强，有明确应用场景
- 70-79分: 有一定实用性，但场景有限
- 60-69分: 实用性一般，偏理论或演示
- <60分: 缺乏实用性，难以落地

#### 3. 代码质量（Code Quality）
**评分要点**:
- 代码结构和架构（30%）
- 代码规范和风格（25%）
- 测试覆盖率（20%）
- 性能优化（15%）
- 安全性（10%）

**评分标准**:
- 90-100分: 代码优雅，架构合理，测试完善
- 80-89分: 代码质量良好，结构清晰
- 70-79分: 代码可读，有基本测试
- 60-69分: 代码能运行，但质量一般
- <60分: 代码混乱，缺少测试，有明显问题

#### 4. 完成度（Completeness）
**评分要点**:
- 功能完整性（50%）
- 项目稳定性（30%）
- 部署可用性（20%）

**评分标准**:
- 90-100分: 功能完整，稳定可用，可直接部署
- 80-89分: 核心功能完整，基本稳定
- 70-79分: 主要功能完成，有些小bug
- 60-69分: 部分功能完成，不够稳定
- <60分: 功能不完整，无法正常使用

#### 5. 文档质量（Documentation）
**评分要点**:
- README 完整性（40%）
- 代码注释质量（30%）
- API 文档（20%）
- 部署说明（10%）

**评分标准**:
- 90-100分: 文档完善，注释详细，易于理解
- 80-89分: 文档完整，有必要的说明
- 70-79分: 有基本文档，但不够详细
- 60-69分: 文档简陋，缺少关键信息
- <60分: 几乎没有文档

#### 6. 展示效果（Presentation）
**评分要点**:
- UI/UX 设计（50%）
- 演示效果（30%）
- 项目演示文档（20%）

**评分标准**:
- 90-100分: 界面精美，演示效果出色
- 80-89分: 界面美观，演示流畅
- 70-79分: 界面简洁，演示基本完整
- 60-69分: 界面一般，演示不够流畅
- <60分: 界面简陋，演示效果差

### 总分计算公式

```
总分 = Σ (维度得分 × 维度权重)
```

示例:
```
总分 = 创新性(90) × 0.25 + 实用性(85) × 0.20 + 代码质量(80) × 0.20 
     + 完成度(85) × 0.15 + 文档质量(85) × 0.10 + 展示效果(80) × 0.10
     = 22.5 + 17.0 + 16.0 + 12.75 + 8.5 + 8.0
     = 84.75 ≈ 85分
```

---

## 🔍 故障排查

### 常见问题

#### 1. 无法克隆 GitHub/Gitee 仓库

**问题**: `Authentication is required but no CredentialsProvider has been registered`

**解决方案**:
```bash
# 方案1: 使用 HTTPS + Token
export GITHUB_TOKEN=ghp_xxxxx
git config --global credential.helper store

# 方案2: 使用 SSH
ssh-keygen -t rsa -b 4096
cat ~/.ssh/id_rsa.pub  # 添加到 GitHub/Gitee

# 方案3: 在 URL 中包含 Token
https://token@github.com/user/repo.git
```

#### 2. AI 服务调用失败

**问题**: `Failed to connect to API`

**解决方案**:
```bash
# 检查 API Key
echo $AI_API_KEY

# 检查网络连接
curl https://api.deepseek.com/v1

# 检查配额
# 登录 AI 服务商后台查看配额使用情况

# 增加重试次数和超时时间
aiService:
  maxRetries: 5
  retryDelay: 2000
  connectTimeout: 600000
```

#### 3. 内存不足

**问题**: `Java heap space OutOfMemoryError`

**解决方案**:
```bash
# 增加 JVM 内存
export MAVEN_OPTS="-Xmx4G -Xms2G"

# 或在运行时指定
java -Xmx4G -Xms2G -jar ai-reviewer.jar ...

# 减少并发数
batchProcessing:
  maxConcurrentProjects: 2
```

#### 4. 项目太大，分析超时

**问题**: `Project analysis timeout`

**解决方案**:
```yaml
# 增加超时时间
hackathon:
  batchProcessing:
    projectTimeout: 600000  # 10分钟

# 启用快速模式
hackathon:
  fastMode: true

# 限制扫描的文件
fileScan:
  maxFileSize: 5242880  # 5MB
  maxTotalFiles: 10000
```

---

## 💡 最佳实践

### 1. 黑客松前准备

#### 一周前
- [ ] 测试评审系统
- [ ] 配置 AI 服务
- [ ] 准备评分标准
- [ ] 测试网络连接

#### 三天前
- [ ] 收集参赛项目信息
- [ ] 验证项目可访问性
- [ ] 准备批处理脚本
- [ ] 测试批量评审

#### 一天前
- [ ] 最终测试
- [ ] 准备排行榜模板
- [ ] 准备报告模板
- [ ] 备份配置文件

### 2. 评审流程优化

#### 分批评审
```bash
# 将项目分成多批，逐批评审
batch1=(project1 project2 project3)
batch2=(project4 project5 project6)

# 评审第一批
for project in "${batch1[@]}"; do
  review_project "$project"
done

# 检查结果，再评审第二批
```

#### 失败重试
```bash
# 记录失败的项目
failed_projects=()

# 重试失败的项目
for project in "${failed_projects[@]}"; do
  echo "重试: $project"
  review_project "$project" || echo "$project" >> failed.txt
done
```

### 3. 结果验证

#### 检查评分合理性
```python
import json
import statistics

scores = []
for file in glob.glob('scores/*.json'):
    with open(file) as f:
        score = json.load(f)
        scores.append(score['totalScore'])

print(f"平均分: {statistics.mean(scores)}")
print(f"中位数: {statistics.median(scores)}")
print(f"标准差: {statistics.stdev(scores)}")
print(f"最高分: {max(scores)}")
print(f"最低分: {min(scores)}")

# 检查异常值
for score in scores:
    if score > 95 or score < 30:
        print(f"异常分数: {score}")
```

### 4. 性能优化

#### 并发控制
- 小型黑客松（<20个项目）: maxConcurrent = 3-5
- 中型黑客松（20-50个项目）: maxConcurrent = 5-10
- 大型黑客松（>50个项目）: maxConcurrent = 10-20

#### 资源分配
```
项目数量    推荐配置
1-10       4GB 内存, 2 CPU
11-30      8GB 内存, 4 CPU
31-50      16GB 内存, 8 CPU
50+        32GB 内存, 16 CPU
```

### 5. 评审公平性

#### 多次评审取平均
```java
// 对每个项目评审3次，取平均分
List<Integer> scores = new ArrayList<>();
for (int i = 0; i < 3; i++) {
    HackathonScore score = analyzeProject(project);
    scores.add(score.getTotalScore());
}
int avgScore = (int) scores.stream()
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0);
```

#### 去除极端值
```java
// 评审5次，去掉最高和最低分
List<Integer> scores = new ArrayList<>();
for (int i = 0; i < 5; i++) {
    scores.add(analyzeProject(project).getTotalScore());
}
scores.sort(Integer::compareTo);
// 去除首尾
scores.remove(0);
scores.remove(scores.size() - 1);
int avgScore = (int) scores.stream()
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0);
```

---

## 📞 技术支持

### 遇到问题？

1. **查看文档**: [完整文档](https://github.com/jinhua10/ai-reviewer/wiki)
2. **提交 Issue**: [GitHub Issues](https://github.com/jinhua10/ai-reviewer/issues)
3. **社区讨论**: [Discussions](https://github.com/jinhua10/ai-reviewer/discussions)
4. **邮件支持**: support@yumbo.top

### 贡献代码

欢迎贡献代码和改进建议！

```bash
# Fork 项目
git clone https://github.com/your-username/ai-reviewer.git

# 创建分支
git checkout -b feature/hackathon-improvements

# 提交改进
git commit -m "feat: improve hackathon scoring"
git push origin feature/hackathon-improvements

# 创建 Pull Request
```

---

## 📝 附录

### A. 评分模板

#### 创新性评分模板
```markdown
### 创新性评估

**技术创新** (40分):
- [ ] 使用了新技术/新框架 (0-15分)
- [ ] 算法/架构有创新 (0-15分)
- [ ] 实现方式独特 (0-10分)

**解决方案创新** (30分):
- [ ] 问题定义独特 (0-10分)
- [ ] 解决思路新颖 (0-15分)
- [ ] 用户体验创新 (0-5分)

**原创性** (30分):
- [ ] 非常规解决方案 (0-15分)
- [ ] 独立思考和实现 (0-15分)

**总分**: ___/100
```

### B. 常用命令速查

```bash
# 快速评审 GitHub 项目
java -jar ai-reviewer.jar hackathon --github-url URL --team NAME

# 快速评审 Gitee 项目
java -jar ai-reviewer.jar hackathon --gitee-url URL --team NAME

# 批量评审
./batch-review.sh

# 生成排行榜
python generate_leaderboard.py

# 查看进度
watch -n 5 'ls scores/*.json | wc -l'

# 导出 Excel
python export_excel.py

# 清理缓存
rm -rf ~/.ai-reviewer-cache
```

### C. 配置文件模板

完整的配置文件模板请参考:
- [config.yaml](src/main/resources/config.yaml)
- [hackathon-config.yaml](src/main/resources/hackathon-config.yaml)

---

<div align="center">

**祝您的黑客松活动圆满成功！** 🎉

Made with ❤️ by AI-Reviewer Team

[返回顶部](#黑客松项目评审完整指南)

</div>

