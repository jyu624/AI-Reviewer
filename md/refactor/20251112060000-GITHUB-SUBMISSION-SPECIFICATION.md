# 🎯 黑客松代码提交规范与 GitHub 集成方案

> **创建时间**: 2025-11-12 06:00:00  
> **版本**: v1.0  
> **状态**: 📋 规范文档  

---

## 📦 代码提交方式对比

### 方案对比表

| 维度 | GitHub（推荐） | AWS S3 | 混合方案 |
|------|---------------|--------|----------|
| 版本控制 | ✅ 完整 | ❌ 无 | ⚠️ 部分 |
| 提交历史 | ✅ 可追溯 | ❌ 无 | ⚠️ GitHub有 |
| 代码审查 | ✅ 方便 | ❌ 困难 | ⚠️ 部分方便 |
| 自动集成 | ✅ 简单 | ⚠️ 需开发 | ⚠️ 复杂 |
| 存储成本 | ✅ 免费 | ⚠️ 按量收费 | ⚠️ 混合 |
| 团队协作 | ✅ 原生支持 | ❌ 不支持 | ⚠️ 分散 |
| 社区认可 | ✅ 标准做法 | ❌ 非主流 | ⚠️ 一般 |
| **推荐度** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

---

## ✅ 推荐方案：GitHub + S3 混合

### 1. GitHub 存储源代码（主要）

**存储内容**：
```
✅ 必须放在 GitHub:
  - 所有源代码文件
  - README.md（项目说明）
  - LICENSE（开源协议）
  - .gitignore
  - 配置文件（脱敏后）
  - 构建脚本（Dockerfile, pom.xml, package.json等）
  - 文档（API文档、架构文档）

❌ 不要放在 GitHub:
  - API密钥、密码（使用环境变量）
  - 大型二进制文件（> 100MB）
  - 编译产物（target/, build/, node_modules/）
  - 临时文件、日志
```

**仓库要求**：
```yaml
基本要求:
  可见性: public（公开仓库）
  分支: main/master
  README: 必须有，包含项目说明、安装步骤、运行方法
  LICENSE: 必须有（推荐 MIT/Apache 2.0）
  .gitignore: 必须有

代码质量要求:
  提交历史: 
    - 最少 3 次有意义的 commit
    - 不能是 "Initial commit" 后就一个 "Final commit"
    - commit message 要清晰
  
  代码结构:
    - 项目结构清晰
    - 代码可运行
    - 有基本的错误处理
  
  文档完整性:
    - README 包含：项目介绍、技术栈、安装、使用、贡献指南
    - 核心代码有注释
    - 复杂逻辑有说明

评分加分项:
  - 有单元测试 (+10分)
  - 有 CI/CD 配置 (+5分)
  - 有完整的 API 文档 (+5分)
  - 提交历史清晰规范 (+5分)
  - 有 GitHub Actions (+5分)
```

### 2. S3 存储补充资料（可选）

**存储内容**：
```
✅ 适合放在 S3:
  - 打包好的可执行文件（.jar, .exe, .dmg）
  - 演示视频（MP4, > 50MB）
  - 演示 PPT（大文件）
  - 设计稿原文件（.psd, .sketch, .fig）
  - 大型数据集（训练数据、测试数据）
  - 项目截图集合
  - 备份文件

❌ 不要放在 S3:
  - 源代码（必须在 GitHub）
  - 文本文档（应该在 GitHub）
  - 小文件（< 1MB，放 GitHub 更方便）
```

**S3 访问控制**：
```yaml
访问方式:
  - 公开读（Public Read）- 演示视频、PPT
  - 预签名 URL（Presigned URL）- 敏感资料
  - CloudFront CDN - 加速访问

文件组织:
  hackathon-submissions/
    ├── {team-id}/
    │   ├── demo-video.mp4
    │   ├── presentation.pptx
    │   ├── executable/
    │   │   ├── app-v1.0.jar
    │   │   └── README.txt
    │   └── screenshots/
    │       ├── screenshot1.png
    │       └── screenshot2.png
```

---

## 🔧 GitHub 集成实现方案

### 方案 A: GitHub URL 提交（推荐）⭐⭐⭐⭐⭐

