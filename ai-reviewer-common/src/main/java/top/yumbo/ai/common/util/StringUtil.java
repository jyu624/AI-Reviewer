package top.yumbo.ai.common.util;
import org.apache.commons.lang3.StringUtils;
/**
 * String utility class
 */
public final class StringUtil {
    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    /**
     * Check if string is blank
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }
    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
    /**
     * Safe trim
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
}
