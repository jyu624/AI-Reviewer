package top.yumbo.ai.reviewer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenEstimatorTest {

    @Test
    void estimatesForEmpty() {
        assertThat(TokenEstimator.estimateTokens("")).isEqualTo(0);
        assertThat(TokenEstimator.estimateTokens(null)).isEqualTo(0);
    }

    @Test
    void estimatesForShortText() {
        String s = "public class App { }";
        int t = TokenEstimator.estimateTokens(s);
        assertThat(t).isGreaterThanOrEqualTo(1);
    }

    @Test
    void estimatesForLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("word ");
        int t = TokenEstimator.estimateTokens(sb.toString());
        // words=1000 => byWords ~ 1300 tokens estimate, byChars ~ ~500
        assertThat(t).isGreaterThanOrEqualTo(500);
        assertThat(t).isLessThanOrEqualTo(2000);
    }
}

