package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class FileSelector {
    private final Set<String> includeExtensions;
    private final Set<String> excludeDirNames;
    private final Set<String> mustKeepFileNames;

    public FileSelector(Set<String> includeExtensions, Set<String> excludeDirNames, Set<String> mustKeepFileNames) {
        this.includeExtensions = includeExtensions;
        this.excludeDirNames = excludeDirNames;
        this.mustKeepFileNames = mustKeepFileNames;
    }

    public List<Path> selectFiles(Path root) throws IOException {
        List<Path> results = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path name = dir.getFileName();
                if (name != null && excludeDirNames.contains(name.toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                // always keep specified filenames
                if (mustKeepFileNames.contains(fileName)) {
                    results.add(file);
                    return FileVisitResult.CONTINUE;
                }

                // exclude binary-like files by extension
                int lastDot = fileName.lastIndexOf('.');
                String ext = lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
                if (ext.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                if (includeExtensions.contains(ext)) {
                    results.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // sort results for determinism
        return results.stream().sorted().collect(Collectors.toList());
    }

    public static FileSelector defaultSelector() {
        Set<String> exts = new HashSet<>(Arrays.asList(
                "java", "xml", "yml", "yaml", "json", "md", "py", "js", "ts", "properties", "gradle", "pom"));
        Set<String> excludeDirs = new HashSet<>(Arrays.asList(
                "node_modules", "venv", "build", "target", ".git", ".idea", "dist"));
        Set<String> mustKeep = new HashSet<>(Arrays.asList("README.md", "README.MD", "pom.xml", "build.gradle", "package.json", "App.java", "Main.java"));
        return new FileSelector(exts, excludeDirs, mustKeep);
    }
}

