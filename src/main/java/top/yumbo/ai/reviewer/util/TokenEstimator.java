package top.yumbo.ai.reviewer.util;

/**
 * Token估算器 - 估算文本的token数量
 * 基于简单的启发式规则，实际token数可能与模型的具体实现略有差异
 */
public class TokenEstimator {

    // 平均每个单词的token数（英文）
    private static final double AVG_TOKENS_PER_WORD = 1.3;

    // 中文字符的token估算比例
    private static final double CHINESE_TOKEN_RATIO = 0.6;

    /**
     * 估算文本的token数量
     * @param text 输入文本
     * @return 预估token数
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // 统计中文字符数
        int chineseChars = countChineseCharacters(text);

        // 统计英文单词数
        int englishWords = countEnglishWords(text);

        // 估算token数
        double chineseTokens = chineseChars * CHINESE_TOKEN_RATIO;
        double englishTokens = englishWords * AVG_TOKENS_PER_WORD;

        // 加上标点符号和特殊字符的token
        int punctuationTokens = countPunctuation(text);

        return (int) Math.ceil(chineseTokens + englishTokens + punctuationTokens);
    }

    /**
     * 统计中文字符数
     */
    private int countChineseCharacters(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (isChineseCharacter(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断是否为中文字符
     */
    private boolean isChineseCharacter(char c) {
        return (c >= 0x4E00 && c <= 0x9FFF) || // 基本汉字
               (c >= 0x3400 && c <= 0x4DBF) || // 扩展A
               (c >= 0x20000 && c <= 0x2A6DF) || // 扩��B
               (c >= 0x2A700 && c <= 0x2B73F) || // 扩展C
               (c >= 0x2B740 && c <= 0x2B81F) || // 扩展D
               (c >= 0x2B820 && c <= 0x2CEAF); // 扩展E
    }

    /**
     * 统计英文单词数
     */
    private int countEnglishWords(String text) {
        // 移除中文字符和标点符号，保留英文和数字
        String englishText = text.replaceAll("[\\u4e00-\\u9fa5\\p{Punct}\\s]+", " ");
        String[] words = englishText.trim().split("\\s+");
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty() && word.matches("[a-zA-Z0-9]+")) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计标点符号数
     */
    private int countPunctuation(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (isPunctuation(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断是否为标点符号
     */
    private boolean isPunctuation(char c) {
        return c == '.' || c == ',' || c == '!' || c == '?' || c == ';' || c == ':' ||
               c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
               c == '<' || c == '>' || c == '"' || c == '\'' || c == '-' || c == '_' ||
               c == '+' || c == '=' || c == '*' || c == '/' || c == '\\' || c == '|' ||
               c == '&' || c == '^' || c == '%' || c == '$' || c == '#' || c == '@' ||
               c == '~' || c == '`';
    }

    /**
     * 估算代码的token数（更精确的估算）
     * @param code 代码文本
     * @return 预估token数
     */
    public int estimateCodeTokens(String code) {
        if (code == null || code.isEmpty()) {
            return 0;
        }

        // 代码通常有更多的特殊字符和关键字
        int baseTokens = estimateTokens(code);

        // 为代码结构添加额外token
        int lines = code.split("\n").length;
        int indentationTokens = countIndentation(code);

        return baseTokens + (lines / 2) + (indentationTokens / 4);
    }

    /**
     * 统计缩进字符数
     */
    private int countIndentation(String code) {
        int count = 0;
        for (char c : code.toCharArray()) {
            if (c == ' ' || c == '\t') {
                count++;
            } else if (c == '\n') {
                // 重置行开始
            } else {
                break;
            }
        }
        return count;
    }
}
