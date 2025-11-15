package top.yumbo.ai.adaptor.parser;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.api.model.FileMetadata;
import top.yumbo.ai.api.model.PreProcessedData;
import top.yumbo.ai.api.parser.IFileParser;
import top.yumbo.ai.common.constants.Constants;
import top.yumbo.ai.common.exception.ParseException;
import top.yumbo.ai.common.util.FileUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Abstract base class for AST-based file parsers
 * Provides common functionality for parsing source code using Abstract Syntax Tree
 */
@Slf4j
public abstract class AbstractASTParser implements IFileParser {
    /**
     * Get the file extensions this parser supports
     * 
     * @return array of supported file extensions (e.g., ".py", ".js")
     */
    protected abstract String[] getSupportedExtensions();
    /**
     * Get the language name for this parser
     * 
     * @return language name (e.g., "Python", "JavaScript")
     */
    protected abstract String getLanguageName();
    /**
     * Parse the file content and extract AST information
     * 
     * @param content file content as string
     * @param file original file
     * @return map containing extracted AST information
     * @throws Exception if parsing fails
     */
    protected abstract Map<String, Object> parseAST(String content, File file) throws Exception;
    @Override
    public boolean support(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        for (String extension : getSupportedExtensions()) {
            if (fileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public PreProcessedData parse(File file) throws Exception {
        log.debug("Parsing {} file: {}", getLanguageName(), file.getAbsolutePath());
        try {
            // Read file content
            String content = FileUtil.readFileToString(file);
            // Parse AST and extract information
            Map<String, Object> astInfo = parseAST(content, file);
            // Build context with AST information
            Map<String, Object> context = new HashMap<>();
            context.put("language", getLanguageName());
            context.put("astInfo", astInfo);
            context.putAll(extractBasicMetrics(content));
            // Build file metadata
            FileMetadata metadata = FileMetadata.builder()
                    .filePath(file.toPath())
                    .fileName(file.getName())
                    .fileType(getLanguageName().toLowerCase())
                    .fileSize(file.length())
                    .encoding(Constants.DEFAULT_ENCODING)
                    .build();
            return PreProcessedData.builder()
                    .metadata(metadata)
                    .content(content)
                    .context(context)
                    .parserName(getParserName())
                    .build();
        } catch (Exception e) {
            log.error("Error parsing {} file: {}", getLanguageName(), file.getName(), e);
            throw new ParseException(getLanguageName() + " parse error: " + file.getName(), e);
        }
    }
    /**
     * Extract basic code metrics from content
     * 
     * @param content file content
     * @return map of metrics
     */
    protected Map<String, Object> extractBasicMetrics(String content) {
        Map<String, Object> metrics = new HashMap<>();
        String[] lines = content.split("\\r?\\n");
        metrics.put("totalLines", lines.length);
        // Count non-empty lines
        long nonEmptyLines = 0;
        long commentLines = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                nonEmptyLines++;
            }
            if (isCommentLine(trimmed)) {
                commentLines++;
            }
        }
        metrics.put("nonEmptyLines", nonEmptyLines);
        metrics.put("commentLines", commentLines);
        metrics.put("codeLines", nonEmptyLines - commentLines);
        metrics.put("characterCount", content.length());
        return metrics;
    }
    /**
     * Check if a line is a comment line
     * Override this method for language-specific comment detection
     * 
     * @param line trimmed line content
     * @return true if line is a comment
     */
    protected boolean isCommentLine(String line) {
        return line.startsWith("//") || 
               line.startsWith("#") || 
               line.startsWith("/*") || 
               line.startsWith("*") ||
               line.startsWith("*/");
    }
    /**
     * Extract function/method information from AST
     * 
     * @param astData AST data structure
     * @return list of function information maps
     */
    protected abstract List<Map<String, Object>> extractFunctions(Object astData);
    /**
     * Extract class/type information from AST
     * 
     * @param astData AST data structure
     * @return list of class information maps
     */
    protected abstract List<Map<String, Object>> extractClasses(Object astData);
    /**
     * Extract import/dependency information from AST
     * 
     * @param astData AST data structure
     * @return list of import statements
     */
    protected abstract List<String> extractImports(Object astData);
    @Override
    public int getPriority() {
        return 5; // Medium priority for AST parsers
    }
}
