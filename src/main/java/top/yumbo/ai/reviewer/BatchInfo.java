package top.yumbo.ai.reviewer;

import java.nio.file.Path;
import java.util.List;

public class BatchInfo {
    private final Path path;
    private final List<String> snippets;
    private final long charCount;

    public BatchInfo(Path path, List<String> snippets, long charCount) {
        this.path = path;
        this.snippets = snippets;
        this.charCount = charCount;
    }

    public Path getPath() {
        return path;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    public long getCharCount() {
        return charCount;
    }
}

