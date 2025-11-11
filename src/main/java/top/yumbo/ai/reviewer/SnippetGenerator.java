package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnippetGenerator {
    private final int maxLinesPerFile;

    public SnippetGenerator(int maxLinesPerFile) {
        this.maxLinesPerFile = maxLinesPerFile;
    }

    public String snippetFor(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        String header = "// File: " + file + " (chars=" + content.length() + ")\n";
        String body;
        if (file.getFileName().toString().toLowerCase().endsWith(".java")) {
            // use language-aware splitting: treat maxLinesPerFile as maxMethods
            int maxMethods = Math.max(1, this.maxLinesPerFile / 40); // heuristic: ~40 lines per method
            List<String> methods = LanguageSplitter.splitJavaMethods(content, maxMethods);
            if (!methods.isEmpty()) {
                // attempt to include class declaration if present
                Pattern classPat = Pattern.compile("(public\\s+class|class)\\s+[\\w_]+[^\\{]*\\{", Pattern.MULTILINE);
                Matcher cm = classPat.matcher(content);
                String classDecl = null;
                if (cm.find()) {
                    classDecl = cm.group();
                }
                if (classDecl == null) {
                    // synthesize a minimal class declaration from filename
                    String fname = file.getFileName().toString();
                    if (fname.toLowerCase().endsWith(".java")) {
                        String base = fname.substring(0, fname.length() - 5);
                        // try to find an actual class declaration "class <base>" in content
                        int pos = content.indexOf("class " + base);
                        if (pos >= 0) {
                            int brace = content.indexOf('{', pos);
                            if (brace > pos) {
                                classDecl = content.substring(pos, brace + 1);
                            } else {
                                classDecl = "public class " + base + " {";
                            }
                        } else {
                            classDecl = "public class " + base + " {";
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                if (classDecl != null) {
                    sb.append(classDecl).append("\n");
                }
                sb.append(String.join("\n\n", methods));
                if (methods.size() < maxMethods) {
                    // append a note if few methods
                    sb.append("\n// (methods extracted: " + methods.size() + ")");
                }
                return header + sb.toString();
            }
        }

        // fallback: line-based trimming
        List<String> allLines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<String> lines = allLines;
        boolean truncated = false;
        if (allLines.size() > maxLinesPerFile) {
            lines = allLines.subList(0, maxLinesPerFile);
            truncated = true;
        }
        body = String.join("\n", lines);
        if (truncated) {
            body += "\n// ...omitted " + (allLines.size() - maxLinesPerFile) + " lines...";
        }
        return header + body;
    }
}
