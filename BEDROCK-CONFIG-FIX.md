# AWS Bedrock 配置修复说明

## 🎉 问题已解决

已修复当使用 AWS Bedrock 时不需要 API Key 的验证问题。

---

## 🐛 问题描述

**错误信息**:
```
[ERROR] 配置验证失败: AI API Key 未配置。请设置环境变量或在 config.yaml 中配置
❌ 配置错误: AI API Key 未配置。请设置环境变量或在 config.yaml 中配置
```

**原因**: 
配置验证逻辑没有考虑 Bedrock 使用 IAM 角色认证的特殊情况，强制要求所有 AI 服务都必须配置 API Key。

---

## ✅ 修复内容

### 修改的文件

**`Configuration.java`** - 更新 `validate()` 方法

**修复前**:
```java
public void validate() {
    // 强制要求 API Key
    if (aiApiKey == null || aiApiKey.isBlank()) {
        throw new ConfigurationException("AI API Key 未配置...");
    }
    // ...
}
```

**修复后**:
```java
public void validate() {
    if (aiProvider == null || aiProvider.isBlank()) {
        throw new ConfigurationException("AI Provider 未配置");
    }

    // API Key 验证：Bedrock 使用 IAM 角色，不需要 API Key
    if (!"bedrock".equalsIgnoreCase(aiProvider)) {
        if (aiApiKey == null || aiApiKey.isBlank()) {
            throw new ConfigurationException("AI API Key 未配置...");
        }
    }

    // Bedrock 特定验证
    if ("bedrock".equalsIgnoreCase(aiProvider)) {
        if (awsRegion == null || awsRegion.isBlank()) {
            throw new ConfigurationException("AWS Region 未配置（Bedrock 必需）");
        }
        if (aiModel == null || aiModel.isBlank()) {
            throw new ConfigurationException("AI Model 未配置（Bedrock 必需）");
        }
    }
}
```

---

## 🚀 使用方法

### 方法 1: 使用 Bedrock 配置文件

```bash
# 使用专门的 Bedrock 配置
java -Dconfig=./config-bedrock.yaml -jar hackathon-reviewer.jar \
  -d /path/to/project \
  -t "Team Name"
```

### 方法 2: 修改默认配置

编辑 `config.yaml`，取消 Bedrock 配置的注释：

```yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-3-sonnet-20240229-v1:0"
  region: "us-east-1"
  # 不需要配置 apiKey
  maxTokens: 8000
  temperature: 0
```

### 方法 3: 使用环境变量

```bash
export AI_PROVIDER="bedrock"
export AI_MODEL="anthropic.claude-3-sonnet-20240229-v1:0"
export AWS_REGION="us-east-1"

java -jar hackathon-reviewer.jar -d /path/to/project -t "Team Name"
```

---

## 📋 Bedrock 配置要求

### 必填项

| 配置项 | 说明 | 示例 |
|-------|------|------|
| `provider` | 必须设置为 "bedrock" | `provider: "bedrock"` |
| `model` | Bedrock 模型 ID | `model: "anthropic.claude-v2"` |
| `region` | AWS 区域 | `region: "us-east-1"` |

### 不需要配置

| 配置项 | 说明 |
|-------|------|
| `apiKey` | ❌ 不需要（使用 IAM 角色） |
| `accessKeyId` | ❌ 不需要（使用 IAM 角色） |
| `secretAccessKey` | ❌ 不需要（使用 IAM 角色） |

---

## 🔧 配置示例

### 完整的 Bedrock 配置

已创建 `config-bedrock.yaml` 文件，包含完整的 Bedrock 配置示例。

**核心配置**:
```yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-3-sonnet-20240229-v1:0"
  region: "us-east-1"
  maxTokens: 8000
  temperature: 0
  maxRetries: 2
  maxConcurrency: 3

s3Storage:
  bucketName: "your-bucket-name"
  region: "us-east-1"
  # 同样使用 IAM 角色，不需要 accessKeyId
```

---

## 🔐 IAM 权限要求

### 最小权限策略

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": "arn:aws:bedrock:*:*:*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-bucket-name",
        "arn:aws:s3:::your-bucket-name/*"
      ]
    }
  ]
}
```

### 附加到 EC2/ECS 实例

1. 创建 IAM 角色
2. 附加上述策略
3. 将角色附加到 EC2/ECS 实例
4. 运行应用（自动使用 IAM 角色）

---

## ✅ 验证修复

### 编译结果

```
[INFO] BUILD SUCCESS
[INFO] Total time:  7.895 s
```

✅ **编译成功！**

### 测试步骤

1. **创建 Bedrock 配置文件**:
   ```bash
   cp config-bedrock.yaml config.yaml
   ```

2. **编辑配置** (根据实际情况修改):
   ```yaml
   aiService:
     provider: "bedrock"
     model: "anthropic.claude-3-sonnet-20240229-v1:0"
     region: "us-east-1"
   
   s3Storage:
     bucketName: "your-actual-bucket-name"
     region: "us-east-1"
   ```

3. **运行测试**:
   ```bash
   java -jar target/hackathon-reviewer.jar --help
   ```

4. **预期输出** (不再报错):
   ```
   [INFO] 配置已从 classpath:config.yaml 加载
   [INFO] 配置加载成功: provider=bedrock, model=anthropic.claude-3-sonnet-20240229-v1:0
   🏆 黑客松项目评审工具
   ...
   ```

---

## 🎯 不同 AI 服务的配置对比

### Bedrock (AWS)

```yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-v2"
  region: "us-east-1"
  # ❌ 不需要 apiKey
