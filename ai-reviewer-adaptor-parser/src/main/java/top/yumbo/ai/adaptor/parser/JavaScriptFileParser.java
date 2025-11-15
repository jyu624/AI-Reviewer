package top.yumbo.ai.adaptor.parser;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * JavaScript file parser using pattern matching and simple AST analysis
 * Extracts functions, classes, imports/exports from JavaScript/TypeScript code
 */
@Slf4j
public class JavaScriptFileParser extends AbstractASTParser {
    // Regex patterns for JavaScript/TypeScript syntax
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(?:(?:\\{[^}]+\\}|\\*\\s+as\\s+\\w+|\\w+)\\s+from\\s+)?['\"]([^'\"]+)['\"]");
    private static final Pattern EXPORT_PATTERN = Pattern.compile("^\\s*export\\s+(?:default\\s+)?(?:class|function|const|let|var|interface|type)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?(?:default\\s+)?class\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?\\s*\\{?");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?(?:default\\s+)?(?:async\\s+)?function\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern ARROW_FUNCTION_PATTERN = Pattern.compile("^\\s*(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?\\(([^)]*)\\)\\s*=>");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^\\s*(?:async\\s+)?(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?interface\\s+(\\w+)(?:\\s+extends\\s+[\\w,\\s]+)?\\s*\\{");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*(?:export\\s+)?type\\s+(\\w+)\\s*=");
    @Override
    protected String[] getSupportedExtensions() {
        return new String[]{".js", ".jsx", ".ts", ".tsx", ".mjs", ".cjs"};
    }
    @Override
    protected String getLanguageName() {
        return "JavaScript";
    }
    @Override
    protected Map<String, Object> parseAST(String content, File file) throws Exception {
        Map<String, Object> astInfo = new HashMap<>();
        // Detect if TypeScript
        boolean isTypeScript = file.getName().endsWith(".ts") || file.getName().endsWith(".tsx");
        astInfo.put("isTypeScript", isTypeScript);
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
        // Extract exports
        int exportCount = countExports(content);
        astInfo.put("exportCount", exportCount);
        if (isTypeScript) {
            // Extract TypeScript-specific constructs
            List<Map<String, Object>> interfaces = extractInterfacesFromContent(content);
            astInfo.put("interfaces", interfaces);
            astInfo.put("interfaceCount", interfaces.size());
            List<String> types = extractTypesFromContent(content);
            astInfo.put("types", types);
            astInfo.put("typeCount", types.size());
        }
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
     * Extract import statements from JavaScript code
     */
    private List<String> extractImportsFromContent(String content) throws Exception {
        List<String> imports = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find()) {
                imports.add(line.trim());
            }
            // Also handle require statements
            if (line.contains("require(") && line.contains("=")) {
                imports.add(line.trim());
            }
        }
        return imports;
    }
    /**
     * Extract class definitions from JavaScript code
     */
    private List<Map<String, Object>> extractClassesFromContent(String content) throws Exception {
        List<Map<String, Object>> classes = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("name", classMatcher.group(1));
                classInfo.put("lineNumber", lineNumber);
                if (classMatcher.group(2) != null) {
                    classInfo.put("extends", classMatcher.group(2));
                }
                classInfo.put("isExported", line.trim().startsWith("export"));
                // Extract methods within the class
                List<Map<String, Object>> methods = extractMethodsFromClass(reader);
                classInfo.put("methods", methods);
                classInfo.put("methodCount", methods.size());
                classes.add(classInfo);
            }
        }
        return classes;
    }
    /**
     * Extract function definitions from JavaScript code
     */
    private List<Map<String, Object>> extractFunctionsFromContent(String content) throws Exception {
        List<Map<String, Object>> functions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            // Regular function declaration
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
            if (functionMatcher.find()) {
                Map<String, Object> functionInfo = new HashMap<>();
                functionInfo.put("name", functionMatcher.group(1));
                functionInfo.put("parameters", functionMatcher.group(2).trim());
                functionInfo.put("lineNumber", lineNumber);
                functionInfo.put("type", "function");
                functionInfo.put("isExported", line.trim().startsWith("export"));
                functionInfo.put("isAsync", line.contains("async"));
                functions.add(functionInfo);
                continue;
            }
            // Arrow function
            Matcher arrowMatcher = ARROW_FUNCTION_PATTERN.matcher(line);
            if (arrowMatcher.find()) {
                Map<String, Object> functionInfo = new HashMap<>();
                functionInfo.put("name", arrowMatcher.group(1));
                functionInfo.put("parameters", arrowMatcher.group(2).trim());
                functionInfo.put("lineNumber", lineNumber);
                functionInfo.put("type", "arrow");
                functionInfo.put("isAsync", line.contains("async"));
                functions.add(functionInfo);
            }
        }
        return functions;
    }
    /**
     * Extract methods from a class body
     */
    private List<Map<String, Object>> extractMethodsFromClass(BufferedReader reader) throws Exception {
        List<Map<String, Object>> methods = new ArrayList<>();
        String line;
        int braceCount = 1; // Already in class body
        while ((line = reader.readLine()) != null && braceCount > 0) {
            // Count braces to know when we exit the class
            braceCount += line.chars().filter(ch -> ch == '{').count();
            braceCount -= line.chars().filter(ch -> ch == '}').count();
            if (braceCount == 0) break;
            Matcher methodMatcher = METHOD_PATTERN.matcher(line);
            if (methodMatcher.find()) {
                Map<String, Object> methodInfo = new HashMap<>();
                methodInfo.put("name", methodMatcher.group(1));
                methodInfo.put("parameters", methodMatcher.group(2).trim());
                methodInfo.put("isAsync", line.contains("async"));
                methods.add(methodInfo);
            }
        }
        return methods;
    }
    /**
     * Extract TypeScript interfaces
     */
    private List<Map<String, Object>> extractInterfacesFromContent(String content) throws Exception {
        List<Map<String, Object>> interfaces = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(line);
            if (interfaceMatcher.find()) {
                Map<String, Object> interfaceInfo = new HashMap<>();
                interfaceInfo.put("name", interfaceMatcher.group(1));
                interfaceInfo.put("lineNumber", lineNumber);
                interfaceInfo.put("isExported", line.trim().startsWith("export"));
                interfaces.add(interfaceInfo);
            }
        }
        return interfaces;
    }
    /**
     * Extract TypeScript type aliases
     */
    private List<String> extractTypesFromContent(String content) throws Exception {
        List<String> types = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher typeMatcher = TYPE_PATTERN.matcher(line);
            if (typeMatcher.find()) {
                types.add(typeMatcher.group(1));
            }
        }
        return types;
    }
    /**
     * Count export statements
     */
    private int countExports(String content) throws Exception {
        int count = 0;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            if (EXPORT_PATTERN.matcher(line).find() || line.trim().startsWith("export {")) {
                count++;
            }
        }
        return count;
    }
    @Override
    protected boolean isCommentLine(String line) {
        return line.startsWith("//") || 
               line.startsWith("/*") || 
               line.startsWith("*") ||
               line.startsWith("*/");
    }
    @Override
    public int getPriority() {
        return 8; // Higher priority for JavaScript files
    }
}
