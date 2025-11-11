package top.yumbo.ai.refactor.domain.model;

/**
 * 项目类型枚举
 */
public enum ProjectType {

    JAVA("java", "Java项目"),
    PYTHON("python", "Python项目"),
    JAVASCRIPT("javascript", "JavaScript项目"),
    TYPESCRIPT("typescript", "TypeScript项目"),
    GO("go", "Go项目"),
    RUST("rust", "Rust项目"),
    CSHARP("csharp", "C#项目"),
    CPP("cpp", "C++项目"),
    MIXED("mixed", "混合语言项目"),
    UNKNOWN("unknown", "未知类型");

    private final String primaryLanguage;
    private final String displayName;

    ProjectType(String primaryLanguage, String displayName) {
        this.primaryLanguage = primaryLanguage;
        this.displayName = displayName;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据文件扩展名推断项目类型
     */
    public static ProjectType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNKNOWN;
        }
        // 移除开头的点号
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return switch (ext.toLowerCase()) {
            case "java" -> JAVA;
            case "py" -> PYTHON;
            case "js", "jsx" -> JAVASCRIPT;
            case "ts", "tsx" -> TYPESCRIPT;
            case "go" -> GO;
            case "rs" -> RUST;
            case "cs" -> CSHARP;
            case "cpp", "cc", "cxx", "hpp", "h" -> CPP;
            default -> UNKNOWN;
        };
    }
}

