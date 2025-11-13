# AWS S3 存储服务 - 快速参考

## 快速开始（5 分钟）

### 1. 配置 (config.yaml)
```yaml
s3Storage:
  region: "us-east-1"
  bucketName: "my-project-bucket"
  maxConcurrency: 10
```
**不需要 apiKey - 自动使用 IAM 角色！**

### 2. 代码示例
```java
// 创建服务
S3StorageConfig config = S3StorageConfig.builder()
    .region("us-east-1")
    .bucketName("my-bucket")
    .build();
S3StorageAdapter adapter = new S3StorageAdapter(config);
S3StorageService service = new S3StorageService(adapter);

// 下载项目
S3DownloadResult result = service.downloadProjectForReview(
    "my-bucket",
    "projects/my-app/",
    Paths.get("./temp")
);

System.out.println(result);
service.shutdown();
```

### 3. IAM 策略
```json
{
  "Effect": "Allow",
  "Action": ["s3:GetObject", "s3:ListBucket"],
  "Resource": ["arn:aws:s3:::my-bucket/*"]
}
```

---

## 核心 API

### S3StorageService (应用服务层)

| 方法 | 说明 | 返回 |
|------|------|------|
| `downloadProjectForReview(bucket, prefix, localDir)` | 下载项目到本地 | S3DownloadResult |
| `downloadProjectToMemory(bucket, prefix)` | 下载项目到内存 | S3DownloadResult |
| `listSourceCodeFiles(bucket, prefix)` | 列出源代码文件 | List<S3File> |
| `listConfigFiles(bucket, prefix)` | 列出配置文件 | List<S3File> |
| `getProjectStatistics(bucket, prefix)` | 获取项目统计 | String |
| `uploadReviewReport(bucket, key, content)` | 上传报告 | void |

### S3StorageAdapter (适配器层)

| 方法 | 说明 | 返回 |
|------|------|------|
| `listFiles(bucket, prefix)` | 列出文件 | List<S3File> |
| `downloadFile(bucket, key)` | 下载单个文件 | S3File |
| `downloadFolder(bucket, prefix)` | 下载文件夹 | S3DownloadResult |
| `downloadFolderToDirectory(bucket, prefix, dir)` | 下载到本地 | S3DownloadResult |
| `fileExists(bucket, key)` | 检查文件存在 | boolean |
| `getFileMetadata(bucket, key)` | 获取元数据 | S3File |
| `uploadFile(bucket, key, content)` | 上传文件 | void |
| `deleteFile(bucket, key)` | 删除文件 | void |

---

## 领域模型

### S3File
```java
S3File {
    String key;              // S3 键
    String fileName;         // 文件名
    long sizeInBytes;        // 大小
    Instant lastModified;    // 修改时间
    byte[] content;          // 内容（可选）
    
    // 方法
    boolean isDirectory()
    boolean isSourceCode()
    boolean isConfigFile()
    double getSizeInKB()
    double getSizeInMB()
}
```

### S3DownloadResult
```java
S3DownloadResult {
    List<S3File> files;
    int totalFileCount;
    int successCount;
    int failureCount;
    long totalSizeInBytes;
    List<String> errors;
    
    // 方法
    boolean isSuccess()
    double getSuccessRate()
    double getTotalSizeInMB()
    double getDownloadSpeedMBps()
    long getDurationMillis()
}
```

---

## 常用代码片段

### 1. 基本下载
```java
S3StorageService service = new S3StorageService(adapter);
S3DownloadResult result = service.downloadProjectForReview(
    "my-bucket",
    "projects/my-app/",
    Paths.get("./temp")
);
System.out.println("成功: " + result.getSuccessCount());
```

### 2. 列出文件
```java
List<S3File> sourceFiles = service.listSourceCodeFiles(
    "my-bucket",
    "projects/my-app/"
);
sourceFiles.forEach(f -> 
    System.out.println(f.getFileName() + " - " + f.getSizeInKB() + " KB")
);
```

### 3. 异步下载
```java
service.downloadProjectForReviewAsync(bucket, prefix, localDir)
    .thenAccept(result -> {
        System.out.println("下载完成: " + result);
    });
```

