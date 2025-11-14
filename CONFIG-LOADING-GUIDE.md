# 配置文件加载 - 使用指南

## 概述

`ConfigurationLoader` 现在支持通过 `-Dconfig` 系统属性指定自定义配置文件路径。

---

## 🚀 使用方法

### 方法 1: 使用 -Dconfig 参数（推荐）

```bash
# 指定绝对路径
java -Dconfig=/path/to/custom-config.yaml -jar hackathon-reviewer.jar -d /path/to/project

# 指定相对路径
java -Dconfig=./configs/prod-config.yaml -jar hackathon-reviewer.jar -d /path/to/project

# Windows 路径
java -Dconfig=D:\configs\hackathon-config.yaml -jar hackathon-reviewer.jar -d C:\projects\my-app
```

### 方法 2: 使用默认 config.yaml

```bash
# 从 classpath 加载（JAR 内部的 config.yaml）
java -jar hackathon-reviewer.jar -d /path/to/project

# 从当前目录加载（与 JAR 同目录的 config.yaml）
java -jar hackathon-reviewer.jar -d /path/to/project
```

---

## 📋 配置加载优先级

加载顺序（优先级从高到低）：

### 1. 系统属性 `-Dconfig`（最高优先级）

```bash
java -Dconfig=/custom/path/config.yaml -jar hackathon-reviewer.jar ...
```

**特点**：
- ✅ 可以指定任意路径的配置文件
- ✅ 支持绝对路径和相对路径
- ✅ 如果文件不存在，会降级到其他方式
- ✅ 适合不同环境使用不同配置

### 2. Classpath 中的 config.yaml

```
hackathon-reviewer.jar
  └── config.yaml  (打包在 JAR 内部)
```

**特点**：
- ✅ 默认配置，随 JAR 分发
- ✅ 适合生产环境的默认配置
- ✅ 用户无需手动创建配置文件

### 3. 当前目录的 config.yaml

```
./
├── hackathon-reviewer.jar
└── config.yaml  (与 JAR 同目录)
```

**特点**：
- ✅ 方便本地开发和测试
- ✅ 可以覆盖 JAR 内部的配置
- ✅ 无需重新打包 JAR

### 4. 默认配置

如果以上都找不到，使用代码中的默认值。

---

## 🎯 使用场景

### 场景 1: 开发环境

```bash
# 使用开发配置
java -Dconfig=./configs/dev-config.yaml -jar hackathon-reviewer.jar \
  -d /path/to/project \
  -t "Dev Team"
```

**dev-config.yaml**:
```yaml
aiService:
  provider: "deepseek"
  model: "deepseek-chat"
  apiKey: "sk-dev-key-12345"

s3Storage:
  bucketName: "dev-hackathon-bucket"
  region: "us-east-1"

logging:
  level: "DEBUG"
```

### 场景 2: 生产环境

```bash
# 使用生产配置
java -Dconfig=/etc/hackathon/prod-config.yaml -jar hackathon-reviewer.jar \
  -s projects/team-awesome/ \
  -t "Team Awesome"
```

**prod-config.yaml**:
```yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-v2"
  region: "us-east-1"

s3Storage:
  bucketName: "prod-hackathon-bucket"
  region: "us-east-1"

logging:
  level: "INFO"
```

### 场景 3: 测试环境

```bash
# 使用测试配置
java -Dconfig=./configs/test-config.yaml -jar hackathon-reviewer.jar \
  -z test-project.zip \
  -t "Test Team"
```

**test-config.yaml**:
```yaml
aiService:
  provider: "deepseek"
  model: "deepseek-chat"
  apiKey: "sk-test-key-67890"
  maxTokens: 2000  # 测试环境限制 token

s3Storage:
  bucketName: "test-hackathon-bucket"
  region: "us-east-1"

cache:
  enabled: false  # 测试环境禁用缓存
```

### 场景 4: 多区域部署

#### 美东区域
```bash
java -Dconfig=/etc/hackathon/us-east-config.yaml -jar hackathon-reviewer.jar ...
```

