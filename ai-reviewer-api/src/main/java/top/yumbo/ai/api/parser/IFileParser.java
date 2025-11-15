package top.yumbo.ai.api.parser;
import top.yumbo.ai.api.model.PreProcessedData;
import java.io.File;
/**
 * File parser interface for converting files into preprocessed data
 */
public interface IFileParser {
    /**
     * Check if this parser supports the given file
     * 
     * @param file the file to check
     * @return true if supported, false otherwise
     */
    boolean support(File file);
    /**
     * Parse the file into preprocessed data
     * 
     * @param file the file to parse
     * @return preprocessed data ready for AI processing
     * @throws Exception if parsing fails
     */
    PreProcessedData parse(File file) throws Exception;
    /**
     * Get the parser name
     * 
     * @return parser identifier
     */
    default String getParserName() {
        return this.getClass().getSimpleName();
    }
    /**
     * Get the priority of this parser (higher = more priority)
     * Used when multiple parsers support the same file
     * 
     * @return priority value, default is 0
     */
    default int getPriority() {
        return 0;
    }
}
