package top.yumbo.ai.reviewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FilePrioritizer {
    /**
     * Compute a heuristic score for a file. Higher is more important.
     * Factors:
     * - recency (lastModified)
     * - size (bytes) (log-scaled)
     * - filename keywords (Controller, Service, Dao, Repo, Main, App, pom.xml, README)
     */
    public static double score(Path file) {
        try {
            if (!Files.isRegularFile(file)) return 0.0;
            long now = System.currentTimeMillis();
            long last = Files.getLastModifiedTime(file).toMillis();
            double normTime = (double) last / Math.max(1L, now); // ~ 0..1
            long size = Files.size(file);
            double sizeScore = Math.log10((double) size + 1.0);
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            int kw = 0;
            if (name.contains("controller")) kw += 3;
            if (name.contains("service")) kw += 3;
            if (name.contains("dao") || name.contains("repo") || name.contains("repository")) kw += 2;
            if (name.contains("main") || name.contains("app")) kw += 3;
            if (name.equalsIgnoreCase("pom.xml") || name.equalsIgnoreCase("build.gradle") || name.equalsIgnoreCase("package.json")) kw += 5;
            if (name.startsWith("readme")) kw += 5;

            // combine with weights
            double score = 0.6 * normTime + 0.3 * (sizeScore / 6.0) + 0.1 * (kw);
            return score;
        } catch (IOException e) {
            return 0.0;
        }
    }
}

