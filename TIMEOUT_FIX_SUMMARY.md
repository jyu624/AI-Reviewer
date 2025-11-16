# Timeout Configuration Fix Summary

## Issues Fixed

### 1. MaxTokens Configuration Not Loading (20000 → 2000)
**Problem**: Configuration was set to `max-tokens: 20000` in `application.yml`, but it was showing as `maxTokens=2000` in the logs.

**Root Cause**: Missing `@NestedConfigurationProperty` annotation on the `ai` field in `AIReviewerProperties` class. Spring Boot's `@ConfigurationProperties` requires this annotation when binding nested objects that are not inner classes.

**Fix**: Added `@NestedConfigurationProperty` annotation in `AIReviewerProperties.java`:
```java
@NestedConfigurationProperty
private AIConfig ai = new AIConfig();
```

**Location**: `ai-reviewer-starter/src/main/java/top/yumbo/ai/starter/config/AIReviewerProperties.java`

---

### 2. AWS Bedrock Read Timeout Error
**Problem**: Application was experiencing `SocketTimeoutException: Read timed out` when calling AWS Bedrock API, even though `timeout-seconds: 120` was configured.

**Root Cause**: The AWS SDK has multiple timeout configurations:
- **API call timeout**: Total time for the entire API call
- **API call attempt timeout**: Time for each individual attempt  
- **Socket timeout**: Time to wait for data to be read from the socket (was missing)

The code was only setting API call timeouts but not the socket read timeout.

**Fix**: Configured the Apache HTTP client with socket timeout in `BedrockAdapter.java`:
```java
software.amazon.awssdk.http.apache.ApacheHttpClient.Builder httpClientBuilder = 
    software.amazon.awssdk.http.apache.ApacheHttpClient.builder()
        .socketTimeout(java.time.Duration.ofSeconds(config.getTimeoutSeconds()))
        .connectionTimeout(java.time.Duration.ofSeconds(30));

var clientBuilder = BedrockRuntimeClient.builder()
        .region(Region.of(config.getRegion()))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .httpClient(httpClientBuilder.build());
```

**Location**: `application-demo/hackathonApplication/src/main/java/top/yumbo/ai/application/hackathon/ai/BedrockAdapter.java`

---

## Configuration Details

Current working configuration in `application.yml`:
```yaml
ai:
  provider: bedrock
  region: us-east-1
  model: "arn:aws:bedrock:us-east-1:590184013141:inference-profile/us.anthropic.claude-sonnet-4-5-20250929-v1:0"
  temperature: 0.7
  max-tokens: 20000        # Now properly loaded
  timeout-seconds: 120     # Now applied to socket timeout
  max-retries: 3
```

## Timeout Configuration Hierarchy

The Bedrock adapter now configures three levels of timeout:
1. **Socket Timeout**: 120 seconds (from config) - Time to read data from socket
2. **Connection Timeout**: 30 seconds (hardcoded) - Time to establish connection
3. **API Call Timeout**: 120 seconds (from config) - Total time for entire API call
4. **API Call Attempt Timeout**: 120 seconds (from config) - Time per retry attempt

## Testing

After these fixes:
- ✅ Configuration properly loads `maxTokens=20000` instead of `2000`
- ✅ Socket timeout is set to 120 seconds
- ✅ Build successful with no errors
- ✅ Application should now handle longer Bedrock API responses without timing out

## Next Steps

If you still experience timeout issues:
1. Increase `timeout-seconds` in `application.yml` (e.g., to 180 or 300)
2. Consider reducing `max-tokens` to get faster responses
3. Check AWS Bedrock service status and quotas
4. Monitor the actual response times in logs

## Date
2025-11-16

