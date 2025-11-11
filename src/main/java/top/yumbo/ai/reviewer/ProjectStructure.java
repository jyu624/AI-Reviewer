package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ProjectStructure {

    public static String tree(Path root, int maxDepth) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(root)) {
            return "";
        }
        try (Stream<Path> stream = Files.walk(root, maxDepth)) {
            stream.filter(p -> !p.equals(root))
                    .forEach(p -> {
                        int depth = root.relativize(p).getNameCount();
                        String indent = "    ".repeat(Math.max(0, depth - 1));
                        if (Files.isDirectory(p)) {
                            sb.append(indent).append("├── ").append(p.getFileName()).append("/\n");
                        } else {
                            sb.append(indent).append("├── ").append(p.getFileName()).append("\n");
                        }
                    });
        }
        return sb.toString();
    }
}
