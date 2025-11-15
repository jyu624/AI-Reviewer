# 批量评审示例文件

本目录包含批量评审功能的示例输入文件。

## 文件说明

### 📄 batch-review-sample.csv
CSV格式的示例文件，包含4个团队的提交信息。

**适用场景**：
- Excel编辑
- 简单快速
- 适合非技术人员

**使用方法**：
```bash
# 在交互式界面中选择 "2. 批量评审项目"
# 输入文件路径: examples/batch-review-sample.csv
```

### 📄 batch-review-sample.json
JSON格式的示例文件，包含更丰富的元数据。

**适用场景**：
- 程序化生成
- 包含额外信息（如标签）
- 结构化数据

**使用方法**：
```bash
# 在交互式界面中选择 "2. 批量评审项目"
# 输入文件路径: examples/batch-review-sample.json
```

### 📄 batch-review-sample.txt
纯文本格式的示例文件，最简单的格式。

**适用场景**：
- 快速测试
- 只有URL列表
- 命令行生成

**使用方法**：
```bash
# 在交互式界面中选择 "2. 批量评审项目"
# 输入文件路径: examples/batch-review-sample.txt
```

## 自定义示例

你可以复制这些文件并修改为实际的团队信息：

### CSV 模板
```csv
team_name,repo_url,contact_email,submission_time
Your Team,https://github.com/yourteam/project,team@example.com,2025-11-14T10:00:00
```

### JSON 模板
```json
{
  "teams": [
    {
      "teamName": "Your Team",
      "repoUrl": "https://github.com/yourteam/project",
      "contactEmail": "team@example.com",
      "submissionTime": "2025-11-14T10:00:00"
    }
  ]
}
```

### TXT 模板
```
Your Team:https://github.com/yourteam/project
```

## 注意事项

⚠️ **这些示例文件中的URL仅用于演示，实际使用时请替换为真实的仓库地址。**

- GitHub URL 示例：`https://github.com/username/repository`
- Gitee URL 示例：`https://gitee.com/username/repository`
- GitLab URL 示例：`https://gitlab.com/username/repository`
- 本地路径示例：`D:\Projects\my-project` 或 `/home/user/projects/my-project`

## 更多信息

详细使用指南请参考：[批量评审使用指南](../doc/HACKATHON/BATCH-REVIEW-GUIDE.md)

