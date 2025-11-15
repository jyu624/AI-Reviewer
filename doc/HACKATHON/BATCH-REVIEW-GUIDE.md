# 批量评审功能使用指南

## 概述

批量评审功能允许你一次性评审多个黑客松项目，支持并行处理，并自动生成汇总报告和排行榜。

## 支持的文件格式

### 1. CSV 格式（推荐）

**格式**：`team_name,repo_url,contact_email,submission_time`

**示例文件**：`examples/batch-review-sample.csv`

```csv
team_name,repo_url,contact_email,submission_time
Team Alpha,https://github.com/user1/hackathon-project,alpha@example.com,2025-11-14T10:00:00
Team Beta,https://github.com/user2/awesome-app,beta@example.com,2025-11-14T11:30:00
Team Gamma,https://github.com/user3/innovative-solution,gamma@example.com,2025-11-14T12:00:00
```

**特点**：
- 简单易用，Excel可直接编辑
- 每行一个团队
- 第一行为标题（可选）

### 2. JSON 格式

**格式**：包含teams数组的JSON对象

**示例文件**：`examples/batch-review-sample.json`

```json
{
  "hackathon": {
    "name": "2025 黑客松大赛",
    "teams": [
      {
        "teamName": "Team Alpha",
        "repoUrl": "https://github.com/user1/hackathon-project",
        "contactEmail": "alpha@example.com",
        "submissionTime": "2025-11-14T10:00:00",
        "tags": ["AI", "Web"]
      }
    ]
  }
}
```

**特点**：
- 支持更多元数据（如标签）
- 结构化数据
- 易于程序生成

### 3. TXT 格式（简单模式）

**格式**：每行一个URL或"团队名:URL"

**示例文件**：`examples/batch-review-sample.txt`

```
Team Alpha:https://github.com/user1/hackathon-project
Team Beta:https://github.com/user2/awesome-app
https://github.com/user3/innovative-solution
```

**特点**：
- 最简单的格式
- 适合快速测试
- 如果只有URL，会从URL提取团队名

## 使用步骤

### 1. 准备团队提交文件

根据你的需求选择一种格式，创建包含所有参赛团队信息的文件。

### 2. 启动交互式界面

```bash
# 使用命令行启动（需要完整的依赖注入配置）
java -jar ai-reviewer.jar interactive
```

或通过应用主入口启动。

### 3. 选择批量评审选项

在主菜单中选择 `2. 📦 批量评审项目`

### 4. 输入文件路径

```
文件路径: examples/batch-review-sample.csv
```

或使用绝对路径：
```
文件路径: D:\hackathon\submissions.json
```

### 5. 确认并选择并行度

系统会显示找到的团队数量，确认后选择并行评审的线程数（1-10，默认4）：

```
✅ 找到 4 个团队提交

是否开始批量评审？[Y/n]: Y

并行评审线程数 [1-10, 默认4]: 4
```

### 6. 等待评审完成

系统会实时显示评审进度：

```
⏳ 开始批量评审（并行度: 4）...

✅ [1/4] Team Alpha - 评审完成 (得分: 85)
✅ [2/4] Team Beta - 评审完成 (得分: 92)
❌ [3/4] Team Gamma - 评审失败: 仓库不可访问
✅ [4/4] Team Delta - 评审完成 (得分: 78)
```

### 7. 查看结果摘要

评审完成后会显示详细摘要：

```
📊 批量评审完成
============================================================
总数: 4
成功: 3 (75%)
失败: 1
耗时: 2分钟30秒

🏆 排行榜（前10名）:
   1. Team Beta                   得分: 92 (A)
   2. Team Alpha                  得分: 85 (B)
   3. Team Delta                  得分: 78 (C)

❌ 失败的团队:
  • Team Gamma: 仓库不可访问
============================================================
```

### 8. 导出详细报告

系统会询问是否导出详细报告：

```
是否导出详细报告？[Y/n]: Y
```

选择Y后会生成多个文件：

- **汇总报告**：`batch-review-20251114_143025-summary.md`
- **详细结果**：`batch-review-20251114_143025-details.json`
- **排行榜CSV**：`batch-review-20251114_143025-leaderboard.csv`
- **独立报告**：`batch-reports-20251114_143025/` 目录下的各团队报告

## 输出文件说明

### 汇总报告（Markdown）

包含：
- 评审概况统计
- 完整排行榜
- 失败团队列表

适合分享和展示。

### 详细结果（JSON）

包含所有原始数据，适合：
- 进一步数据分析
- 导入到其他系统
- 备份存档

### 排行榜（CSV）

包含：
- 排名
- 团队名称
- 总分及各维度得分
- 联系邮箱

适合：
- Excel处理
- 数据透视分析
- 邮件通知

### 独立报告

为每个成功评审的团队生成独立的Markdown报告，包含：
- 详细的代码分析
- 各维度评分和评语
- 发现的问题
- 改进建议