**工作流程**：
```
1. 团队提交 GitHub URL
   ↓
2. 后台自动克隆代码
   ↓
3. 本地分析评分
   ↓
4. 生成报告并更新排行榜
```

**优势**：
- ✅ 完全自动化
- ✅ 支持增量更新
- ✅ 可追溯代码历史
- ✅ 无需手动上传

**实现细节**：
```java
// 团队提交
public Submission submitGitHubProject(
    String projectId,
    String githubUrl,      // 必填：GitHub 仓库 URL
    String branch,         // 可选：分支名，默认 main
    String commitHash,     // 可选：指定 commit
    Participant submitter
) {
    // 1. 验证 GitHub URL
    validateGitHubUrl(githubUrl);
    
    // 2. 克隆代码到临时目录
    Path localPath = gitHubAdapter.cloneRepository(
        githubUrl, 
        branch != null ? branch : "main"
    );
    
    // 3. 扫描项目文件
    Project coreProject = fileSystemPort.scanProjectFiles(localPath);
    
    // 4. 分析评分
    HackathonProject hackProject = getProjectById(projectId);
    analysisService.analyzeProject(hackProject, coreProject);
    
    // 5. 清理临时文件
    fileSystemPort.deleteDirectory(localPath);
    
    return hackProject.getLatestSubmission();
}
```

**配置要求**：
```yaml
github:
  clone:
    timeout: 300  # 5分钟超时
    depth: 1      # 浅克隆，只拉取最新代码
    retry: 3      # 失败重试3次
  
  validation:
    max_size: 500MB        # 最大仓库大小
    required_files:        # 必需文件
      - README.md
    blocked_extensions:    # 禁止的文件类型
      - .exe
      - .dll
      - .so
```

---

### 方案 B: S3 + GitHub 混合（备选）⭐⭐⭐

**使用场景**：
- 某些团队没有 GitHub 账号
- 代码包含敏感信息不能公开
- 需要提交大文件

**工作流程**：
```
1. 团队上传代码到 S3
   ↓
2. 提交 S3 URL + GitHub URL（可选）
   ↓
3. 后台下载并解压
   ↓
4. 分析评分
```

**实现细节**：
```java
public Submission submitFromS3(
    String projectId,
    String s3Url,          // S3 下载链接
    String githubUrl,      // 可选：GitHub 仓库（用于查看代码）
    Participant submitter
) {
    // 1. 从 S3 下载代码包
    Path localPath = s3Adapter.downloadAndExtract(s3Url);
    
    // 2. 验证文件结构
    validateProjectStructure(localPath);
    
    // 3. 后续流程同方案 A
    // ...
}
```

**缺点**：
- ⚠️ 无法验证提交历史
- ⚠️ 无法追溯代码演进
- ⚠️ 评分会降低（缺少提交历史维度）

---

## 📝 提交规范详细说明

### 1. GitHub 仓库格式要求

#### ✅ 合格的仓库示例

```
https://github.com/awesome-team/hackathon-project

Repository Structure:
awesome-team/hackathon-project/
├── README.md                    ✅ 必需
├── LICENSE                      ✅ 必需
├── .gitignore                   ✅ 必需
├── pom.xml / package.json       ✅ 构建文件
├── Dockerfile                   ⭐ 加分项
├── docs/                        ⭐ 加分项
│   ├── API.md
│   └── ARCHITECTURE.md
├── src/                         ✅ 源代码
│   ├── main/
│   └── test/                    ⭐ 测试代码（加分）
└── .github/                     ⭐ CI/CD（加分）
    └── workflows/
        └── ci.yml

README.md 内容要求:
# Project Title                  ✅ 必需
## Introduction                  ✅ 必需
## Features                      ✅ 必需
## Tech Stack                    ✅ 必需
## Installation                  ✅ 必需
## Usage                         ✅ 必需
## Demo                          ⭐ 加分项
## Contributors                  ✅ 必需
## License                       ✅ 必需

Commit History:
✅ Good Example:
  - feat: add user authentication
  - fix: resolve login bug
  - docs: update README
  - refactor: improve code structure
  (至少 5-10 个有意义的 commit)

❌ Bad Example:
  - Initial commit
  - Final commit
  (只有2个 commit，明显不是真实开发过程)
```

