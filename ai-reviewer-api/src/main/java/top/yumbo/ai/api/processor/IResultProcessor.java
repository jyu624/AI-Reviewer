package top.yumbo.ai.api.processor;
import top.yumbo.ai.api.model.AIResponse;
import top.yumbo.ai.api.model.ProcessResult;
import top.yumbo.ai.api.model.ProcessorConfig;
import java.util.List;
/**
 * Result processor interface for converting AI responses into final output
 */
public interface IResultProcessor {
    /**
     * Process AI responses into final result
     * 
     * @param responses list of AI responses
     * @param config processor configuration
     * @return processed result
     * @throws Exception if processing fails
     */
    ProcessResult process(List<AIResponse> responses, ProcessorConfig config) throws Exception;
    /**
     * Get the processor type identifier
     * 
     * @return processor type (e.g., \"code-review\", \"document-generator\")
     */
    String getProcessorType();
    /**
     * Get supported output formats
     * 
     * @return array of supported formats (e.g., "markdown", "html", "json")
     */
    default String[] getSupportedFormats() {
        return new String[]{"markdown"};
    }
}
