package top.yumbo.ai.core.filter;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
/**
 * File filter for applying include/exclude patterns
 */
@Slf4j
public class FileFilter {
    /**
     * Filter files based on patterns
     */
    public List<Path> filter(List<Path> files, List<String> includePatterns, List<String> excludePatterns) {
        log.info("Filtering {} files with {} include patterns and {} exclude patterns",
                files.size(), 
                includePatterns == null ? 0 : includePatterns.size(),
                excludePatterns == null ? 0 : excludePatterns.size());
        List<PathMatcher> includeMatchers = createMatchers(includePatterns);
        List<PathMatcher> excludeMatchers = createMatchers(excludePatterns);
        List<Path> filtered = files.stream()
                .filter(path -> matchesInclude(path, includeMatchers))
                .filter(path -> !matchesExclude(path, excludeMatchers))
                .collect(Collectors.toList());
        log.info("Filtered to {} files", filtered.size());
        return filtered;
    }
    private List<PathMatcher> createMatchers(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return List.of();
        }
        return patterns.stream()
                .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
                .collect(Collectors.toList());
    }
    private boolean matchesInclude(Path path, List<PathMatcher> matchers) {
        if (matchers.isEmpty()) {
            return true; // No include patterns means include all
        }
        return matchers.stream().anyMatch(matcher -> matcher.matches(path));
    }
    private boolean matchesExclude(Path path, List<PathMatcher> matchers) {
        if (matchers.isEmpty()) {
            return false; // No exclude patterns means exclude none
        }
        return matchers.stream().anyMatch(matcher -> matcher.matches(path));
    }
}
