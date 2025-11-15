package top.yumbo.ai.api.ai;
import top.yumbo.ai.api.model.AIConfig;
import top.yumbo.ai.api.model.AIResponse;
import top.yumbo.ai.api.model.PreProcessedData;
/**
 * AI service interface for invoking different AI providers
 */
public interface IAIService {
    /**
     * Invoke AI service with preprocessed data
     * 
     * @param data preprocessed data from parser
     * @param config AI configuration
     * @return AI response
     * @throws Exception if AI invocation fails
     */
    AIResponse invoke(PreProcessedData data, AIConfig config) throws Exception;
    /**
     * Check if the AI service is available
     * 
     * @return true if available, false otherwise
     */
    boolean isAvailable();
    /**
     * Get the service provider name
     * 
     * @return provider name (e.g., \"OpenAI\", \"Claude\")
     */
    String getProviderName();
    /**
     * Get supported model names
     * 
     * @return array of supported model identifiers
     */
    default String[] getSupportedModels() {
        return new String[0];
    }
}
