package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectAnalyzerTest {

    @Test
    void testSelectorAndStructureAndSnippet() throws IOException {
        Path temp = Files.createTempDirectory("pa_test");
        try {
            // create dirs
            Path src = Files.createDirectories(temp.resolve("src"));
            Path main = Files.createDirectories(src.resolve("main"));
            Path node = Files.createDirectories(temp.resolve("node_modules"));

            // create files
            Path readme = Files.writeString(temp.resolve("README.md"), "This is a readme", StandardCharsets.UTF_8);
            Path app = Files.writeString(main.resolve("App.java"), "public class App { public static void main(String[] a){} }", StandardCharsets.UTF_8);
            Path ignored = Files.writeString(node.resolve("should_ignore.js"), "console.log('hi')", StandardCharsets.UTF_8);

            FileSelector sel = FileSelector.defaultSelector();
            List<Path> files = sel.selectFiles(temp);
            assertThat(files).contains(readme, app);
            assertThat(files).doesNotContain(ignored);

            String tree = ProjectStructure.tree(temp, 3);
            assertThat(tree).contains("src");
            assertThat(tree).contains("README.md");

            SnippetGenerator gen = new SnippetGenerator(5);
            String snip = gen.snippetFor(app);
            assertThat(snip).contains("File:");
            assertThat(snip).contains("public class App");

        } finally {
            // cleanup
            Files.walk(temp)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }
    }
}
