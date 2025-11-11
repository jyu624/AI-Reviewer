package top.yumbo.ai.reviewer.scanner;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.config.Config;
import top.yumbo.ai.reviewer.entity.SourceFile;
import top.yumbo.ai.reviewer.exception.AnalysisException;
import top.yumbo.ai.reviewer.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件扫描器 - 负责扫描和筛选项目文件
 */
@Slf4j
public class FileScanner {

    private final Config.FileScanConfig scanConfig;

    public FileScanner(Config config) {
        this.scanConfig = config.getFileScan();
    }

    /**
     * 扫描核心文件
     * @param projectRoot 项目根目录
     * @return 核心文件列表
     */
    public List<Path> scanCoreFiles(Path projectRoot) throws AnalysisException {
        log.info("开始扫描项目核心文件: {}", projectRoot);

        try {
            List<Path> allFiles = scanAllFiles(projectRoot);
            List<Path> coreFiles = filterCoreFiles(allFiles, projectRoot);

            log.info("扫描完成，共找到 {} 个文件，其中 {} 个为核心文件",
                    allFiles.size(), coreFiles.size());

            return coreFiles.stream()
                    .limit(scanConfig.getMaxFilesCount())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new AnalysisException("文件扫描失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成项目结构树
     * @param projectRoot 项目根目录
     * @return 项目结构字符串
     */
    public String generateProjectStructure(Path projectRoot) throws AnalysisException {
        log.info("生成项目结构树: {}", projectRoot);

        try {
            StringBuilder structure = new StringBuilder();
            structure.append(projectRoot.getFileName()).append("/\n");
            buildDirectoryTree(projectRoot, structure, "", 4); // 限制深度为4层

            return structure.toString();

        } catch (IOException e) {
            throw new AnalysisException("生成项目结构失败: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描所有文件
     */
    private List<Path> scanAllFiles(Path projectRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::shouldIncludeFile)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 筛选核心文件
     */
    private List<Path> filterCoreFiles(List<Path> allFiles, Path projectRoot) {
        return allFiles.stream()
                .filter(path -> isCoreFile(path, projectRoot))
                .sorted(this::compareFilePriority)
                .collect(Collectors.toList());
    }

    /**
     * 判断是否应该包���文件
     */
    private boolean shouldIncludeFile(Path path) {
        String fileName = path.getFileName().toString();

        // 检查排除模式
        for (String excludePattern : scanConfig.getExcludePatterns()) {
            if (matchesPattern(fileName, excludePattern)) {
                return false;
            }
        }

        // 检查包含模式
        for (String includePattern : scanConfig.getIncludePatterns()) {
            if (matchesPattern(fileName, includePattern)) {
                return true;
            }
        }

        // 检查文件大小
        try {
            long sizeKB = Files.size(path) / 1024;
            if (sizeKB > scanConfig.getMaxFileSize()) {
                log.debug("跳过大文件: {} ({} KB)", path, sizeKB);
                return false;
            }
        } catch (IOException e) {
            log.warn("无法获取文件大小: {}", path);
            return false;
        }

        return true;
    }

    /**
     * 判断是否为核心文件
     */
    private boolean isCoreFile(Path path, Path projectRoot) {
        String relativePath = projectRoot.relativize(path).toString();
        String fileName = path.getFileName().toString();

        // 检查核心文件模式
        for (String corePattern : scanConfig.getCoreFilePatterns()) {
            if (matchesPattern(fileName, corePattern) || matchesPattern(relativePath, corePattern)) {
                return true;
            }
        }

        // 特殊文件类型
        if (isEntryPoint(fileName) || isConfigFile(fileName) || isDocumentation(fileName)) {
            return true;
        }

        // 排除测试文件（如果配置了）
        if (!scanConfig.isIncludeTests() && isTestFile(relativePath)) {
            return false;
        }

        // 排除依赖目录（如果配置了）
        if (!scanConfig.isIncludeDependencies() && isDependencyDirectory(relativePath)) {
            return false;
        }

        return false;
    }

    /**
     * 文件优先级比较（核心文件优先）
     */
    private int compareFilePriority(Path path1, Path path2) {
        boolean core1 = isCoreFile(path1, path1.getParent());
        boolean core2 = isCoreFile(path2, path2.getParent());

        if (core1 && !core2) return -1;
        if (!core1 && core2) return 1;

        // 相同优先级，按路径排序
        return path1.compareTo(path2);
    }

    /**
     * 构建目录树
     */
    private void buildDirectoryTree(Path dir, StringBuilder structure, String prefix, int maxDepth) throws IOException {
        if (maxDepth <= 0) return;

        List<Path> children = Files.list(dir)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < children.size(); i++) {
            Path child = children.get(i);
            boolean isLast = (i == children.size() - 1);

            structure.append(prefix)
                    .append(isLast ? "└── " : "├── ")
                    .append(child.getFileName());

            if (Files.isDirectory(child)) {
                structure.append("/\n");
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                buildDirectoryTree(child, structure, newPrefix, maxDepth - 1);
            } else {
                structure.append("\n");
            }
        }
    }

    /**
     * 模式匹配
     */
    private boolean matchesPattern(String text, String pattern) {
        // 简单模式匹配，支持*通配符
        String regex = pattern.replace("*", ".*");
        return text.matches(regex);
    }

    /**
     * 判断是否为入口点文件
     */
    private boolean isEntryPoint(String fileName) {
        return fileName.matches(".*(main|app|index|startup|bootstrap)\\..*") ||
               fileName.equals("Main.java") || fileName.equals("App.java") ||
               fileName.equals("main.py") || fileName.equals("__main__.py") ||
               fileName.equals("index.js") || fileName.equals("app.js");
    }

    /**
     * 判断是否为配置文件
     */
    private boolean isConfigFile(String fileName) {
        return fileName.matches(".*\\.(yaml|yml|json|xml|properties|config|conf|ini)$") ||
               fileName.matches("(package|pom|build|gradle|makefile|dockerfile|docker-compose)\\..*");
    }

    /**
     * 判断是否为文档文件
     */
    private boolean isDocumentation(String fileName) {
        return fileName.matches(".*\\.(md|txt|rst|adoc)$") ||
               fileName.matches("(README|CHANGELOG|ARCHITECTURE|CONTRIBUTING).*");
    }

    /**
     * 判断是否为测试文件
     */
    private boolean isTestFile(String relativePath) {
        return relativePath.contains("/test/") || relativePath.contains("\\test\\") ||
               relativePath.matches(".*Test\\..*") || relativePath.matches(".*Spec\\..*");
    }

    /**
     * 判断是否为依赖目录
     */
    private boolean isDependencyDirectory(String relativePath) {
        return relativePath.startsWith("node_modules/") ||
               relativePath.startsWith("target/") ||
               relativePath.startsWith("build/") ||
               relativePath.startsWith("dist/") ||
               relativePath.startsWith("venv/") ||
               relativePath.startsWith(".git/") ||
               relativePath.startsWith(".svn/");
    }
}
