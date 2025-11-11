package top.yumbo.ai.reviewer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 文件块实体 - 用于分块处理大文件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileChunk {

    private String filePath;
    private String content;
    private int startLine;
    private int endLine;
    private int chunkIndex;
    private int totalChunks;
    private String chunkType; // function, class, method, etc.
    private String identifier; // 函数名、类名等
    private int estimatedTokens; // 预估token数量

    /**
     * 判断是否为代码块
     */
    public boolean isCodeChunk() {
        return "class".equals(chunkType) ||
               "function".equals(chunkType) ||
               "method".equals(chunkType) ||
               "interface".equals(chunkType);
    }

    /**
     * 获取块大小（字符数）
     */
    public int getSize() {
        return content != null ? content.length() : 0;
    }

    /**
     * 获取块的行数
     */
    public int getLineCount() {
        return endLine - startLine + 1;
    }

    /**
     * 判断是否为最后一个块
     */
    public boolean isLastChunk() {
        return chunkIndex == totalChunks - 1;
    }

    /**
     * 文件块构建器
     */
    public static class FileChunkBuilder {
        public FileChunkBuilder content(String content) {
            this.content = content;
            return this;
        }

        public FileChunkBuilder lines(int startLine, int endLine) {
            this.startLine = startLine;
            this.endLine = endLine;
            return this;
        }
    }
}
