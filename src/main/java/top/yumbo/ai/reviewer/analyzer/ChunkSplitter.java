package top.yumbo.ai.reviewer.analyzer;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.entity.FileChunk;
import top.yumbo.ai.reviewer.util.TokenEstimator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码块分割器 - 将大文件分割成适合AI分析的小块
 */
@Slf4j
public class ChunkSplitter {

    private final TokenEstimator tokenEstimator;
    private static final int MAX_CHUNK_SIZE = 4000; // 最大token数
    private static final int OVERLAP_SIZE = 200; // 重叠token数

    // 代码块识别正则表达式
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static)?\\s*[\\w\\<\\>\\[\\]]+\\s+(\\w+)\\s*\\(");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "(?:function|def|fun)\\s+(\\w+)\\s*\\(");

    public ChunkSplitter() {
        this.tokenEstimator = new TokenEstimator();
    }

    /**
     * 分割多个文件
     * @param files 文件路径列表
     * @param maxChunks 最大块数限制
     * @return 文件块列表
     */
    public List<FileChunk> splitFiles(List<Path> files, int maxChunks) {
        List<FileChunk> allChunks = new ArrayList<>();

        for (Path file : files) {
            try {
                List<FileChunk> fileChunks = splitFile(file);
                allChunks.addAll(fileChunks);

                if (allChunks.size() >= maxChunks) {
                    log.warn("达到最大块数限制: {}", maxChunks);
                    break;
                }
            } catch (Exception e) {
                log.error("分割文件失败: {}", file, e);
            }
        }

        return allChunks.stream()
                .limit(maxChunks)
                .collect(ArrayList::new, (list, chunk) -> list.add(chunk), List::addAll);
    }

    /**
     * 分割单个文件
     * @param filePath 文件路径
     * @return 文件块列表
     */
    public List<FileChunk> splitFile(Path filePath) throws Exception {
        String content = java.nio.file.Files.readString(filePath);
        String fileName = filePath.getFileName().toString();

        // 估算总token数
        int totalTokens = tokenEstimator.estimateTokens(content);

        if (totalTokens <= MAX_CHUNK_SIZE) {
            // 文件不大，直接作为一个块
            return List.of(FileChunk.builder()
                    .filePath(filePath.toString())
                    .content(content)
                    .startLine(1)
                    .endLine(content.split("\n").length)
                    .chunkIndex(0)
                    .totalChunks(1)
                    .estimatedTokens(totalTokens)
                    .build());
        }

        // 文件较大，需要分割
        return splitLargeFile(filePath, content);
    }

    /**
     * 分割大文件
     */
    private List<FileChunk> splitLargeFile(Path filePath, String content) {
        List<FileChunk> chunks = new ArrayList<>();
        String[] lines = content.split("\n");
        int totalLines = lines.length;

        // 识别代码结构
        List<CodeBlock> codeBlocks = identifyCodeBlocks(content);

        if (codeBlocks.isEmpty()) {
            // 没有识别到结构化代码，按行分割
            return splitByLines(filePath, lines);
        }

        // 按代码块分割
        int currentLine = 0;
        int chunkIndex = 0;

        for (CodeBlock block : codeBlocks) {
            if (currentLine < block.startLine) {
                // 处理块之间的内容
                List<FileChunk> gapChunks = splitContent(filePath, lines, currentLine, block.startLine - 1, chunkIndex);
                chunks.addAll(gapChunks);
                chunkIndex += gapChunks.size();
            }

            // 处理代码块
            List<FileChunk> blockChunks = splitContent(filePath, lines, block.startLine, block.endLine, chunkIndex);
            for (FileChunk chunk : blockChunks) {
                chunk.setChunkType(block.type);
                chunk.setIdentifier(block.identifier);
            }
            chunks.addAll(blockChunks);
            chunkIndex += blockChunks.size();

            currentLine = block.endLine + 1;
        }

        // 处理剩余内容
        if (currentLine < totalLines) {
            List<FileChunk> remainingChunks = splitContent(filePath, lines, currentLine, totalLines - 1, chunkIndex);
            chunks.addAll(remainingChunks);
        }

        // 设置总数
        final int totalChunks = chunks.size();
        chunks.forEach(chunk -> chunk.setTotalChunks(totalChunks));

        return chunks;
    }

    /**
     * 按行分割内容
     */
    private List<FileChunk> splitByLines(Path filePath, String[] lines) {
        List<FileChunk> chunks = new ArrayList<>();
        int totalLines = lines.length;
        int chunkSize = Math.max(50, totalLines / 10); // 每块大约50-100行

        for (int i = 0; i < totalLines; i += chunkSize) {
            int endLine = Math.min(i + chunkSize - 1, totalLines - 1);
            String chunkContent = String.join("\n", java.util.Arrays.copyOfRange(lines, i, endLine + 1));

            chunks.add(FileChunk.builder()
                    .filePath(filePath.toString())
                    .content(chunkContent)
                    .startLine(i + 1)
                    .endLine(endLine + 1)
                    .chunkIndex(chunks.size())
                    .estimatedTokens(tokenEstimator.estimateTokens(chunkContent))
                    .build());
        }

        chunks.forEach(chunk -> chunk.setTotalChunks(chunks.size()));
        return chunks;
    }

    /**
     * 分割内容块
     */
    private List<FileChunk> splitContent(Path filePath, String[] lines, int startLine, int endLine, int startIndex) {
        List<FileChunk> chunks = new ArrayList<>();
        String content = String.join("\n", java.util.Arrays.copyOfRange(lines, startLine, endLine + 1));
        int tokens = tokenEstimator.estimateTokens(content);

        if (tokens <= MAX_CHUNK_SIZE) {
            chunks.add(FileChunk.builder()
                    .filePath(filePath.toString())
                    .content(content)
                    .startLine(startLine + 1)
                    .endLine(endLine + 1)
                    .chunkIndex(startIndex)
                    .estimatedTokens(tokens)
                    .build());
        } else {
            // 需要进一步分割
            int midLine = startLine + (endLine - startLine) / 2;
            List<FileChunk> firstHalf = splitContent(filePath, lines, startLine, midLine, startIndex);
            List<FileChunk> secondHalf = splitContent(filePath, lines, midLine + 1, endLine, startIndex + firstHalf.size());
            chunks.addAll(firstHalf);
            chunks.addAll(secondHalf);
        }

        return chunks;
    }

    /**
     * 识别代码块
     */
    private List<CodeBlock> identifyCodeBlocks(String content) {
        List<CodeBlock> blocks = new ArrayList<>();
        String[] lines = content.split("\n");

        // 识别类
        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        while (classMatcher.find()) {
            int lineNumber = getLineNumber(content, classMatcher.start());
            blocks.add(new CodeBlock("class", classMatcher.group(1), lineNumber, findBlockEnd(lines, lineNumber)));
        }

        // 识别方法/函数
        Pattern methodPattern = getMethodPattern(content);
        if (methodPattern != null) {
            Matcher methodMatcher = methodPattern.matcher(content);
            while (methodMatcher.find()) {
                int lineNumber = getLineNumber(content, methodMatcher.start());
                blocks.add(new CodeBlock("method", methodMatcher.group(1), lineNumber, findBlockEnd(lines, lineNumber)));
            }
        }

        // 按行号排序
        blocks.sort((a, b) -> Integer.compare(a.startLine, b.startLine));

        return blocks;
    }

    /**
     * 获取方法模式
     */
    private Pattern getMethodPattern(String content) {
        if (content.contains("public static void main") || content.contains("function ") || content.contains("def ")) {
            return METHOD_PATTERN; // Java/C# style
        } else if (content.contains("def ")) {
            return FUNCTION_PATTERN; // Python style
        }
        return null;
    }

    /**
     * 获取行号
     */
    private int getLineNumber(String content, int charIndex) {
        return content.substring(0, charIndex).split("\n").length;
    }

    /**
     * 查找块结束行
     */
    private int findBlockEnd(String[] lines, int startLine) {
        int braceCount = 0;
        boolean inBlock = false;

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.contains("{")) {
                braceCount++;
                inBlock = true;
            }
            if (line.contains("}")) {
                braceCount--;
            }

            if (inBlock && braceCount == 0) {
                return i;
            }
        }

        return Math.min(startLine + 50, lines.length - 1); // 默认50行或文件结尾
    }

    /**
     * 代码块内部类
     */
    private static class CodeBlock {
        String type;
        String identifier;
        int startLine;
        int endLine;

        CodeBlock(String type, String identifier, int startLine, int endLine) {
            this.type = type;
            this.identifier = identifier;
            this.startLine = startLine;
            this.endLine = endLine;
        }
    }
}
