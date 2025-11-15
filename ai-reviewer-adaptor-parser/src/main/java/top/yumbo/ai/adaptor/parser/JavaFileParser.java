package top.yumbo.ai.adaptor.parser;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.api.model.FileMetadata;
import top.yumbo.ai.api.model.PreProcessedData;
import top.yumbo.ai.api.parser.IFileParser;
import top.yumbo.ai.common.constants.Constants;
import top.yumbo.ai.common.exception.ParseException;
import top.yumbo.ai.common.util.FileUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * Java file parser using JavaParser library
 */
@Slf4j
public class JavaFileParser implements IFileParser {
    private final JavaParser javaParser = new JavaParser();
    @Override
    public boolean support(File file) {
        return file.getName().endsWith(Constants.JAVA_FILE_EXTENSION);
    }
    @Override
    public PreProcessedData parse(File file) throws Exception {
        log.debug("Parsing Java file: {}", file.getAbsolutePath());
        try {
            String content = FileUtil.readFileToString(file);
            // Parse Java file
//            CompilationUnit cu = javaParser.parse(file).getResult()
//                    .orElseThrow(() -> new ParseException("Failed to parse Java file: " + file.getName()));
            // Extract metadata
//            Map<String, Object> context = new HashMap<>();
//            cu.getPackageDeclaration().ifPresent(pkg ->
//                context.put("package", pkg.getNameAsString()));
//            context.put("imports", cu.getImports().size());
//            context.put("types", cu.getTypes().size());
            // Build metadata
            FileMetadata metadata = FileMetadata.builder()
                    .filePath(file.toPath())
                    .fileName(file.getName())
                    .fileType("java")
                    .fileSize(file.length())
                    .encoding(Constants.DEFAULT_ENCODING)
                    .build();
            return PreProcessedData.builder()
                    .metadata(metadata)
                    .content(content)
//                    .context(context)
                    .parserName(getParserName())
                    .build();
        } catch (Exception e) {
            log.error("Error parsing Java file: {}", file.getName(), e);
            throw new ParseException("Java parse error: " + file.getName(), e);
        }
    }
    @Override
    public int getPriority() {
        return 10; // Higher priority for Java files
    }
}
