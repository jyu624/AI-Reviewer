package top.yumbo.ai.adaptor.parser;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Python file parser using pattern matching and simple AST analysis
 * Extracts functions, classes, decorators, and docstrings from Python code
 */
@Slf4j
public class PythonFileParser extends AbstractASTParser {
    // Regex patterns for Python syntax
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*(import\\s+[\\w.,\\s]+|from\\s+[\\w.]+\\s+import\\s+[\\w.,\\s*]+)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*class\\s+(\\w+)\\s*(?:\\([^)]*\\))?\\s*:");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\s*def\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:->\\s*[^:]+)?\\s*:");
    private static final Pattern DECORATOR_PATTERN = Pattern.compile("^\\s*@(\\w+(?:\\.\\w+)*)(?:\\([^)]*\\))?");
    private static final Pattern DOCSTRING_PATTERN = Pattern.compile("^\\s*[\"\"\"\'\'\']{3}");
    @Override
    protected String[] getSupportedExtensions() {
        return new String[]{".py"};
    }
    @Override
    protected String getLanguageName() {
        return "Python";
    }
    @Override
    protected Map<String, Object> parseAST(String content, File file) throws Exception {
        Map<String, Object> astInfo = new HashMap<>();
        // Extract imports
        List<String> imports = extractImportsFromContent(content);
        astInfo.put("imports", imports);
        astInfo.put("importCount", imports.size());
        // Extract classes
        List<Map<String, Object>> classes = extractClassesFromContent(content);
        astInfo.put("classes", classes);
        astInfo.put("classCount", classes.size());
        // Extract functions
        List<Map<String, Object>> functions = extractFunctionsFromContent(content);
        astInfo.put("functions", functions);
        astInfo.put("functionCount", functions.size());
        // Extract decorators
        List<String> decorators = extractDecoratorsFromContent(content);
        astInfo.put("decorators", decorators);
        astInfo.put("decoratorCount", decorators.size());
        return astInfo;
    }
    @Override
    protected List<Map<String, Object>> extractFunctions(Object astData) {
        if (astData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> astMap = (Map<String, Object>) astData;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> functions = (List<Map<String, Object>>) astMap.get("functions");
            return functions != null ? functions : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    @Override
    protected List<Map<String, Object>> extractClasses(Object astData) {
        if (astData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> astMap = (Map<String, Object>) astData;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classes = (List<Map<String, Object>>) astMap.get("classes");
            return classes != null ? classes : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    @Override
    protected List<String> extractImports(Object astData) {
        if (astData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> astMap = (Map<String, Object>) astData;
            @SuppressWarnings("unchecked")
            List<String> imports = (List<String>) astMap.get("imports");
            return imports != null ? imports : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    /**
     * Extract import statements from Python code
     */
    private List<String> extractImportsFromContent(String content) throws Exception {
        List<String> imports = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find()) {
                imports.add(matcher.group(1).trim());
            }
        }
        return imports;
    }
    /**
     * Extract class definitions from Python code
     */
    private List<Map<String, Object>> extractClassesFromContent(String content) throws Exception {
        List<Map<String, Object>> classes = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNumber = 0;
        List<String> currentDecorators = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            // Check for decorators
            Matcher decoratorMatcher = DECORATOR_PATTERN.matcher(line);
            if (decoratorMatcher.find()) {
                currentDecorators.add(decoratorMatcher.group(1));
                continue;
            }
            // Check for class definition
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("name", classMatcher.group(1));
                classInfo.put("lineNumber", lineNumber);
                classInfo.put("decorators", new ArrayList<>(currentDecorators));
                // Try to extract docstring
                String docstring = extractDocstring(reader);
                if (docstring != null) {
                    classInfo.put("docstring", docstring);
                }
                classes.add(classInfo);
                currentDecorators.clear();
            } else if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                // Clear decorators if we hit non-decorator, non-empty line
                currentDecorators.clear();
            }
        }
        return classes;
    }
    /**
     * Extract function definitions from Python code
     */
    private List<Map<String, Object>> extractFunctionsFromContent(String content) throws Exception {
        List<Map<String, Object>> functions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNumber = 0;
        List<String> currentDecorators = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            // Check for decorators
            Matcher decoratorMatcher = DECORATOR_PATTERN.matcher(line);
            if (decoratorMatcher.find()) {
                currentDecorators.add(decoratorMatcher.group(1));
                continue;
            }
            // Check for function definition
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
            if (functionMatcher.find()) {
                Map<String, Object> functionInfo = new HashMap<>();
                functionInfo.put("name", functionMatcher.group(1));
                functionInfo.put("parameters", functionMatcher.group(2).trim());
                functionInfo.put("lineNumber", lineNumber);
                functionInfo.put("decorators", new ArrayList<>(currentDecorators));
                // Try to extract docstring
                String docstring = extractDocstring(reader);
                if (docstring != null) {
                    functionInfo.put("docstring", docstring);
                }
                functions.add(functionInfo);
                currentDecorators.clear();
            } else if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                // Clear decorators if we hit non-decorator, non-empty line
                currentDecorators.clear();
            }
        }
        return functions;
    }
    /**
     * Extract decorators from Python code
     */
    private List<String> extractDecoratorsFromContent(String content) throws Exception {
        List<String> decorators = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = DECORATOR_PATTERN.matcher(line);
            if (matcher.find()) {
                decorators.add(matcher.group(1));
            }
        }
        return decorators;
    }
    /**
     * Extract docstring following a class or function definition
     */
    private String extractDocstring(BufferedReader reader) throws Exception {
        // Mark the reader position
        reader.mark(1000);
        String nextLine = reader.readLine();
        if (nextLine != null) {
            Matcher docstringMatcher = DOCSTRING_PATTERN.matcher(nextLine);
            if (docstringMatcher.find()) {
                StringBuilder docstring = new StringBuilder();
                docstring.append(nextLine.trim());
                // Read until closing triple quotes
                String line;
                while ((line = reader.readLine()) != null) {
                    docstring.append("\n").append(line);
                    if (line.trim().endsWith("\"\"\"") || line.trim().endsWith("\'\'\'")) {
                        return docstring.toString();
                    }
                }
                return docstring.toString();
            }
        }
        // Reset if no docstring found
        reader.reset();
        return null;
    }
    @Override
    protected boolean isCommentLine(String line) {
        return line.startsWith("#") || line.startsWith("\"\"\"") || line.startsWith("\'\'\'");
    }
    @Override
    public int getPriority() {
        return 8; // Higher priority for Python files
    }
}
