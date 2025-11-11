package top.yumbo.ai.reviewer.exception;

/**
 * 分析异常类
 */
public class AnalysisException extends Exception {

    private final ErrorType errorType;

    public AnalysisException(String message) {
        super(message);
        this.errorType = ErrorType.GENERAL;
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.GENERAL;
    }

    public AnalysisException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public AnalysisException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        GENERAL("通用错误"),
        FILE_NOT_FOUND("文件未找到"),
        CONFIG_ERROR("配置错误"),
        AI_SERVICE_ERROR("AI服务错误"),
        PARSING_ERROR("解析错误"),
        NETWORK_ERROR("网络错误"),
        PERMISSION_ERROR("权限错误");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
