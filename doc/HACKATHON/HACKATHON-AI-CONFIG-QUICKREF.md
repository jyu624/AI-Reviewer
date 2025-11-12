# 黑客松 AI 配置 - 快速参考卡

> **一页搞定所有配置** 🚀

---

## 🎯 三步配置

### 1️⃣ 选择 AI 服务

| AI 服务 | Provider 名称 | 获取 API Key |
|---------|--------------|-------------|
| DeepSeek（推荐） | `deepseek` | https://platform.deepseek.com/ |
| OpenAI | `openai` | https://platform.openai.com/ |
| Claude | `claude` | https://console.anthropic.com/ |
| Gemini | `gemini` | https://makersuite.google.com/ |
| AWS Bedrock | `bedrock` | AWS Console |

---

### 2️⃣ 设置环境变量

**Windows**:
```cmd
set AI_PROVIDER=deepseek
set AI_API_KEY=your-api-key-here
```

**Linux/Mac**:
```bash
export AI_PROVIDER=deepseek
export AI_API_KEY=your-api-key-here
```

---

### 3️⃣ 运行黑客松

```bash
java -jar hackathon-ai.jar hackathon \
  --github-url https://github.com/user/repo \
  --team "Team Name" \
  --output score.json
```

✅ **完成！**

---

## 📋 常用配置速查

### DeepSeek（默认，推荐）
```cmd
set AI_PROVIDER=deepseek
set AI_API_KEY=sk-xxx
```

### OpenAI GPT-4
```cmd
set AI_PROVIDER=openai
set AI_API_KEY=sk-proj-xxx
set AI_MODEL=gpt-4
```

### Claude 3
```cmd
set AI_PROVIDER=claude
set AI_API_KEY=sk-ant-xxx
set AI_MODEL=claude-3-sonnet-20240229
```

### Gemini
```cmd
set AI_PROVIDER=gemini
set AI_API_KEY=your-google-key
```

### AWS Bedrock
```cmd
set AI_PROVIDER=bedrock
set AWS_REGION=us-east-1
set AWS_ACCESS_KEY_ID=xxx
set AWS_SECRET_ACCESS_KEY=xxx
```

---

## ⚡ 高级用法

### 使用系统属性（临时覆盖）
```bash
java -Dai.provider=openai \
     -Dai.apiKey=sk-xxx \
     -jar hackathon-ai.jar hackathon ...
```

### 使用本地目录（不需要 Git）
```bash
java -jar hackathon-ai.jar hackathon \
  --directory "D:\Projects\my-project" \
  --team "Local Team"
```

### 使用配置文件
创建 `config.yaml`:
```yaml
aiService:
  provider: deepseek
  apiKey: your-key
  model: deepseek-chat
```

---

## 🔧 完整参数表

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| AI_PROVIDER | deepseek | AI 服务商 |
| AI_API_KEY | - | API 密钥（必需） |
| AI_MODEL | 自动 | 模型名称 |
| AI_MAX_TOKENS | 4000 | 最大 Token |
| AI_TEMPERATURE | 0.3 | 温度（0-2） |
| AI_MAX_RETRIES | 3 | 重试次数 |

---

## ❓ 常见问题

**Q: 如何验证配置？**  
A: 查看启动日志：
```
[INFO] AI 服务: deepseek (model: deepseek-chat)
```

**Q: API Key 错误怎么办？**  
A: 检查：
1. Key 是否正确复制
2. 是否有额度
3. 网络能否访问 API

**Q: 如何切换 AI？**  
A: 只需修改环境变量：
```cmd
set AI_PROVIDER=openai
set AI_API_KEY=new-key
```

---

## 📞 获取帮助

```bash
# 查看帮助
java -jar hackathon-ai.jar hackathon --help

# 查看版本
java -jar hackathon-ai.jar --version
```

---

**完整文档**: [20251113010000-HACKATHON-AI-CONFIG-GUIDE.md](./20251113010000-HACKATHON-AI-CONFIG-GUIDE.md)

**版本**: 2.0 | **更新**: 2025-11-13

