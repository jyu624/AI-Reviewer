package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchGeneratorTest {

    @Test
    void testGenerateBatches() throws IOException {
        Path temp = Files.createTempDirectory("bg_test");
        Path snippets = Files.createDirectories(temp.resolve("snippets"));
        Path out = Files.createDirectories(temp.resolve("out"));
        try {
            // create a few snippet files
            Files.writeString(snippets.resolve("1.txt"), "// File A\nline1\nline2\n", StandardCharsets.UTF_8);
            Files.writeString(snippets.resolve("2.txt"), "// File B\nline1\nline2\nline3\n", StandardCharsets.UTF_8);
            Files.writeString(snippets.resolve("3.txt"), "// File C\n" + "x".repeat(500), StandardCharsets.UTF_8);

            BatchGenerator gen = new BatchGenerator(300, BatchGenerator.defaultPromptHeader());
            List<Path> batches = gen.generateBatches(snippets, out);

            assertThat(batches).isNotEmpty();
            // ensure prompt header present in first batch
            String first = Files.readString(batches.get(0), StandardCharsets.UTF_8);
            assertThat(first).contains("请基于下面的代码片段和项目结构进行分析");

            // ensure large snippet forced a split (so we should have more than 1 batch)
            assertThat(batches.size()).isGreaterThanOrEqualTo(2);

        } finally {
            Files.walk(temp).sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p);} catch (IOException e) { }
            });
        }
    }
}

