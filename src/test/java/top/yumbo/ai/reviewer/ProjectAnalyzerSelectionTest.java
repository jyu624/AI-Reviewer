package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectAnalyzerSelectionTest {

    @Test
    void usesSelectionFile() throws Exception {
        Path temp = Files.createTempDirectory("pa_sel");
        try {
            Path src = Files.createDirectories(temp.resolve("src"));
            Path fileA = Files.writeString(src.resolve("A.java"), "public class A{ void a(){} }", StandardCharsets.UTF_8);
            Path fileB = Files.writeString(src.resolve("B.java"), "public class B{ void b(){} }", StandardCharsets.UTF_8);

            Path sel = Files.writeString(temp.resolve("selection.txt"), src.resolve("B.java").toString(), StandardCharsets.UTF_8);

            Path out = temp.resolve("out");
            ProjectAnalyzer.main(new String[]{temp.toString(), out.toString(), "500", "200", "3", "false", "-1", sel.toString()});

            // only B.java should be processed into snippets
            assertThat(Files.exists(out.resolve("snippets"))).isTrue();
            long count = Files.list(out.resolve("snippets")).count();
            assertThat(count).isEqualTo(1);

        } finally {
            Files.walk(temp).sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p);} catch (Exception e) { }
            });
        }
    }
}

