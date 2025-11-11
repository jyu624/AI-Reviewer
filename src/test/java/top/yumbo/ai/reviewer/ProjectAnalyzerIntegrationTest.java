package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectAnalyzerIntegrationTest {

    @Test
    void endToEnd() throws Exception {
        Path temp = Files.createTempDirectory("pa_integ");
        try {
            // create a mini project structure
            Path src = Files.createDirectories(temp.resolve("src/main/java/top/yumbo/ai/reviewer"));
            Files.writeString(temp.resolve("README.md"), "Readme content", StandardCharsets.UTF_8);
            Files.writeString(src.resolve("App.java"), "public class App { public static void main(String[] a){} }", StandardCharsets.UTF_8);

            Path out = temp.resolve("out");
            // run ProjectAnalyzer main
            ProjectAnalyzer.main(new String[]{temp.toString(), out.toString(), "500", "200", "3", "false", "1"});

            // assertions
            assertThat(Files.exists(out.resolve("project_structure.txt"))).isTrue();
            assertThat(Files.isDirectory(out.resolve("snippets"))).isTrue();
            assertThat(Files.isDirectory(out.resolve("batches"))).isTrue();
            assertThat(Files.exists(out.resolve("batch_index.txt"))).isTrue();
            assertThat(Files.exists(out.resolve("top_k_selected.txt"))).isTrue();

            String idx = Files.readString(out.resolve("batch_index.txt"), StandardCharsets.UTF_8);
            assertThat(idx).contains("batches:");

        } finally {
            Files.walk(temp).sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p);} catch (IOException e) { }
            });
        }
    }
}