## 高级配置

### 并行度选择建议

| 团队数量 | 推荐并行度 | 说明 |
|---------|----------|------|
| 1-5 | 2-3 | 避免资源浪费 |
| 6-20 | 4-6 | 平衡性能和资源 |
| 21-50 | 6-8 | 充分利用多核 |
| 50+ | 8-10 | 最大并行度 |

**注意**：
- 并行度越高，评审越快，但占用资源越多
- 如果涉及Git克隆，建议降低并行度避免网络拥塞
- 本地项目评审可以使用更高并行度

### 本地路径支持

你可以混合使用Git URL和本地路径：

```csv
Team Alpha,https://github.com/user1/project,alpha@example.com,2025-11-14T10:00:00
Team Local,D:\Projects\my-hackathon-project,local@example.com,2025-11-14T11:00:00
```

本地路径将直接读取，不会进行Git克隆。

## 故障排除

### 1. 文件格式错误

**错误**：`不支持的文件格式`

**解决**：确保文件扩展名为 `.csv`, `.json`, 或 `.txt`

### 2. 解析失败

**错误**：`JSON 解析失败` 或 `没有找到有效的团队提交`

**解决**：
- 检查文件格式是否正确
- CSV确保逗号分隔正确
- JSON确保语法正确（可用在线JSON验证工具）

### 3. 仓库不可访问

**错误**：`仓库不可访问: https://...`

**解决**：
- 确认仓库URL正确
- 检查网络连接
- 对于私有仓库，确保已配置认证信息

### 4. 本地路径不存在

**错误**：`本地路径不存在: D:\...`

**解决**：
- 确认路径正确
- Windows路径使用双反斜杠 `\\` 或单正斜杠 `/`

### 5. 内存不足

**现象**：评审过程中程序崩溃或变慢

**解决**：
- 降低并行度
- 增加JVM堆内存：`java -Xmx4G -jar ai-reviewer.jar`
- 分批评审

## 最佳实践

### 1. 文件准备

- ✅ 使用CSV格式便于编辑和维护
- ✅ 提前验证所有URL可访问性
- ✅ 使用有意义的团队名称
- ✅ 记录联系邮箱便于后续通知

### 2. 评审执行

- ✅ 先用小批量测试（2-3个团队）
- ✅ 根据机器配置选择合适的并行度
- ✅ 为大批量评审预留足够时间
- ✅ 评审过程中保持网络稳定

### 3. 结果处理

- ✅ 及时保存生成的报告
- ✅ 用排行榜CSV进行数据分析
- ✅ 为获奖团队单独发送详细报告
- ✅ 保存JSON文件作为备份

## 示例场景

### 场景1：小型黑客松（10个团队）

```bash
# 1. 准备CSV文件（Excel编辑）
# 2. 启动评审，选择并行度4
# 3. 等待约5-10分钟
# 4. 导出所有报告
# 5. 将排行榜CSV导入Excel进行排名
```

### 场景2：大型黑客松（50个团队）

```bash
# 1. 准备JSON文件（从报名系统导出）
# 2. 分批评审：每批10-15个
# 3. 合并结果
# 4. 为前10名团队生成独立报告
# 5. 发送获奖通知邮件
```

### 场景3：混合模式（线上+线下）

```bash
# CSV内容：
# Team A,https://github.com/teamA/project,teamA@example.com,2025-11-14T10:00:00
# Team B,D:\local-projects\teamB,teamB@example.com,2025-11-14T11:00:00

# 同时评审GitHub项目和本地提交
```

## 技术细节

### 评审流程

1. **解析文件**：根据格式读取团队信息
2. **验证数据**：检查必填字段
3. **克隆/读取**：克隆Git仓库或读取本地路径
4. **扫描文件**：识别源代码文件
5. **AI分析**：调用AI模型进行评审
6. **评分计算**：根据配置的权重计算总分
7. **生成报告**：格式化输出结果

### 性能指标

基于Intel i7/16GB RAM的测试：

| 项目类型 | 单个评审时间 | 10个项目（并行度4） |
|---------|------------|-------------------|
| 小型项目（<100文件） | 30-60秒 | 2-3分钟 |
| 中型项目（100-500文件） | 1-3分钟 | 5-8分钟 |
| 大型项目（>500文件） | 3-10分钟 | 15-30分钟 |

*注：时间包含Git克隆和AI分析*

## 相关文档

- [黑客松评审指南](../doc/HACKATHON/HACKATHON-GUIDE.md)
- [评分配置说明](../doc/HACKATHON/HACKATHON-SCORING-GUIDE.md)
- [架构文档](../doc/ARCHITECTURE.md)

## 支持

如有问题，请参考：
- GitHub Issues: [提交问题](https://github.com/yourusername/AI-Reviewer/issues)
- 文档: [完整文档](../doc/)
- 示例: [更多示例](examples/)

