# DeepSeek AI 测试快速使用指南

## 🚀 立即开始

### 方式 1: 不配置 API Key（单元测试）

```bash
mvn test -Dtest=DeepSeekAIAdapterTest
```

**输出示例**:
```
⚠️  未配置 DEEPSEEK_API_KEY 环境变量，跳过真实 API 测试

[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 2
```

- ✅ 运行 28 个单元测试
- ⏭️ 跳过 2 个集成测试
- 🎯 适合：本地开发、快速验证

### 方式 2: 配置真实 API Key（完整测试）⭐ 推荐

#### Windows:
```cmd
set DEEPSEEK_API_KEY=sk-your-real-api-key
mvn test -Dtest=DeepSeekAIAdapterTest
```

#### Linux/Mac:
```bash
export DEEPSEEK_API_KEY=sk-your-real-api-key
mvn test -Dtest=DeepSeekAIAdapterTest
```

**输出示例**:
```
✅ 使用真实的 DeepSeek API Key 进行集成测试

[真实API测试] 
✅ AI 分析结果: 标准的 Hello World 程序，结构清晰，无明显问题
✅ API 可用

[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
```

- ✅ 运行所有 30 个测试
- ✅ 包括真实 API 验证
- 🎯 适合：发布前验证、CI/CD

---

## 📋 如何获取 DeepSeek API Key