**us-east-config.yaml**:
```yaml
aiService:
  provider: "bedrock"
  region: "us-east-1"

s3Storage:
  bucketName: "hackathon-us-east"
  region: "us-east-1"
```

#### 欧洲区域
```bash
java -Dconfig=/etc/hackathon/eu-west-config.yaml -jar hackathon-reviewer.jar ...
```

**eu-west-config.yaml**:
```yaml
aiService:
  provider: "bedrock"
  region: "eu-west-1"

s3Storage:
  bucketName: "hackathon-eu-west"
  region: "eu-west-1"
```

---

## 📁 推荐的配置文件组织

### 单机部署

```
/opt/hackathon/
├── hackathon-reviewer.jar
├── config.yaml              # 默认配置
└── configs/
    ├── dev-config.yaml      # 开发配置
    ├── test-config.yaml     # 测试配置
    └── prod-config.yaml     # 生产配置
```

使用:
```bash
# 开发
java -Dconfig=configs/dev-config.yaml -jar hackathon-reviewer.jar ...

# 生产
java -Dconfig=configs/prod-config.yaml -jar hackathon-reviewer.jar ...
```

### 多环境部署

```
/etc/hackathon/
├── dev/
│   └── config.yaml
├── staging/
│   └── config.yaml
└── production/
    └── config.yaml

/opt/hackathon/
└── hackathon-reviewer.jar
```

使用:
```bash
# 开发环境
java -Dconfig=/etc/hackathon/dev/config.yaml -jar /opt/hackathon/hackathon-reviewer.jar ...

# 生产环境
java -Dconfig=/etc/hackathon/production/config.yaml -jar /opt/hackathon/hackathon-reviewer.jar ...
```

---

## 🔍 日志输出

### 使用自定义配置

```bash
$ java -Dconfig=./my-config.yaml -jar hackathon-reviewer.jar --help

2025-11-14 08:50:26 [INFO] ConfigurationLoader - 配置已从自定义路径加载: /home/user/my-config.yaml
2025-11-14 08:50:26 [INFO] ConfigurationLoader - 配置加载成功: provider=bedrock, model=claude-v2
```

### 配置文件不存在

```bash
$ java -Dconfig=./non-existent.yaml -jar hackathon-reviewer.jar --help

2025-11-14 08:50:26 [WARN] ConfigurationLoader - 自定义配置文件不存在: /home/user/non-existent.yaml，尝试其他路径
2025-11-14 08:50:26 [INFO] ConfigurationLoader - 配置已从 classpath:config.yaml 加载
```

### 使用默认配置

```bash
$ java -jar hackathon-reviewer.jar --help

2025-11-14 08:50:26 [INFO] ConfigurationLoader - 配置已从 classpath:config.yaml 加载
2025-11-14 08:50:26 [INFO] ConfigurationLoader - 配置加载成功: provider=deepseek, model=deepseek-chat
```

---

## 🎨 最佳实践

### 1. 环境变量 + 配置文件

```bash
# 配置文件中不包含敏感信息
# my-config.yaml
aiService:
  provider: "bedrock"
  model: "anthropic.claude-v2"
  # apiKey 不写在配置文件中

s3Storage:
  bucketName: "my-bucket"
  # accessKeyId 和 secretAccessKey 不写在配置文件中
```

```bash
# 通过环境变量提供敏感信息
export AI_API_KEY="sk-secret-key-12345"
export AWS_ACCESS_KEY_ID="AKIA..."
export AWS_SECRET_ACCESS_KEY="secret..."

# 运行
java -Dconfig=./my-config.yaml -jar hackathon-reviewer.jar ...
```

### 2. 配置文件模板

创建 `config.yaml.template`:
```yaml
aiService:
  provider: "${AI_PROVIDER}"
  model: "${AI_MODEL}"
  # apiKey 通过环境变量 AI_API_KEY 提供

s3Storage:
  bucketName: "${S3_BUCKET_NAME}"
  region: "${AWS_REGION}"
  # 凭证通过 IAM 角色或环境变量提供
```