#### ❌ 不合格的仓库示例

```
问题1: 只有一个文件
awesome-team/hackathon-project/
└── all-code.py                  ❌ 所有代码在一个文件

问题2: 没有文档
awesome-team/hackathon-project/
├── src/
│   └── main.java
└── pom.xml                      ❌ 缺少 README

问题3: 包含敏感信息
awesome-team/hackathon-project/
├── config.properties            ❌ 包含 API Key
└── database-backup.sql          ❌ 包含生产数据

问题4: 提交历史不真实
Commits:
  - Initial commit (5000 files changed)  ❌ 明显作弊
  - Update README.md
```

---

### 2. URL 格式验证

**支持的格式**：
```java
// ✅ 标准 HTTPS URL
https://github.com/username/repository

// ✅ 带 .git 后缀
https://github.com/username/repository.git

// ✅ 指定分支
https://github.com/username/repository/tree/develop

// ✅ 指定 commit
https://github.com/username/repository/commit/abc123

// ❌ 不支持 SSH（需要密钥）
git@github.com:username/repository.git

// ❌ 不支持私有仓库（需要 token）
https://github.com/username/private-repo  (403 Forbidden)
```

**验证逻辑**：
```java
public boolean validateGitHubUrl(String url) {
    // 1. 格式检查
    if (!url.matches("^https://github\\.com/[\\w-]+/[\\w.-]+.*$")) {
        throw new IllegalArgumentException("无效的 GitHub URL 格式");
    }
    
    // 2. 仓库可访问性检查
    if (!isRepositoryAccessible(url)) {
        throw new IllegalArgumentException("仓库不存在或不可访问（可能是私有仓库）");
    }
    
    // 3. 仓库大小检查
    long repoSize = getRepositorySize(url);
    if (repoSize > 500 * 1024 * 1024) {  // 500MB
        throw new IllegalArgumentException("仓库大小超过限制（500MB）");
    }
    
    // 4. 必需文件检查
    if (!hasReadme(url)) {
        throw new IllegalArgumentException("缺少 README.md 文件");
    }
    
    return true;
}
```

---

### 3. 评分规则调整

基于 GitHub 仓库的评分应该包含：

```java
public class GitHubBasedScoringService extends HackathonScoringService {
    
    @Override
    public HackathonScore calculateScore(
        ReviewReport reviewReport,
        Project project,
        GitHubMetrics githubMetrics  // 新增：GitHub 指标
    ) {
        int codeQuality = calculateCodeQuality(reviewReport);
        int innovation = calculateInnovation(reviewReport, project);
        int completeness = calculateCompleteness(reviewReport, project);
        int documentation = calculateDocumentation(project);
        
        // 新增：GitHub 贡献度评分（调整权重）
        int githubContribution = calculateGitHubContribution(githubMetrics);
        
        // 调整权重：
        // 代码质量 35% (原40%)
        // 创新性 30%
        // 完成度 15% (原20%)
        // 文档质量 10%
        // GitHub 贡献 10% (新增)
        
        return HackathonScore.builder()
            .codeQuality(codeQuality)
            .innovation(innovation)
            .completeness(completeness + githubContribution / 2)
            .documentation(documentation)
            .build();
    }
    
    /**
     * 计算 GitHub 贡献度
     */
    private int calculateGitHubContribution(GitHubMetrics metrics) {
        int score = 0;
        
        // 提交历史 (0-40分)
        int commitCount = metrics.getCommitCount();
        if (commitCount >= 3) score += 10;
        if (commitCount >= 10) score += 10;
        if (commitCount >= 20) score += 10;
        
        // Commit 质量 (0-20分)
        if (metrics.hasDescriptiveCommitMessages()) score += 10;
        if (metrics.hasRegularCommits()) score += 10;  // 不是集中在最后一天
        
        // 协作指标 (0-20分)
        if (metrics.getContributorCount() > 1) score += 10;  // 多人协作
        if (metrics.hasPullRequests()) score += 5;  // 有 PR 流程
        if (metrics.hasCodeReview()) score += 5;  // 有 Code Review
        
        // 项目活跃度 (0-20分)
        if (metrics.getFirstCommitDate().isBefore(deadline.minusDays(7))) {
            score += 10;  // 提前开始
        }
        if (metrics.hasGitHubActions()) score += 5;  // 有 CI/CD
        if (metrics.hasIssues()) score += 5;  // 有 Issue 管理
        
        return Math.min(100, score);
    }
}

/**
 * GitHub 指标
 */
@Data
public class GitHubMetrics {
    private int commitCount;                    // 提交次数
    private int contributorCount;               // 贡献者数量
    private LocalDateTime firstCommitDate;      // 首次提交时间
    private LocalDateTime lastCommitDate;       // 最后提交时间
    private boolean hasDescriptiveCommitMessages;  // 有描述性的 commit message
    private boolean hasRegularCommits;          // 有规律的提交（不是突击）
    private boolean hasPullRequests;            // 有 PR
    private boolean hasCodeReview;              // 有 Code Review
    private boolean hasGitHubActions;           // 有 CI/CD
    private boolean hasIssues;                  // 有 Issue
    private List<String> branches;              // 分支列表
    private int codeFrequency;                  // 代码提交频率
}
```