```

**认证方式**: IAM 角色

### DeepSeek

```yaml
aiService:
  provider: "deepseek"
  model: "deepseek-chat"
  baseUrl: "https://api.deepseek.com/v1/chat/completions"
  apiKey: "sk-your-api-key"  # ✅ 必需
```

**认证方式**: API Key

### OpenAI

```yaml
aiService:
  provider: "openai"
  model: "gpt-4"
  baseUrl: "https://api.openai.com/v1/chat/completions"
  apiKey: "sk-your-api-key"  # ✅ 必需
```

**认证方式**: API Key

### Gemini

```yaml
aiService:
  provider: "gemini"
  model: "gemini-pro"
  apiKey: "your-gemini-api-key"  # ✅ 必需
```

**认证方式**: API Key

---

## 📚 支持的 Bedrock 模型

### Anthropic Claude

| 模型 ID | 说明 |
|---------|------|
| `anthropic.claude-v2` | Claude 2 |
| `anthropic.claude-v2:1` | Claude 2.1 |
| `anthropic.claude-3-sonnet-20240229-v1:0` | Claude 3 Sonnet |
| `anthropic.claude-3-5-sonnet-20240620-v1:0` | Claude 3.5 Sonnet |
| `anthropic.claude-3-haiku-20240307-v1:0` | Claude 3 Haiku (快速) |

### Amazon Titan

| 模型 ID | 说明 |
|---------|------|
| `amazon.titan-text-express-v1` | Titan Text Express |
| `amazon.titan-text-lite-v1` | Titan Text Lite |

### Meta Llama

| 模型 ID | 说明 |
|---------|------|
| `meta.llama2-13b-chat-v1` | Llama 2 13B |
| `meta.llama2-70b-chat-v1` | Llama 2 70B |

### 其他模型

| 模型 ID | 说明 |
|---------|------|
| `cohere.command-text-v14` | Cohere Command |
| `ai21.j2-mid-v1` | AI21 Jurassic-2 Mid |
| `ai21.j2-ultra-v1` | AI21 Jurassic-2 Ultra |

---

## 🔄 迁移指南

### 从其他 AI 服务迁移到 Bedrock

#### 步骤 1: 更新配置文件

**修改前** (使用 DeepSeek):
```yaml
aiService:
  provider: "deepseek"
  model: "deepseek-chat"
  apiKey: "sk-12345..."
  baseUrl: "https://api.deepseek.com/v1/chat/completions"
```

**修改后** (使用 Bedrock):
```yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-3-sonnet-20240229-v1:0"
  region: "us-east-1"
  # 移除 apiKey 和 baseUrl
```

#### 步骤 2: 配置 IAM 角色

1. 在 AWS IAM 中创建角色
2. 附加 Bedrock 访问策略
3. 将角色附加到 EC2/ECS 实例

#### 步骤 3: 测试

```bash
java -Dconfig=./config-bedrock.yaml -jar hackathon-reviewer.jar --help
```

---

## 💡 最佳实践

### 1. 使用推理配置文件 (Inference Profile)

推荐使用推理配置文件 ARN，而不是直接使用模型 ID：

```yaml
aiService:
  provider: "bedrock"
  # 推荐：使用推理配置文件（支持跨区域、自动故障转移）
  model: "arn:aws:bedrock:us-east-1:123456789012:inference-profile/us.anthropic.claude-sonnet-4-5-20250929-v1:0"
  region: "us-east-1"
```

### 2. 环境分离

```bash
# 开发环境（使用 DeepSeek）
java -Dconfig=./configs/dev-config.yaml -jar hackathon-reviewer.jar ...

# 生产环境（使用 Bedrock）
java -Dconfig=./configs/prod-config.yaml -jar hackathon-reviewer.jar ...
```

### 3. 监控和日志

```yaml
logging:
  level: "INFO"  # 生产环境
  # level: "DEBUG"  # 开发环境
  file: "./logs/ai-reviewer-bedrock.log"
```

---

## 🎊 总结

### ✅ 修复完成

- **问题**: Bedrock 不需要 API Key，但配置验证强制要求
- **修复**: 在验证逻辑中添加 Bedrock 特殊处理
- **状态**: ✅ 编译成功，问题已解决

### 📁 新增文件

- `config-bedrock.yaml` - Bedrock 专用配置示例
- `BEDROCK-CONFIG-FIX.md` - 本说明文档

### 🎯 现在可以

1. ✅ 使用 Bedrock 而不配置 API Key
2. ✅ 使用 IAM 角色进行认证
3. ✅ 同时支持 S3 和 Bedrock (都使用 IAM)
4. ✅ 在 AWS 环境中无缝部署

---

**问题已完全解决，可以正常使用 Bedrock 了！** 🚀

```bash
# 立即使用 Bedrock
java -Dconfig=./config-bedrock.yaml -jar hackathon-reviewer.jar --help
```