用户复制并填写:
```bash
cp config.yaml.template config.yaml
# 编辑 config.yaml，填写实际值
```

### 3. Docker 部署

**Dockerfile**:
```dockerfile
FROM openjdk:17-slim

# 复制 JAR 和默认配置
COPY target/hackathon-reviewer.jar /app/
COPY config.yaml /app/config-default.yaml

WORKDIR /app

# 使用环境变量指定配置文件
ENTRYPOINT ["java", "-Dconfig=${CONFIG_FILE:-/app/config-default.yaml}", "-jar", "hackathon-reviewer.jar"]
```

**docker-compose.yml**:
```yaml
version: '3'
services:
  hackathon-reviewer:
    image: hackathon-reviewer:2.0
    environment:
      - CONFIG_FILE=/etc/hackathon/prod-config.yaml
      - AI_API_KEY=${AI_API_KEY}
      - AWS_REGION=us-east-1
    volumes:
      - ./configs/prod-config.yaml:/etc/hackathon/prod-config.yaml
```

### 4. Kubernetes 部署

**ConfigMap**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hackathon-config
data:
  config.yaml: |
    aiService:
      provider: "bedrock"
      model: "anthropic.claude-v2"
      region: "us-east-1"
    s3Storage:
      bucketName: "k8s-hackathon-bucket"
      region: "us-east-1"
```

**Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hackathon-reviewer
spec:
  template:
    spec:
      containers:
      - name: reviewer
        image: hackathon-reviewer:2.0
        command: ["java"]
        args: 
          - "-Dconfig=/etc/hackathon/config.yaml"
          - "-jar"
          - "/app/hackathon-reviewer.jar"
        volumeMounts:
        - name: config
          mountPath: /etc/hackathon
      volumes:
      - name: config
        configMap:
          name: hackathon-config
```

---

## ❓ 常见问题

### Q1: 配置文件路径支持什么格式？

**A**: 支持以下格式：
- 绝对路径: `/etc/hackathon/config.yaml`
- 相对路径: `./configs/config.yaml`
- Windows 路径: `D:\configs\config.yaml`
- 用户主目录: `~/configs/config.yaml` (需要展开)

### Q2: 配置文件不存在会怎样？

**A**: 会降级到下一个优先级：
1. 尝试从 classpath 加载
2. 尝试从当前目录加载
3. 使用默认配置

### Q3: 如何验证配置加载成功？

**A**: 查看日志输出：
```
[INFO] ConfigurationLoader - 配置已从自定义路径加载: /path/to/config.yaml
[INFO] ConfigurationLoader - 配置加载成功: provider=bedrock, model=claude-v2
```

### Q4: 可以同时使用配置文件和环境变量吗？

**A**: 可以！优先级为：
1. 系统属性 (`-D` 参数)
2. 环境变量
3. 配置文件
4. 默认值

### Q5: 配置文件支持哪些格式？

**A**: 目前只支持 YAML 格式（`.yaml` 或 `.yml`）

---

## 📊 配置优先级总结

```
最高优先级
    ↓
1. 系统属性 (-D 参数)
    ↓
2. 环境变量 (AI_API_KEY, AWS_REGION, etc.)
    ↓
3. -Dconfig 指定的配置文件
    ↓
4. classpath 中的 config.yaml
    ↓
5. 当前目录的 config.yaml
    ↓
6. 代码中的默认值
    ↓
最低优先级
```

---

## 🎉 总结

通过 `-Dconfig` 参数，您可以：

✅ **灵活部署**: 不同环境使用不同配置  
✅ **安全管理**: 配置文件与 JAR 分离  
✅ **方便维护**: 修改配置无需重新打包  
✅ **多环境支持**: 轻松切换开发/测试/生产配置  
✅ **向后兼容**: 不影响现有的默认配置加载方式

---

**立即体验新功能！**

```bash
java -Dconfig=./my-config.yaml -jar hackathon-reviewer.jar --help
```

