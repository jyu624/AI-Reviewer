package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageSplitterTest {

    @Test
    void splitsJavaMethods() {
        String src = "public class X {\n public void a() { System.out.println(1); }\n private int b(int x) { if (x>0) return x; return 0; }\n}\n";
        List<String> methods = LanguageSplitter.splitJavaMethods(src, 10);
        assertThat(methods).isNotEmpty();
        assertThat(methods.size()).isGreaterThanOrEqualTo(2);
        assertThat(methods.get(0)).contains("void a");
    }
}

