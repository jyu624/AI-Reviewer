package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectAnalyzer {
    private static void printUsage() {
        System.out.println("ProjectAnalyzer - preprocess a codebase for LLM analysis\n");
        System.out.println("Usage: ProjectAnalyzer <rootPath> <outDir> <maxCharsPerBatch> <snippetMaxLines> <treeDepth> <includeTests> <topK> <selectionFile>");
        System.out.println("All arguments are optional and have defaults:\n");
        System.out.println("  rootPath           (default: .)");
        System.out.println("  outDir             (default: llm_output)");
        System.out.println("  maxCharsPerBatch   (default: 100000)");
        System.out.println("  snippetMaxLines    (default: 200)");
        System.out.println("  treeDepth          (default: 4)");
        System.out.println("  includeTests       (default: false)");
        System.out.println("  topK               (default: -1, no topK)");
        System.out.println("  selectionFile      (optional path to file list, one per line)");
        System.out.println("Examples:");
        System.out.println("  java -cp target/ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer . llm_output 100000 200 4 false 20");
        System.out.println("  java -cp target/ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer /path/to/repo out 50000 120 3 false -1 selection.txt");
    }

    public static void main(String[] args) throws Exception {
        // quick support for -h/--help
        for (String a : args) {
            if ("-h".equals(a) || "--help".equals(a)) {
                printUsage();
                return;
            }
        }

        Path root = Paths.get(args.length > 0 ? args[0] : ".");
        Path outDir = Paths.get(args.length > 1 ? args[1] : "llm_output");
        int maxCharsPerBatch = args.length > 2 ? Integer.parseInt(args[2]) : 100_000;
        int snippetMaxLines = args.length > 3 ? Integer.parseInt(args[3]) : 200;
        int treeDepth = args.length > 4 ? Integer.parseInt(args[4]) : 4;
        boolean includeTests = args.length > 5 ? Boolean.parseBoolean(args[5]) : false;
        int topK = args.length > 6 ? Integer.parseInt(args[6]) : -1;
        String selectionFilePath = args.length > 7 ? args[7] : null;

        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }

        FileSelector selector = FileSelector.defaultSelector();
        List<Path> files = selector.selectFiles(root);

        // If a selection file is provided, override the files list
        if (selectionFilePath != null && !selectionFilePath.isBlank()) {
            Path selFile = Paths.get(selectionFilePath);
            if (!selFile.isAbsolute()) selFile = root.resolve(selFile);
            if (Files.exists(selFile)) {
                List<String> lines = Files.readAllLines(selFile, StandardCharsets.UTF_8);
                List<Path> picked = new ArrayList<>();
                for (String line : lines) {
                    if (line == null) continue;
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Path p = Paths.get(line);
                    if (!p.isAbsolute()) p = root.resolve(p);
                    if (Files.exists(p) && Files.isRegularFile(p)) picked.add(p);
                }
                if (!picked.isEmpty()) {
                    files = picked;
                    System.out.println("Using selection file: " + selFile + " -> " + files.size() + " files");
                }
            }
        }

        if (!includeTests) {
            files = files.stream()
                    .filter(p -> {
                        String s = p.toString().replace('\\', '/').toLowerCase();
                        return !(s.contains("/test/") || s.contains("/tests/") || s.contains("/src/test/"));
                    })
                    .collect(Collectors.toList());
        }

        // prioritize files by heuristic score
        files.sort((a, b) -> Double.compare(FilePrioritizer.score(b), FilePrioritizer.score(a)));

        // write selected files with score for audit
        Path selectedList = outDir.resolve("selected_files.txt");
        StringBuilder selSb = new StringBuilder();
        for (Path p : files) {
            selSb.append(String.format("%f\t%s\n", FilePrioritizer.score(p), p.toString()));
        }
        Files.writeString(selectedList, selSb.toString(), StandardCharsets.UTF_8);
        System.out.println("Wrote selected_files.txt");

        // apply topK filtering if requested
        if (topK > 0 && files.size() > topK) {
            List<Path> topFiles = files.subList(0, Math.min(topK, files.size()));
            Path topSelected = outDir.resolve("top_k_selected.txt");
            StringBuilder topSb = new StringBuilder();
            for (Path p : topFiles) topSb.append(p.toString()).append("\n");
            Files.writeString(topSelected, topSb.toString(), StandardCharsets.UTF_8);
            files = topFiles;
            System.out.println("Applied topK filter: " + topK + " files");
        }

        // write project structure with configurable depth
        String tree = ProjectStructure.tree(root, treeDepth);
        Files.writeString(outDir.resolve("project_structure.txt"), tree, StandardCharsets.UTF_8);
        System.out.println("Wrote project_structure.txt");

        // write snippets into outDir/snippets with configurable max lines
        Path snippetsDir = outDir.resolve("snippets");
        if (!Files.exists(snippetsDir)) {
            Files.createDirectories(snippetsDir);
        }

        SnippetGenerator gen = new SnippetGenerator(snippetMaxLines);
        int count = 0;
        for (Path f : files) {
            try {
                String snip = gen.snippetFor(f);
                // name safe: replace colon, slash, backslash, whitespace with underscore
                String safeName = f.toString().replaceAll("[:\\\\/\\s]+", "_");
                Path out = snippetsDir.resolve((++count) + "__" + safeName + ".txt");
                Files.writeString(out, snip, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Failed to read file: " + f + " -> " + e.getMessage());
            }
        }
        System.out.println("Generated " + count + " snippets in " + snippetsDir.toAbsolutePath());

        // generate batches from snippets and collect info
        Path batchesDir = outDir.resolve("batches");
        BatchGenerator batchGen = new BatchGenerator(maxCharsPerBatch, BatchGenerator.defaultPromptHeader());
        // map original file basenames to snippet filenames in snippetsDir
        List<Path> snippetFiles = Files.list(snippetsDir).filter(p -> !Files.isDirectory(p)).sorted().collect(Collectors.toList());
        Map<String, String> basenameToSnippet = new HashMap<>();
        for (Path sp : snippetFiles) {
            String name = sp.getFileName().toString();
            int idx = name.lastIndexOf("__");
            if (idx >= 0) {
                String rest = name.substring(idx + 2);
                if (rest.endsWith(".txt")) rest = rest.substring(0, rest.length() - 4);
                for (Path origPath : files) {
                    String b = origPath.getFileName().toString();
                    if (rest.endsWith(b)) {
                        basenameToSnippet.put(b, name);
                        break;
                    }
                }
            }
        }

        List<String> finalPriorityOrder = new ArrayList<>();
        for (Path p : files) {
            String b = p.getFileName().toString();
            if (basenameToSnippet.containsKey(b)) finalPriorityOrder.add(basenameToSnippet.get(b));
        }

        List<BatchInfo> batches;
        if (!finalPriorityOrder.isEmpty()) {
            batches = batchGen.generateBatchesWithPriority(snippetsDir, batchesDir, finalPriorityOrder);
        } else {
            batches = batchGen.generateBatchesWithInfo(snippetsDir, batchesDir);
        }

        // write detailed batch index with snippet lists and token estimate
        Path index = outDir.resolve("batch_index.txt");
        StringBuilder sb = new StringBuilder();
        sb.append("batches:\n");
        for (BatchInfo b : batches) {
            sb.append(b.getPath().getFileName()).append("\tchars=").append(b.getCharCount());
            long estTokens = TokenEstimator.estimateTokens(Files.readString(b.getPath(), StandardCharsets.UTF_8));
            sb.append("\ttokens~").append(estTokens).append("\n");
            sb.append("  snippets:\n");
            for (String s : b.getSnippets()) {
                sb.append("    - ").append(s).append("\n");
            }
        }
        Files.writeString(index, sb.toString(), StandardCharsets.UTF_8);
        System.out.println("Generated " + batches.size() + " batches in " + batchesDir.toAbsolutePath());
    }
}
