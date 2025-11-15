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
import java.util.Map;
/**
 * Plain text file parser - fallback parser for all text files
 */
@Slf4j
public class PlainTextFileParser implements IFileParser {
    @Override
    public boolean support(File file) {
        // Support all files as fallback
        return true;
    }
    @Override
    public PreProcessedData parse(File file) throws Exception {
        log.debug("Parsing text file: {}", file.getAbsolutePath());
        try {
            String content = FileUtil.readFileToString(file);
            // Extract basic metadata
            Map<String, Object> context = new HashMap<>();
            context.put("lineCount", content.split("\\r?\\n").length);
            context.put("characterCount", content.length());
            // Build metadata
            FileMetadata metadata = FileMetadata.builder()
                    .filePath(file.toPath())
                    .fileName(file.getName())
                    .fileType(FileUtil.getFileExtension(file))
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
            log.error("Error parsing text file: {}", file.getName(), e);
            throw new ParseException("Text parse error: " + file.getName(), e);
        }
    }
    @Override
    public int getPriority() {
        return 0; // Lowest priority - fallback parser
    }
}
