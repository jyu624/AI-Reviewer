package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class BatchGenerator {
    private final int maxCharsPerBatch;
    private final String promptHeader;

    public BatchGenerator(int maxCharsPerBatch, String promptHeader) {
        this.maxCharsPerBatch = maxCharsPerBatch;
        this.promptHeader = promptHeader == null ? "" : promptHeader;
    }

    public List<BatchInfo> generateBatchesWithInfo(Path snippetsDir, Path outDir) throws IOException {
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }
        List<Path> snippetFiles = Files.list(snippetsDir)
                .filter(p -> !Files.isDirectory(p))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        List<BatchInfo> created = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        List<String> currentSnippets = new ArrayList<>();
        int batchIdx = 0;

        // start with prompt header
        current.append(promptHeader).append("\n\n");

        for (Path s : snippetFiles) {
            String content = Files.readString(s, StandardCharsets.UTF_8);
            if (current.length() + content.length() > maxCharsPerBatch && current.length() > promptHeader.length() + 2) {
                // flush
                batchIdx++;
                Path out = outDir.resolve(String.format("batch_%03d.txt", batchIdx));
                Files.writeString(out, current.toString(), StandardCharsets.UTF_8);
                long chars = Files.readString(out, StandardCharsets.UTF_8).length();
                created.add(new BatchInfo(out, new ArrayList<>(currentSnippets), chars));
                // reset
                current = new StringBuilder();
                current.append(promptHeader).append("\n\n");
                currentSnippets.clear();
            }
            // append snippet separator and content
            current.append("// --- snippet: ").append(s.getFileName()).append(" ---\n");
            current.append(content).append("\n\n");
            currentSnippets.add(s.getFileName().toString());
        }

        // flush remainder if any snippets were added
        if (currentSnippets.size() > 0) {
            batchIdx++;
            Path out = outDir.resolve(String.format("batch_%03d.txt", batchIdx));
            Files.writeString(out, current.toString(), StandardCharsets.UTF_8);
            long chars = Files.readString(out, StandardCharsets.UTF_8).length();
            created.add(new BatchInfo(out, new ArrayList<>(currentSnippets), chars));
        }

        return created;
    }

    public List<Path> generateBatches(Path snippetsDir, Path outDir) throws IOException {
        List<BatchInfo> infos = generateBatchesWithInfo(snippetsDir, outDir);
        List<Path> paths = new ArrayList<>();
        for (BatchInfo b : infos) paths.add(b.getPath());
        return paths;
    }

    /**
     * Generate batches attempting to distribute high-priority snippets across batches.
     * priorityOrder: list of snippet file names (not full paths) in descending priority.
     */
    public List<BatchInfo> generateBatchesWithPriority(Path snippetsDir, Path outDir, List<String> priorityOrder) throws IOException {
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        // load all snippets into memory (small files assumed)
        List<Path> snippetFiles = Files.list(snippetsDir)
                .filter(p -> !Files.isDirectory(p))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Map<String, Path> nameToPath = new HashMap<>();
        for (Path p : snippetFiles) nameToPath.put(p.getFileName().toString(), p);

        // prioritize: start with priorityOrder, then remaining files
        List<String> orderedNames = new ArrayList<>();
        for (String n : priorityOrder) if (nameToPath.containsKey(n)) orderedNames.add(n);
        for (Path p : snippetFiles) if (!orderedNames.contains(p.getFileName().toString())) orderedNames.add(p.getFileName().toString());

        // simple round-robin assignment to batches: create bins and fill sequentially until size limits
        List<StringBuilder> batchContents = new ArrayList<>();
        List<List<String>> batchSnips = new ArrayList<>();

        // create at least one bin
        batchContents.add(new StringBuilder(promptHeader).append("\n\n"));
        batchSnips.add(new ArrayList<>());

        for (String name : orderedNames) {
            Path p = nameToPath.get(name);
            if (p == null) continue;
            String content = Files.readString(p, StandardCharsets.UTF_8);
            boolean placed = false;
            // try to place into existing batches starting with those with fewest priority snippets
            // we try to spread priority items first
            for (int i = 0; i < batchContents.size(); i++) {
                StringBuilder sb = batchContents.get(i);
                if (sb.length() + content.length() <= maxCharsPerBatch) {
                    sb.append("// --- snippet: ").append(name).append(" ---\n");
                    sb.append(content).append("\n\n");
                    batchSnips.get(i).add(name);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                // create new batch
                StringBuilder sb = new StringBuilder(promptHeader).append("\n\n");
                sb.append("// --- snippet: ").append(name).append(" ---\n");
                sb.append(content).append("\n\n");
                batchContents.add(sb);
                List<String> l = new ArrayList<>();
                l.add(name);
                batchSnips.add(l);
            }
        }

        // flush batches to files
        List<BatchInfo> out = new ArrayList<>();
        for (int i = 0; i < batchContents.size(); i++) {
            int idx = i + 1;
            Path outPath = outDir.resolve(String.format("batch_%03d.txt", idx));
            Files.writeString(outPath, batchContents.get(i).toString(), StandardCharsets.UTF_8);
            long chars = Files.readString(outPath, StandardCharsets.UTF_8).length();
            out.add(new BatchInfo(outPath, batchSnips.get(i), chars));
        }
        return out;
    }

    public static String defaultPromptHeader() {
        return "请基于下面的代码片段和项目结构进行分析：\n- 请先输出项目的核心功能（1-2句话）\n- 然后列出潜在的架构或性能问题（按高/中/低风险分类）\n- 针对每个问题给出可执行的修复建议（要点）\n";
    }
}
