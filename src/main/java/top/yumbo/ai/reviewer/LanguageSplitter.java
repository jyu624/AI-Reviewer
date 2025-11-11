package top.yumbo.ai.reviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageSplitter {
    private static final Pattern JAVA_METHOD_PATTERN = Pattern.compile(
            "((?:public|protected|private|static|\\s)\\s+)*([\\w<>\\[\\]]+)\\s+([\\w]+)\\s*\\([^)]*\\)\\s*\\{",
            Pattern.MULTILINE);

    /**
     * Attempt to extract top-level method bodies from Java source.
     * This is a heuristic: finds method signatures and then attempts to capture the block by balance of braces.
     */
    public static List<String> splitJavaMethods(String source, int maxMethods) {
        List<String> methods = new ArrayList<>();
        Matcher m = JAVA_METHOD_PATTERN.matcher(source);
        while (m.find() && methods.size() < maxMethods) {
            int start = m.start();
            // find matching braces from the opening brace position
            int bracePos = source.indexOf('{', m.end() - 1);
            if (bracePos < 0) continue;
            int depth = 1;
            int i = bracePos + 1;
            for (; i < source.length() && depth > 0; i++) {
                char c = source.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') depth--;
            }
            int end = (depth == 0) ? i : Math.min(source.length(), bracePos + 1000);
            String methodText = source.substring(start, end);
            methods.add(methodText);
        }
        return methods;
    }
}