---

## 🚀 实现优先级

### Phase 1: 基础 GitHub 集成（本周）

```
✅ Day 2-3: GitHub 基础集成
  - GitHubPort 接口定义
  - GitHubAdapter 实现（JGit）
  - 仓库克隆、文件扫描
  - 基本验证（URL格式、文件存在）

🟡 Day 4-5: 完整流程
  - 端到端集成测试
  - GitHub → 分析 → 评分 → 排行榜
  - 错误处理和重试
```

### Phase 2: GitHub 指标采集（下周）

```
🟡 高级功能:
  - 提取 commit 历史
  - 分析 commit 质量
  - 计算贡献者数量
  - 检测 CI/CD 配置
  
🟡 评分优化:
  - 加入 GitHub 贡献度评分
  - 调整权重分配
```

### Phase 3: S3 备选方案（可选）

```
⚪ 如果需要:
  - S3 集成
  - 混合提交支持
```

---

## 📋 团队提交检查清单

### 提交前自查

```markdown
## 代码检查
- [ ] 代码已推送到 GitHub 公开仓库
- [ ] 仓库包含 README.md
- [ ] README 包含：项目介绍、安装、使用说明
- [ ] 仓库包含 LICENSE 文件
- [ ] 代码可以成功运行
- [ ] 没有硬编码的密钥/密码
- [ ] 有 .gitignore 文件
- [ ] 至少有 3 次有意义的 commit

## 文档检查
- [ ] README 格式清晰
- [ ] 核心代码有注释
- [ ] 复杂逻辑有说明
- [ ] 有技术栈说明

## 加分项（可选）
- [ ] 有单元测试
- [ ] 有 CI/CD 配置
- [ ] 有 API 文档
- [ ] 提交历史规范
- [ ] 有分支管理
- [ ] 有 Issue/PR
```

---

## 🎯 最终建议

### 推荐配置

```yaml
主要方式: GitHub（必须）
  - 所有源代码
  - 文档
  - 配置文件

补充方式: S3（可选）
  - 演示视频
  - 大文件
  - 可执行文件

评分权重:
  代码质量: 35%
  创新性: 30%
  完成度: 15%
  文档质量: 10%
  GitHub 贡献: 10%
```

### 实施步骤

```
Week 1:
  ✅ 实现基础 GitHub 集成
  ✅ URL 提交 + 自动克隆
  ✅ 基本验证

Week 2:
  🟡 添加 GitHub 指标采集
  🟡 优化评分算法
  🟡 完善错误处理

Week 3+:
  ⚪ S3 备选方案（如果需要）
  ⚪ 高级功能
```

---

**文档版本**: v1.0  
**创建时间**: 2025-11-12 06:00:00  
**建议采纳**: ⭐⭐⭐⭐⭐ GitHub 作为主要方式  
**下一步**: 开始实现 GitHubPort 和 GitHubAdapter  

---

*建议：优先实现 GitHub 集成，S3 作为后期补充方案* 🚀

