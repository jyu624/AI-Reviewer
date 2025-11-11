package top.yumbo.ai.reviewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenEstimator {
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");

    /**
     * Improved token estimate: combines word-based counting with sub-token splitting.
     * Heuristic:
     * - split into word-like tokens using WORD_PATTERN
     * - each long word contributes ceil(len/4) tokens (approx subword)
     * - punctuation and symbols each count as one token roughly
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chars = text.length();
        // base by characters
        int byChars = Math.max(1, chars / 4);

        // word/subword computation
        Matcher m = WORD_PATTERN.matcher(text);
        int tokenCount = 0;
        int words = 0;
        while (m.find()) {
            words++;
            String w = m.group();
            // approximate subword: 1 token per ~4 chars
            tokenCount += Math.max(1, (int) Math.ceil((double) w.length() / 4.0));
        }
        // punctuation/symbols: count remaining non-word characters roughly
        int nonWordChars = chars - text.replaceAll("\\W", "").length();
        int punctTokens = Math.max(0, nonWordChars / 2);

        int byWords = Math.max(1, tokenCount + punctTokens);
        // final estimate is max of char-based and word-based heuristics
        return Math.max(byChars, byWords);
    }
}