1. 访问 [https://platform.deepseek.com](https://platform.deepseek.com)
2. 注册/登录账号
3. 进入"API Keys"页面
4. 创建新的 API Key
5. 复制 API Key（格式：`sk-...`）

---

## 🧪 测试分类

### 单元测试（28个）- 不需要 API Key

✅ 自动运行，无需配置

| 测试类别 | 测试方法 | 说明 |
|---------|---------|------|
| 构造函数 | shouldCreateAdapterWithProvidedConfig | 验证初始化 |
| 构造函数 | shouldUseDefaultValuesForMissingConfig | 验证默认值 |
| 构造函数 | shouldSetConcurrencyLimit | 验证并发限制 |
| 基本方法 | shouldReturnDeepSeek | 验证提供商名称 |
| 基本方法 | shouldReturnConfiguredMaxConcurrency | 验证最大并发数 |
| 参数验证 | shouldRejectNullPrompt | 拒绝 null |
| 参数验证 | shouldRejectEmptyPrompt | 拒绝空字符串 |
| 异步方法 | shouldReturnCompletableFuture | 验证异步返回 |
| 异步方法 | shouldHandleRequestAsynchronously | 验证异步处理 |
| 批量处理 | shouldHandleBatchRequests | 验证批量请求 |
| 批量处理 | shouldHandleEmptyArray | 验证空数组 |
| 并发控制 | shouldLimitConcurrentRequests | 验证并发限制 |
| 重试机制 | shouldRetryFailedRequests | 验证重试逻辑 |
| 关闭行为 | shouldShutdownGracefully | 验证正常关闭 |
| 配置验证 | shouldAcceptValidApiKey | 验证有效配置 |
| 边界条件 | shouldHandleVeryLongPrompt | 验证长文本 |
| 边界条件 | shouldHandleSpecialCharactersInPrompt | 验证特殊字符 |
| 边界条件 | shouldHandleUnicodeCharacters | 验证 Unicode |
| 性能测试 | shouldCreateAdapterQuickly | 验证创建速度 |
| 性能测试 | shouldShutdownInReasonableTime | 验证关闭速度 |
| ... | ... | 其他单元测试 |

### 集成测试（2个）- 需要 API Key

⚠️ 需要配置 `DEEPSEEK_API_KEY` 环境变量

| 测试方法 | 说明 | 预期结果 |
|---------|------|---------|
| shouldAnalyzeSimpleCodeWithRealAPI | 分析 Hello World 代码 | 返回 AI 评价 |
| shouldReturnTrueWithRealAPI | 检查 API 可用性 | 返回 true |

---

## 💡 实用示例

### 示例 1: 临时设置 API Key

```bash
# Windows - 临时设置（当前终端有效）
set DEEPSEEK_API_KEY=sk-your-api-key
mvn test -Dtest=DeepSeekAIAdapterTest

# Linux/Mac - 临时设置
DEEPSEEK_API_KEY=sk-your-api-key mvn test -Dtest=DeepSeekAIAdapterTest
```

### 示例 2: 永久设置 API Key

**Windows**:
```cmd
# 设置系统环境变量
setx DEEPSEEK_API_KEY "sk-your-api-key"

# 重启终端后生效
mvn test -Dtest=DeepSeekAIAdapterTest
```

**Linux/Mac**:
```bash
# 添加到 ~/.bashrc 或 ~/.zshrc
echo 'export DEEPSEEK_API_KEY=sk-your-api-key' >> ~/.bashrc
source ~/.bashrc

mvn test -Dtest=DeepSeekAIAdapterTest
```

### 示例 3: 在 IDE 中运行

**IntelliJ IDEA**:
1. 打开 `DeepSeekAIAdapterTest.java`
2. 右键 → Run → Edit Configurations
3. 添加环境变量：`DEEPSEEK_API_KEY=sk-your-api-key`
4. 运行测试

**VS Code**:
```json
// .vscode/launch.json
{
  "configurations": [
    {
      "type": "java",
      "name": "DeepSeek Test",
      "request": "launch",
      "mainClass": "DeepSeekAIAdapterTest",
      "env": {
        "DEEPSEEK_API_KEY": "sk-your-api-key"
      }
    }
  ]
}
```

### 示例 4: CI/CD 配置

**GitHub Actions**:
```yaml
name: Test with DeepSeek

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          
      - name: Run Tests
        env:
          DEEPSEEK_API_KEY: ${{ secrets.DEEPSEEK_API_KEY }}
        run: mvn test -Dtest=DeepSeekAIAdapterTest
```

**GitLab CI**:
```yaml
test:
  script:
    - mvn test -Dtest=DeepSeekAIAdapterTest
  variables:
    DEEPSEEK_API_KEY: $DEEPSEEK_API_KEY
```

---

## 🔍 测试输出解释

### 无 API Key 时：

```
⚠️  未配置 DEEPSEEK_API_KEY 环境变量，跳过真实 API 测试

[INFO] Running top.yumbo.ai.reviewer.adapter.output.ai.DeepSeekAIAdapterTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 2, Time elapsed: 3.5 s
```

**说明**:
- ⏭️ Skipped: 2 - 跳过了 2 个需要真实 API 的测试
- ✅ 其他 28 个测试通过

### 有 API Key 时：

```
✅ 使用真实的 DeepSeek API Key 进行集成测试

[INFO] Running top.yumbo.ai.reviewer.adapter.output.ai.DeepSeekAIAdapterTest

[真实API测试] 
✅ AI 分析结果: 标准的 Hello World 程序，结构清晰，无明显问题
✅ API 可用

[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.3 s
```

**说明**:
- ✅ Skipped: 0 - 所有测试都运行
- 🚀 包括真实 API 调用
- ⏱️ 时间较长（因为调用了真实 API）

---

## ⚠️ 常见问题

### Q1: 测试总是跳过集成测试？

**A**: 检查环境变量是否正确设置

```bash
# Windows
echo %DEEPSEEK_API_KEY%

# Linux/Mac
echo $DEEPSEEK_API_KEY
```

如果输出为空或 `test-api-key`，说明没有正确配置。

### Q2: API 调用失败？

**A**: 可能的原因：
1. API Key 无效或过期
2. 网络连接问题
3. API 配额用完
4. 账户被限制

**检查方法**:
```bash
# 测试 API 连接
curl -X POST https://api.deepseek.com/v1/chat/completions \
  -H "Authorization: Bearer $DEEPSEEK_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"deepseek-chat","messages":[{"role":"user","content":"Hello"}]}'
```

### Q3: 测试运行很慢？

**A**: 如果配置了真实 API Key，集成测试会调用真实 API，需要等待响应。

**解决方案**:
- 本地开发时不配置 API Key（只运行单元测试，3-5秒）
- 发布前配置 API Key（运行完整测试，10-15秒）

### Q4: 如何只运行单元测试？

**A**: 不设置环境变量即可：

```bash
# 确保没有设置 API Key
set DEEPSEEK_API_KEY=

# 运行测试（只会运行单元测试）
mvn test -Dtest=DeepSeekAIAdapterTest
```

### Q5: 如何只运行集成测试？

**A**: 使用 Maven 的 test 过滤：

```bash
set DEEPSEEK_API_KEY=sk-your-api-key
mvn test -Dtest=DeepSeekAIAdapterTest#shouldAnalyzeSimpleCodeWithRealAPI
```

---

## 📊 测试对比

| 场景 | 配置 API Key | 运行时间 | 测试数量 | 跳过数量 | 适用场景 |
|------|-------------|---------|---------|---------|---------|
| 快速开发 | ❌ 否 | 3-5秒 | 28 | 2 | 本地开发 |
| 完整验证 | ✅ 是 | 10-15秒 | 30 | 0 | 发布前、CI/CD |
| 调试集成 | ✅ 是 | 1-2秒 | 1-2 | 28 | 调试 API 问题 |

---

## 🎯 最佳实践

### ✅ 推荐做法

1. **本地开发** - 不配置 API Key，快速验证逻辑
2. **提交前** - 配置 API Key，运行完整测试
3. **CI/CD** - 从 Secret 读取 API Key，自动测试
4. **定期验证** - 每周至少运行一次完整测试

### ❌ 避免做法

1. **不要**在代码中硬编码 API Key
2. **不要**提交包含 API Key 的配置文件
3. **不要**在公共 CI 日志中暴露 API Key
4. **不要**共享个人 API Key

---

## 🔐 安全建议

1. **使用环境变量** - 永远不要硬编码
2. **定期轮换** - 定期更新 API Key
3. **限制权限** - 使用只读或受限的 API Key
4. **监控使用** - 定期检查 API 使用量
5. **保护 Secret** - 在 CI/CD 中使用 Secret 管理

---

## 📚 相关文档

- [DeepSeek 测试改进完整报告](./20251112071600-DEEPSEEK-TEST-IMPROVEMENT.md)
- [DeepSeek 官方文档](https://platform.deepseek.com/docs)
- [项目 README](../README.md)

---

**创建时间**: 2025-11-12 07:16:00  
**作者**: GitHub Copilot (世界顶级架构师)

**立即开始使用真实 API 测试吧！** 🚀

