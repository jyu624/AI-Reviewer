# 🏆 黑客松项目评审完整指南

<div align="center">

**AI-Reviewer 黑客松模式 - 从零到专家**

支持 GitHub & Gitee | 多AI服务 | 自动评分 | 实时排行榜 | 详细报告

[快速开始](#-5分钟快速开始) · [详细教程](#-详细教程) · [配置说明](#-配置说明) · [常见问题](#-常见问题)

</div>

---

## 📖 目录

- [5分钟快速开始](#-5分钟快速开始)
- [详细教程](#-详细教程)
  - [阶段1: 环境准备](#阶段1-环境准备)
  - [阶段2: 配置AI服务](#阶段2-配置ai服务)
  - [阶段3: 评审第一个项目](#阶段3-评审第一个项目)
  - [阶段4: 批量评审](#阶段4-批量评审)
  - [阶段5: 查看和导出结果](#阶段5-查看和导出结果)
- [命令详解](#-命令详解)
- [配置说明](#-配置说明)
- [评分标准](#-评分标准)
- [常见问题](#-常见问题)
- [故障排查](#-故障排查)
- [最佳实践](#-最佳实践)

---

## 🚀 5分钟快速开始

### 前置条件
- ✅ Java 17 或更高版本
- ✅ Maven 3.8+
- ✅ AI服务API密钥（DeepSeek/OpenAI/Gemini等任一）

### 快速上手

```bash
# 1. 克隆并编译项目
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer
mvn clean package -DskipTests

# 2. 配置AI服务（选择其中一种）
# 方式A: 使用环境变量（推荐）
export AI_PROVIDER=deepseek
export AI_API_KEY=your-deepseek-api-key

# 方式B: 编辑配置文件
vim src/main/resources/config.yaml

# 3. 评审一个GitHub项目
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/spring-projects/spring-boot \
  --team "Spring Team" \
  --output score.json \
  --report report.md

# 4. 查看结果
cat score.json
cat report.md
```

### 快速评分示例输出

```json
{
  "projectName": "spring-boot",
  "teamName": "Spring Team",
  "overallScore": 92,
  "grade": "A+",
  "dimensions": {
    "codeQuality": 95,
    "innovation": 88,
    "completeness": 93,
    "documentation": 90
  },
  "totalScore": 92,
  "analysisTime": "2025-11-13T10:30:00",
  "summary": "优秀的企业级框架，代码质量高，架构清晰..."
}
```

---

## 📚 详细教程

## 阶段1: 环境准备

### 1.1 检查系统要求

| 组件 | 最低要求 | 推荐配置 | 检查命令 |
|------|---------|---------|---------|
| **Java** | 17+ | 21+ | `java -version` |
| **Maven** | 3.8+ | 3.9+ | `mvn -version` |
| **内存** | 4GB | 8GB+ | - |
| **磁盘** | 10GB | 20GB+ | - |
| **网络** | 能访问 GitHub/Gitee | 稳定高速 | - |

### 1.2 安装Java（如果未安装）

<details>
<summary><b>Windows</b></summary>

```bash
# 1. 下载 Java 17+ JDK
# 访问: https://www.oracle.com/java/technologies/downloads/

# 2. 安装后配置环境变量
# JAVA_HOME = C:\Program Files\Java\jdk-17
# Path 添加 %JAVA_HOME%\bin

# 3. 验证安装
java -version
```
</details>

<details>
<summary><b>macOS</b></summary>

```bash
# 使用 Homebrew 安装
brew install openjdk@17

# 验证安装
java -version
```
</details>

<details>
<summary><b>Linux (Ubuntu/Debian)</b></summary>

```bash
# 安装 OpenJDK 17
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# 验证安装
java -version
```
</details>

### 1.3 安装Maven（如果未安装）

<details>
<summary><b>Windows</b></summary>

```bash
# 1. 下载 Maven
# 访问: https://maven.apache.org/download.cgi

# 2. 解压并配置环境变量
# MAVEN_HOME = C:\Program Files\apache-maven-3.9.6
# Path 添加 %MAVEN_HOME%\bin

# 3. 验证安装
mvn -version
```
</details>

<details>
<summary><b>macOS/Linux</b></summary>

```bash
# macOS
brew install maven

# Linux
sudo apt-get install maven

# 验证安装
mvn -version
```
</details>

### 1.4 克隆项目

```bash
# 克隆AI-Reviewer项目
git clone https://github.com/jinhua10/ai-reviewer.git
cd ai-reviewer

# 查看项目结构
ls -la
```

### 1.5 编译项目

```bash
# 清理并编译
mvn clean package -DskipTests

# 等待编译完成，看到如下输出表示成功：
# [INFO] BUILD SUCCESS
# [INFO] Total time: 15.xxx s

# 验证JAR文件生成
ls -lh target/ai-reviewer-2.0.jar
```

---

## 阶段2: 配置AI服务

### 2.1 为什么需要配置AI服务？

AI-Reviewer 使用**大语言模型（LLM）**来分析代码质量、评估创新性和生成评审报告。您需要选择并配置一个AI服务提供商。

### 2.2 支持的AI服务

| AI服务 | 优势 | 费用 | 推荐场景 |
|--------|------|------|---------|
| **DeepSeek** | 🌟 性价比最高 | ¥0.001/1K tokens | 大规模评审 |
| **OpenAI** | 🎯 最强理解能力 | $0.01/1K tokens | 高质量分析 |
| **Gemini** | 🔥 免费额度大 | 免费/付费混合 | 预算有限 |
| **Claude** | 📖 长文本处理 | $0.008/1K tokens | 大型项目 |
| **AWS Bedrock** | 🏢 企业级 | 按需付费 | 企业部署 |

### 2.3 配置方式详解

#### 🎯 方式1: 使用环境变量（推荐）

**优点**：安全、灵活、不修改代码

<details>
<summary><b>配置 DeepSeek（推荐）</b></summary>

```bash
# Linux/macOS (临时，当前终端有效)
export AI_PROVIDER=deepseek
export AI_API_KEY=sk-your-deepseek-api-key-here

# Linux/macOS (永久，添加到 ~/.bashrc 或 ~/.zshrc)
echo 'export AI_PROVIDER=deepseek' >> ~/.bashrc
echo 'export AI_API_KEY=sk-your-deepseek-api-key-here' >> ~/.bashrc
source ~/.bashrc

# Windows (临时)
set AI_PROVIDER=deepseek
set AI_API_KEY=sk-your-deepseek-api-key-here

# Windows (永久，使用系统环境变量设置)
# 1. 右键"此电脑" -> 属性 -> 高级系统设置 -> 环境变量
# 2. 新建用户变量:
#    变量名: AI_PROVIDER  值: deepseek
#    变量名: AI_API_KEY   值: sk-your-deepseek-api-key-here
```

**获取 DeepSeek API Key**：
1. 访问 https://platform.deepseek.com/
2. 注册并登录
3. 在"API Keys"页面创建新密钥
4. 复制密钥（格式：sk-xxxxxxxxx）

</details>

<details>
<summary><b>配置 OpenAI</b></summary>

```bash
# 设置环境变量
export AI_PROVIDER=openai
export AI_API_KEY=sk-your-openai-api-key-here

# 可选：指定模型
export AI_MODEL=gpt-4  # 或 gpt-3.5-turbo
```

**获取 OpenAI API Key**：
1. 访问 https://platform.openai.com/
2. 登录并进入 API Keys 页面
3. 创建新密钥
</details>

<details>
<summary><b>配置 Gemini</b></summary>

```bash
# 设置环境变量
export AI_PROVIDER=gemini
export AI_API_KEY=your-gemini-api-key-here
```

**获取 Gemini API Key**：
1. 访问 https://makersuite.google.com/app/apikey
2. 创建新项目并获取API密钥
</details>

#### 🎯 方式2: 修改配置文件

**优点**：集中管理、可版本控制（注意不要提交密钥）

```bash
# 编辑配置文件
vim src/main/resources/config.yaml
```

**配置 DeepSeek 示例**：
```yaml
# AI服务配置
aiService:
  provider: "deepseek"  # 指定AI服务商
  apiKey: "sk-your-deepseek-api-key-here"  # 您的API密钥
  baseUrl: "https://api.deepseek.com/v1/chat/completions"  # API地址
  model: "deepseek-chat"  # 模型名称
  maxTokens: 8000  # 最大token数
  temperature: 0.7  # 创造性(0-1)，0.7适合代码评审
  
  # 超时配置（毫秒）
  connectTimeout: 300000    # 连接超时: 5分钟
  readTimeout: 60000        # 读取超时: 1分钟
  analyzeTimeout: 300000    # 分析超时: 5分钟
  
  maxRetries: 2             # 失败重试次数
  maxConcurrency: 20        # 最大并发数
```

**配置 OpenAI 示例**：
```yaml
aiService:
  provider: "openai"
  apiKey: "sk-your-openai-api-key-here"
  baseUrl: "https://api.openai.com/v1/chat/completions"
  model: "gpt-4"  # 或 gpt-3.5-turbo
  maxTokens: 8000
  temperature: 0.7
```

**配置 Gemini 示例**：
```yaml
aiService:
  provider: "gemini"
  apiKey: "your-gemini-api-key-here"
  baseUrl: "https://generativelanguage.googleapis.com/v1"
  model: "gemini-pro"
  maxTokens: 8000
  temperature: 0.7
```

### 2.4 验证配置

```bash
# 运行一个简单的测试
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.adapter.input.cli.CommandLineAdapter \
  --help

# 如果看到帮助信息且没有错误，说明配置成功
```

---

## 阶段3: 评审第一个项目

### 3.1 理解命令结构

黑客松评审命令的基本结构：

```bash
java -cp <JAR文件> <主类名> <参数选项>
```

**完整示例**：
```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/project \
  --team "Team Name" \
  --output score.json \
  --report report.md
```

### 3.2 命令详解

让我们详细解释每个部分：

#### 📦 `java -cp target/ai-reviewer-2.0.jar`

**含义**：使用Java运行程序，`-cp`（classpath）指定JAR文件位置

**详解**：
- `java`：Java虚拟机命令
- `-cp`：类路径（Class Path）参数，告诉Java去哪里找程序
- `target/ai-reviewer-2.0.jar`：编译后的JAR文件路径

#### 🎯 `top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp`

**含义**：黑客松评审应用的主类（入口点）

**详解**：
- 这是Java类的全限定名（包名 + 类名）
- 这个类包含 `main()` 方法，是程序的入口
- 负责：解析参数 → 克隆项目 → 分析代码 → 生成报告

#### 🌐 `--github-url <URL>` 或 `--gitee-url <URL>`

**含义**：指定要评审的Git仓库地址

**详解**：
- **从GitHub克隆**：`--github-url https://github.com/user/project`
- **从Gitee克隆**：`--gitee-url https://gitee.com/user/project`
- 程序会自动：
  1. 克隆仓库到临时目录
  2. 扫描所有源代码文件
  3. 分析完成后自动清理临时文件

**支持的URL格式**：
```bash
# HTTPS (推荐，无需配置SSH密钥)
--github-url https://github.com/username/repository

# SSH (需要配置SSH密钥)
--github-url git@github.com:username/repository.git
```

#### 📁 `--directory <路径>` 或 `-d <路径>`

**含义**：使用本地项目目录，不从Git克隆

**详解**：
- 适用场景：
  - ✅ 项目已在本地
  - ✅ 私有项目无法通过URL访问
  - ✅ 需要评审本地修改（未提交）
  
**示例**：
```bash
# 使用绝对路径
--directory /home/user/projects/my-hackathon-project

# 使用相对路径
--directory ./my-hackathon-project

# Windows路径
--directory C:\Users\user\projects\my-hackathon-project
```

#### 👥 `--team "Team Name"` 或 `-t "Team Name"`

**含义**：指定参赛团队名称

**详解**：
- 用于在报告中标识团队
- 用于生成排行榜
- **注意**：如果团队名称包含空格，必须用引号括起来

**示例**：
```bash
--team "Team Awesome"       # ✅ 正确：有空格，用引号
--team TeamAwesome          # ✅ 正确：无空格，可不用引号
--team Team Awesome         # ❌ 错误：有空格但没引号
```

#### 🌿 `--branch <分支名>` 或 `-b <分支名>`

**含义**：指定要克隆的Git分支（可选）

**详解**：
- 默认值：`main`
- 其他常见分支：`master`、`develop`、`feature/xxx`

**示例**：
```bash
--branch develop            # 克隆develop分支
--branch feature/new-ui     # 克隆特定feature分支
```

#### 💾 `--output <文件>` 或 `-o <文件>`

**含义**：指定JSON格式的评分结果输出文件（可选）

**详解**：
- 生成结构化的评分数据（JSON格式）
- 适合程序处理、数据分析、批量导入

**输出示例** (`score.json`)：
```json
{
  "projectName": "my-project",
  "teamName": "Team Awesome",
  "overallScore": 85,
  "grade": "A",
  "dimensions": {
    "codeQuality": 88,
    "innovation": 82,
    "completeness": 85,
    "documentation": 80
  },
  "totalScore": 85,
  "analysisTime": "2025-11-13T10:30:00",
  "summary": "项目整体质量良好...",
  "suggestions": [
    "建议增加单元测试覆盖率",
    "部分代码可以优化性能"
  ]
}
```

#### 📝 `--report <文件>` 或 `-r <文件>`

**含义**：指定Markdown格式的详细报告输出文件（可选）

**详解**：
- 生成人类可读的详细报告（Markdown格式）
- 包含：评分细节、代码分析、改进建议、最佳实践

**输出示例** (`report.md`)：
```markdown
# 黑客松项目评审报告

## 项目信息
- **项目名称**: my-project
- **团队名称**: Team Awesome
- **评审时间**: 2025-11-13 10:30:00

## 总体评分: 85/100 (A)

## 各维度评分

### 代码质量: 88/100
- ✅ 代码结构清晰，符合SOLID原则
- ✅ 命名规范统一
- ⚠️ 部分方法过长，建议拆分

### 创新性: 82/100
- ✅ 采用了新颖的技术方案
- ✅ 用户体验设计独特

### 完整性: 85/100
- ✅ 核心功能完整
- ⚠️ 缺少错误处理机制

### 文档质量: 80/100
- ✅ README清晰
- ⚠️ 代码注释偏少

## 改进建议
1. 增加单元测试覆盖率（当前约30%）
2. 优化数据库查询性能
3. 补充API文档

## 亮点
- 架构设计清晰，易于扩展
- 用户界面美观，交互流畅
- 代码风格统一
```

### 3.3 完整工作流程

当您运行黑客松评审命令时，程序执行以下步骤：

```
1. 📥 克隆项目
   ↓
   程序从GitHub/Gitee克隆项目到临时目录
   (如果使用--directory，则直接使用本地目录)
   
2. 🔍 扫描文件
   ↓
   扫描所有源代码文件（.java, .py, .js等）
   过滤掉测试文件、第三方库、生成代码
   
3. 📊 构建项目模型
   ↓
   分析项目结构：目录树、文件类型、代码行数
   识别技术栈：框架、语言、工具
   
4. 🤖 AI分析
   ↓
   调用AI服务分析代码质量
   评估：架构、创新性、完整性、文档
   生成改进建议
   
5. 🎯 计算评分
   ↓
   代码质量 (40%)：复杂度、规范性、可维护性
   创新性 (30%)：技术方案、用户体验、商业价值
   完整性 (20%)：功能完整性、错误处理、边界情况
   文档质量 (10%)：README、注释、API文档
   
6. 📝 生成报告
   ↓
   生成JSON评分文件 (--output)
   生成Markdown详细报告 (--report)
   
7. 🧹 清理
   ↓
   删除临时克隆的项目文件
   (本地目录不会被删除)
```

### 3.4 实战示例

#### 示例1: 评审GitHub项目

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/spring-projects/spring-petclinic \
  --team "Spring Demo" \
  --output spring-petclinic-score.json \
  --report spring-petclinic-report.md
```

**执行过程**：
```
正在克隆项目: https://github.com/spring-projects/spring-petclinic
项目克隆完成: /tmp/hackathon-repos/spring-petclinic
正在扫描项目...
项目信息:
  - 团队: Spring Demo
  - 名称: spring-petclinic
  - 类型: Java
  - 文件数: 145
  - 代码行数: 12,543

正在分析项目...
✓ 代码结构分析完成
✓ 代码质量评估完成
✓ 创新性评估完成
✓ 完整性检查完成
✓ 文档质量评估完成

分析完成！

=== 黑客松评审结果 ===
团队: Spring Demo
项目: spring-petclinic
总分: 92/100 (A+)

维度评分:
  • 代码质量: 95/100
  • 创新性: 88/100
  • 完整性: 93/100
  • 文档质量: 90/100

评分结果已保存到: spring-petclinic-score.json
详细报告已保存到: spring-petclinic-report.md

分析耗时: 45,231 毫秒
```

#### 示例2: 评审Gitee项目

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --gitee-url https://gitee.com/dromara/hutool \
  --team "Hutool Team" \
  -o hutool-score.json
```

#### 示例3: 评审本地项目

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --directory /home/user/my-hackathon-project \
  --team "My Team" \
  --output my-project-score.json \
  --report my-project-report.md
```

#### 示例4: 指定分支评审

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/project \
  --branch develop \
  --team "Dev Team" \
  -o score.json
```

---

## 阶段4: 批量评审

### 4.1 准备项目列表

创建一个文本文件 `projects.txt`，每行一个项目：

```
Team A,https://github.com/teamA/project-a
Team B,https://github.com/teamB/project-b
Team C,https://gitee.com/teamC/project-c
Team D,/local/path/to/project-d
```

### 4.2 批量评审脚本

<details>
<summary><b>Linux/macOS 批量脚本</b></summary>

```bash
#!/bin/bash
# batch-review.sh

JAR_FILE="target/ai-reviewer-2.0.jar"
MAIN_CLASS="top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp"
RESULTS_DIR="./hackathon-results"

# 创建结果目录
mkdir -p "$RESULTS_DIR"

# 读取项目列表并评审
while IFS=',' read -r team url; do
    echo "========================================="
    echo "评审项目: $team"
    echo "========================================="
    
    # 生成输出文件名
    team_safe=$(echo "$team" | tr ' ' '_')
    output_file="$RESULTS_DIR/${team_safe}_score.json"
    report_file="$RESULTS_DIR/${team_safe}_report.md"
    
    # 判断URL类型并执行评审
    if [[ $url == http* ]]; then
        # Git URL
        if [[ $url == *github.com* ]]; then
            java -cp "$JAR_FILE" "$MAIN_CLASS" \
                --github-url "$url" \
                --team "$team" \
                --output "$output_file" \
                --report "$report_file"
        elif [[ $url == *gitee.com* ]]; then
            java -cp "$JAR_FILE" "$MAIN_CLASS" \
                --gitee-url "$url" \
                --team "$team" \
                --output "$output_file" \
                --report "$report_file"
        fi
    else
        # 本地目录
        java -cp "$JAR_FILE" "$MAIN_CLASS" \
            --directory "$url" \
            --team "$team" \
            --output "$output_file" \
            --report "$report_file"
    fi
    
    echo ""
done < projects.txt

echo "批量评审完成！结果保存在: $RESULTS_DIR"
```

**使用方法**：
```bash
chmod +x batch-review.sh
./batch-review.sh
```
</details>

<details>
<summary><b>Windows 批量脚本</b></summary>

```batch
@echo off
REM batch-review.bat

set JAR_FILE=target\ai-reviewer-2.0.jar
set MAIN_CLASS=top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp
set RESULTS_DIR=hackathon-results

REM 创建结果目录
if not exist "%RESULTS_DIR%" mkdir "%RESULTS_DIR%"

REM 读取项目列表并评审
for /F "tokens=1,2 delims=," %%A in (projects.txt) do (
    echo =========================================
    echo 评审项目: %%A
    echo =========================================
    
    set team=%%A
    set url=%%B
    set team_safe=%team: =_%
    set output_file=%RESULTS_DIR%\%team_safe%_score.json
    set report_file=%RESULTS_DIR%\%team_safe%_report.md
    
    REM 执行评审
    java -cp "%JAR_FILE%" %MAIN_CLASS% ^
        --github-url "%%B" ^
        --team "%%A" ^
        --output "%output_file%" ^
        --report "%report_file%"
    
    echo.
)

echo 批量评审完成！结果保存在: %RESULTS_DIR%
pause
```

**使用方法**：
```cmd
batch-review.bat
```
</details>

### 4.3 并发评审（高级）

利用GNU Parallel实现高速并发评审：

```bash
# 安装 GNU Parallel
# Ubuntu/Debian: sudo apt-get install parallel
# macOS: brew install parallel

# 并发评审（4个项目同时）
cat projects.txt | parallel -j 4 --colsep ',' \
  'java -cp target/ai-reviewer-2.0.jar \
   top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
   --github-url {2} --team {1} \
   --output results/{1//}_score.json \
   --report results/{1//}_report.md'
```

---

## 阶段5: 查看和导出结果

### 5.1 查看JSON评分

```bash
# 查看原始JSON
cat score.json

# 格式化查看（使用jq工具）
cat score.json | jq '.'

# 提取关键信息
cat score.json | jq '{team: .teamName, score: .overallScore, grade: .grade}'
```

### 5.2 查看Markdown报告

```bash
# 命令行查看
cat report.md

# 或使用Markdown预览工具
# VSCode: 右键 -> Open Preview
# Typora: 双击打开
```

### 5.3 生成排行榜

创建排行榜生成脚本 `generate-leaderboard.sh`：

```bash
#!/bin/bash
# generate-leaderboard.sh

echo "# 🏆 黑客松项目评审排行榜"
echo ""
echo "| 排名 | 团队 | 总分 | 等级 | 代码质量 | 创新性 | 完整性 | 文档 |"
echo "|------|------|------|------|----------|--------|--------|------|"

# 合并所有评分文件并排序
jq -s 'sort_by(-.overallScore) | to_entries | .[] | 
    [.key+1, .value.teamName, .value.overallScore, .value.grade, 
     .value.dimensions.codeQuality, .value.dimensions.innovation,
     .value.dimensions.completeness, .value.dimensions.documentation] | 
    "| \(.[0]) | \(.[1]) | \(.[2]) | \(.[3]) | \(.[4]) | \(.[5]) | \(.[6]) | \(.[7]) |"' \
    hackathon-results/*_score.json -r

echo ""
echo "生成时间: $(date)"
```

**使用方法**：
```bash
chmod +x generate-leaderboard.sh
./generate-leaderboard.sh > leaderboard.md
```

**输出示例** (`leaderboard.md`)：
```markdown
# 🏆 黑客松项目评审排行榜

| 排名 | 团队 | 总分 | 等级 | 代码质量 | 创新性 | 完整性 | 文档 |
|------|------|------|------|----------|--------|--------|------|
| 1 | Team Alpha | 95 | A+ | 98 | 93 | 94 | 90 |
| 2 | Team Beta | 92 | A+ | 94 | 90 | 91 | 88 |
| 3 | Team Gamma | 88 | A | 90 | 86 | 88 | 85 |
| 4 | Team Delta | 85 | A | 87 | 83 | 85 | 82 |

生成时间: 2025-11-13 14:30:00
```

### 5.4 导出Excel报告

使用Python脚本导出Excel：

```python
# export-to-excel.py
import json
import pandas as pd
from pathlib import Path

# 读取所有JSON文件
results_dir = Path('hackathon-results')
data = []

for json_file in results_dir.glob('*_score.json'):
    with open(json_file, 'r', encoding='utf-8') as f:
        score = json.load(f)
        data.append({
            '团队': score['teamName'],
            '项目': score['projectName'],
            '总分': score['overallScore'],
            '等级': score['grade'],
            '代码质量': score['dimensions']['codeQuality'],
            '创新性': score['dimensions']['innovation'],
            '完整性': score['dimensions']['completeness'],
            '文档': score['dimensions']['documentation']
        })

# 创建DataFrame并排序
df = pd.DataFrame(data)
df = df.sort_values('总分', ascending=False)
df.insert(0, '排名', range(1, len(df) + 1))

# 导出Excel
df.to_excel('hackathon-results.xlsx', index=False, engine='openpyxl')
print(f"Excel报告已生成: hackathon-results.xlsx")
```

**运行脚本**：
```bash
pip install pandas openpyxl
python export-to-excel.py
```

---

## 💡 命令详解

### 完整参数列表

| 参数 | 简写 | 说明 | 必需 | 默认值 | 示例 |
|------|------|------|------|--------|------|
| `--github-url` | - | GitHub仓库URL | * | - | `--github-url https://github.com/user/repo` |
| `--gitee-url` | - | Gitee仓库URL | * | - | `--gitee-url https://gitee.com/user/repo` |
| `--directory` | `-d` | 本地项目目录 | * | - | `-d /path/to/project` |
| `--team` | `-t` | 团队名称 | ✓ | "Unknown Team" | `--team "Team A"` |
| `--branch` | `-b` | Git分支 | - | "main" | `-b develop` |
| `--output` | `-o` | JSON输出文件 | - | - | `-o score.json` |
| `--report` | `-r` | Markdown报告 | - | - | `-r report.md` |
| `--help` | `-h` | 显示帮助 | - | - | `--help` |

**注**：标记 * 的参数三选一（github-url、gitee-url、directory）

### 参数组合示例

```bash
# 最简命令（使用默认团队名）
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/repo

# 完整命令（所有参数）
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/repo \
  --team "Team Awesome" \
  --branch develop \
  --output results/score.json \
  --report results/report.md

# 本地项目评审
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  -d ./my-project \
  -t "My Team" \
  -o score.json \
  -r report.md

# 仅生成JSON评分
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --gitee-url https://gitee.com/user/repo \
  --team "Gitee Team" \
  -o score.json

# 仅生成Markdown报告
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/repo \
  --team "Report Team" \
  -r report.md
```

---

## ⚙️ 配置说明

### AI服务配置详解

#### DeepSeek配置（推荐）

```yaml
aiService:
  provider: "deepseek"
  apiKey: "sk-xxx"  # 从 platform.deepseek.com 获取
  baseUrl: "https://api.deepseek.com/v1/chat/completions"
  model: "deepseek-chat"
  maxTokens: 8000        # 单次请求最大token
  temperature: 0.7       # 创造性（0-1），代码评审建议0.7
  maxRetries: 2          # 失败重试次数
  maxConcurrency: 20     # 最大并发数
```

**参数说明**：
- `maxTokens`: 控制AI响应长度，8000适合中等项目
- `temperature`: 
  - 0.0 - 0.3: 更确定性、一致性（适合评分）
  - 0.4 - 0.7: 平衡（推荐用于代码评审）
  - 0.8 - 1.0: 更创造性（不推荐评审）
- `maxConcurrency`: 并发分析数，根据API限制调整

#### OpenAI配置

```yaml
aiService:
  provider: "openai"
  apiKey: "sk-xxx"  # 从 platform.openai.com 获取
  baseUrl: "https://api.openai.com/v1/chat/completions"
  model: "gpt-4"    # 或 gpt-3.5-turbo
  maxTokens: 8000
  temperature: 0.7
```

**模型选择**：
- `gpt-4`: 最强理解能力，费用较高
- `gpt-3.5-turbo`: 性价比高，速度快
- `gpt-4-turbo`: 平衡性能和费用

### 缓存配置

```yaml
cache:
  enabled: true         # 启用缓存
  type: "file"          # 缓存类型: file, redis, memory
  ttlHours: 24          # 缓存过期时间（小时）
  maxSize: 1000         # 最大缓存条目
  
  fileCache:
    baseDir: "${user.home}/.ai-reviewer-cache"
    compression: true   # 启用压缩
```

**使用建议**：
- 启用缓存可避免重复分析相同项目
- 适合多轮评审、测试场景
- 清空缓存：删除 `~/.ai-reviewer-cache` 目录

### 文件扫描配置

```yaml
fileScan:
  # 包含的文件模式
  includePatterns:
    - "*.java"
    - "*.py"
    - "*.js"
    - "*.ts"
    # ... 更多
    
  # 排除的文件模式
  excludePatterns:
    - "*/node_modules/*"
    - "*/target/*"
    - "*/dist/*"
    - "*/build/*"
    - "*/test/*"
    - "*/tests/*"
    - "*/__pycache__/*"
    - "*.min.js"
    
  maxFileSizeKB: 500    # 单文件最大大小（KB）
  maxProjectSizeKB: 50000  # 项目最大大小（KB）
```

---

## 📊 评分标准

### 评分维度详解

#### 1. 代码质量 (40%)

**评估要点**：
- ✅ 代码结构和组织
- ✅ 命名规范（变量、函数、类）
- ✅ 代码复杂度（圈复杂度、嵌套深度）
- ✅ 重复代码检测
- ✅ 设计模式使用
- ✅ SOLID原则遵循
- ✅ 错误处理机制
- ✅ 日志记录

**评分等级**：
- 90-100: 代码质量极高，几乎无可挑剔
- 80-89: 代码质量优秀，有少量改进空间
- 70-79: 代码质量良好，有明显改进点
- 60-69: 代码质量一般，需要重构
- <60: 代码质量较差，存在严重问题

#### 2. 创新性 (30%)

**评估要点**：
- ✅ 技术方案新颖性
- ✅ 问题解决独特性
- ✅ 用户体验创新
- ✅ 技术栈选择合理性
- ✅ 商业价值潜力
- ✅ 市场竞争力

**评分等级**：
- 90-100: 极具创新性，颠覆性方案
- 80-89: 创新性强，有独特亮点
- 70-79: 有一定创新，但不够突出
- 60-69: 创新性一般，常规方案
- <60: 缺乏创新，简单模仿

#### 3. 完整性 (20%)

**评估要点**：
- ✅ 核心功能实现程度
- ✅ 边界情况处理
- ✅ 错误处理和容错
- ✅ 用户体验完整性
- ✅ 性能优化
- ✅ 可扩展性设计

**评分等级**：
- 90-100: 功能完整，考虑全面
- 80-89: 核心功能完整，细节可优化
- 70-79: 基本功能完整，缺少部分特性
- 60-69: 功能不够完整，有明显缺失
- <60: 功能严重不完整

#### 4. 文档质量 (10%)

**评估要点**：
- ✅ README完整性
- ✅ 安装和使用说明
- ✅ API文档
- ✅ 代码注释质量
- ✅ 架构文档
- ✅ 贡献指南

**评分等级**：
- 90-100: 文档详尽，易于理解
- 80-89: 文档完整，有少量遗漏
- 70-79: 文档基本，需要补充
- 60-69: 文档不足，难以使用
- <60: 文档缺失

### 总分计算

```
总分 = 代码质量 × 40% + 创新性 × 30% + 完整性 × 20% + 文档质量 × 10%
```

**等级划分**：
- **A+** (95-100): 卓越
- **A**  (90-94): 优秀
- **A-** (85-89): 良好
- **B+** (80-84): 中上
- **B**  (75-79): 中等
- **B-** (70-74): 中下
- **C**  (60-69): 及格
- **D**  (<60): 不及格

---

## ❓ 常见问题

<details>
<summary><b>Q1: 如何切换AI服务？</b></summary>

**方法1：环境变量（推荐）**
```bash
# 切换到OpenAI
export AI_PROVIDER=openai
export AI_API_KEY=sk-your-openai-key

# 切换到Gemini
export AI_PROVIDER=gemini
export AI_API_KEY=your-gemini-key

# 切换到Claude
export AI_PROVIDER=claude
export AI_API_KEY=sk-ant-your-claude-key
```

**方法2：修改配置文件**
```yaml
# 编辑 src/main/resources/config.yaml
aiService:
  provider: "openai"  # 改为目标AI服务
  apiKey: "your-api-key"
  model: "gpt-4"
```

**重要**：切换AI服务后需要重新编译：
```bash
mvn clean package -DskipTests
```
</details>

<details>
<summary><b>Q2: GitHub克隆失败怎么办？</b></summary>

**原因1：网络问题**
```bash
# 解决方案：使用代理
export https_proxy=http://127.0.0.1:7890
export http_proxy=http://127.0.0.1:7890

# 或使用Gitee镜像（如果有）
--gitee-url https://gitee.com/...
```

**原因2：私有仓库需要认证**
```bash
# 解决方案：使用本地已克隆的项目
git clone https://github.com/user/private-repo
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --directory ./private-repo \
  --team "Team Name" \
  -o score.json
```

**原因3：分支不存在**
```bash
# 检查分支名称
git ls-remote --heads https://github.com/user/repo

# 使用正确的分支名
--branch main  # 或 master
```
</details>

<details>
<summary><b>Q3: 本地项目评审路径怎么写？</b></summary>

**绝对路径（推荐）**
```bash
# Linux/macOS
--directory /home/user/projects/my-project

# Windows
--directory C:\Users\user\projects\my-project
```

**相对路径**
```bash
# 当前目录下的子目录
--directory ./my-project

# 父目录
--directory ../another-project

# 当前目录
--directory .
```

**注意**：路径中包含空格时需要用引号：
```bash
--directory "/path/with spaces/my project"
```
</details>

<details>
<summary><b>Q4: API调用失败，提示"Too Many Requests"</b></summary>

**原因**：超过AI服务的请求限制

**解决方案**：
1. 降低并发数
```yaml
aiService:
  maxConcurrency: 5  # 从20降到5
```

2. 增加重试延迟
```yaml
aiService:
  retryDelay: 3000  # 从1秒增加到3秒
```

3. 启用缓存避免重复请求
```yaml
cache:
  enabled: true
```

4. 分批评审，避免同时提交太多请求
</details>

<details>
<summary><b>Q5: 评分结果JSON文件为空或损坏</b></summary>

**原因**：分析过程中断或失败

**解决方案**：
1. 检查日志文件
```bash
# 查看详细日志
java -Dlog.level=DEBUG -cp target/ai-reviewer-2.0.jar ...
```

2. 检查AI服务状态
```bash
# 测试API可用性
curl -X POST https://api.deepseek.com/v1/chat/completions \
  -H "Authorization: Bearer $AI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"deepseek-chat","messages":[{"role":"user","content":"test"}]}'
```

3. 重新运行评审
```bash
# 删除缓存后重试
rm -rf ~/.ai-reviewer-cache
java -cp target/ai-reviewer-2.0.jar ...
```
</details>

<details>
<summary><b>Q6: 如何评审大型项目（超过10万行代码）？</b></summary>

**策略1：分模块评审**
```bash
# 评审核心模块
--directory ./project/core

# 评审API模块
--directory ./project/api

# 评审Web模块
--directory ./project/web
```

**策略2：调整配置**
```yaml
aiService:
  maxTokens: 16000      # 增加token限制
  analyzeTimeout: 600000  # 增加超时时间（10分钟）
  
fileScan:
  maxFileSizeKB: 1000   # 增加文件大小限制
  maxProjectSizeKB: 100000  # 增加项目大小限制
```

**策略3：过滤测试代码**
```yaml
fileScan:
  excludePatterns:
    - "*/test/*"
    - "*/tests/*"
    - "*Test.java"
    - "*_test.py"
```
</details>

---

## 🔧 故障排查

### 问题诊断检查表

| 问题症状 | 可能原因 | 解决方案 |
|---------|---------|---------|
| ❌ Java命令找不到 | Java未安装或未配置环境变量 | 安装Java并配置PATH |
| ❌ mvn命令找不到 | Maven未安装 | 安装Maven |
| ❌ 编译失败 | 依赖下载失败 | 检查网络，配置Maven镜像 |
| ❌ AI API调用失败 | API密钥错误或服务不可用 | 检查API密钥，测试服务可用性 |
| ❌ Git克隆失败 | 网络问题或仓库不存在 | 检查URL，使用代理或本地目录 |
| ❌ 内存不足 | 项目过大 | 增加JVM内存 `-Xmx4g` |
| ❌ 评分结果异常 | AI服务响应异常 | 检查日志，重试评审 |

### 详细日志查看

```bash
# 启用DEBUG日志
export LOG_LEVEL=DEBUG

# 运行评审
java -Dlog.level=DEBUG -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/repo \
  --team "Team Name" \
  -o score.json 2>&1 | tee review.log

# 查看日志
cat review.log
```

### 增加JVM内存

```bash
# 增加堆内存到4GB
java -Xmx4g -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --github-url https://github.com/user/repo \
  --team "Team Name" \
  -o score.json
```

---

## 🎓 最佳实践

### 1. 评审前准备

✅ **DO**：
- 提前测试AI服务可用性
- 准备好所有项目URL或本地路径
- 创建结果目录
- 启用缓存
- 检查网络连接

❌ **DON'T**：
- 不要在评审期间更改AI服务配置
- 不要使用不稳定的网络
- 不要评审过大的项目（>100MB）

### 2. 批量评审优化

✅ **DO**：
- 使用并发评审（但不要超过API限制）
- 分批次提交（每批10-20个项目）
- 为每个团队创建独立的输出文件
- 定期备份评审结果

❌ **DON'T**：
- 不要同时评审超过50个项目
- 不要使用过高的并发数
- 不要忽略错误继续执行

### 3. 结果分析

✅ **DO**：
- 查看详细的Markdown报告
- 对比不同团队的评分
- 关注AI给出的具体建议
- 生成可视化排行榜

❌ **DON'T**：
- 不要只看总分，要看各维度
- 不要完全依赖AI评分
- 不要忽略报告中的警告信息

### 4. 安全建议

✅ **DO**：
- 使用环境变量管理API密钥
- 定期更换API密钥
- 不要将API密钥提交到Git
- 使用`.gitignore`忽略配置文件

❌ **DON'T**：
- 不要在代码中硬编码API密钥
- 不要共享包含密钥的配置文件
- 不要在公开场合展示API密钥

### 5. 性能优化

✅ **DO**：
- 启用文件缓存
- 排除无关文件（node_modules、target等）
- 限制单文件大小
- 使用合适的并发数

❌ **DON'T**：
- 不要禁用缓存
- 不要评审包含大量第三方代码的项目
- 不要设置过高的maxTokens

---

## 📞 获取帮助

### 查看帮助信息

```bash
java -cp target/ai-reviewer-2.0.jar \
  top.yumbo.ai.reviewer.application.hackathon.cli.HackathonCommandLineApp \
  --help
```

### 社区支持

- 📧 Email: support@ai-reviewer.com
- 💬 GitHub Issues: https://github.com/jinhua10/ai-reviewer/issues
- 📖 Documentation: https://ai-reviewer.com/docs

### 贡献

欢迎提交Issue和Pull Request！

---

## 📄 许可证

Apache License 2.0

---

**最后更新**: 2025-11-13  
**版本**: 2.0  
**作者**: AI-Reviewer Team