### 4. 上传报告
```java
String reportJson = "{...}";
service.uploadReviewReport(
    "my-bucket",
    "reports/review-" + System.currentTimeMillis() + ".json",
    reportJson.getBytes()
);
```

### 5. 检查文件
```java
if (adapter.fileExists("my-bucket", "path/to/file.txt")) {
    S3File file = adapter.downloadFile("my-bucket", "path/to/file.txt");
    System.out.println(new String(file.getContent()));
}
```

### 6. 获取统计
```java
String stats = service.getProjectStatistics("my-bucket", "projects/my-app/");
System.out.println(stats);
// 输出:
// 项目统计 [Bucket: my-bucket, Prefix: projects/my-app/]
// 总文件数: 150
// 源代码文件: 120
// 配置文件: 15
// 总大小: 5.32 MB
```

---

## 配置选项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `region` | us-east-1 | AWS 区域 |
| `bucketName` | null | 默认存储桶 |
| `accessKeyId` | null | Access Key（留空使用 IAM） |
| `secretAccessKey` | null | Secret Key（留空使用 IAM） |
| `maxConcurrency` | 10 | 最大并发数 |
| `connectTimeout` | 30000 | 连接超时（ms） |
| `readTimeout` | 60000 | 读取超时（ms） |
| `maxRetries` | 3 | 最大重试次数 |
| `retryDelay` | 1000 | 重试延迟（ms） |
| `useAccelerateEndpoint` | false | 使用加速端点 |
| `usePathStyleAccess` | false | 路径样式访问 |

---

## 错误处理

| 异常 | 原因 | 解决 |
|------|------|------|
| `Unable to load credentials` | IAM 角色未配置 | 附加 IAM 角色到实例 |
| `Access Denied` | 权限不足 | 检查 IAM 策略 |
| `NoSuchBucket` | 存储桶不存在 | 检查名称和区域 |
| `NoSuchKey` | 文件不存在 | 检查文件路径 |

---

## 最佳实践

### ✅ 推荐
```java
// 1. 始终关闭资源
try {
    // 使用服务
} finally {
    service.shutdown();
}

// 2. 大文件下载到磁盘
service.downloadProjectForReview(bucket, prefix, localDir);

// 3. 分批处理
List<S3File> files = adapter.listFiles(bucket, prefix);
files.stream().limit(100).forEach(file -> {
    // 处理文件
});
```

### ❌ 避免
```java
// 1. 忘记关闭
service.downloadProject(...);
// service.shutdown(); ← 忘记调用

// 2. 大文件加载到内存
S3DownloadResult result = service.downloadProjectToMemory(...);
// 可能导致 OOM

// 3. 同步下载大量文件
for (String key : keys) {
    adapter.downloadFile(bucket, key); // 太慢
}
```

---

## 性能调优

### 小文件多并发
```yaml
s3Storage:
  maxConcurrency: 50
```

### 大文件低并发
```yaml
s3Storage:
  maxConcurrency: 5
  readTimeout: 120000
```

### 使用加速端点
```yaml
s3Storage:
  useAccelerateEndpoint: true
```

---

## 故障排查

### 1. 检查凭证
```bash
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/
```

### 2. 测试连接
```java
boolean available = adapter.isAvailable();
System.out.println("S3 可用: " + available);
```

### 3. 启用调试
```yaml
logging:
  level: "DEBUG"
```

---

## 依赖

### pom.xml
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.21.0</version>
</dependency>
```

---

## 文件位置

| 文件 | 路径 |
|------|------|
| Domain Models | `src/main/java/.../domain/model/` |
| Output Port | `src/main/java/.../application/port/output/S3StoragePort.java` |
| Adapter | `src/main/java/.../adapter/output/storage/S3StorageAdapter.java` |
| Service | `src/main/java/.../application/service/S3StorageService.java` |
| Config | `config-s3-iam.yaml` |
| Test | `src/test/java/.../adapter/output/storage/S3StorageAdapterTest.java` |

---

## 相关文档

- 详细指南: [AWS-S3-INTEGRATION-GUIDE.md](./AWS-S3-INTEGRATION-GUIDE.md)
- IAM 配置: [AWS-BEDROCK-IAM-SETUP.md](./AWS-BEDROCK-IAM-SETUP.md)

