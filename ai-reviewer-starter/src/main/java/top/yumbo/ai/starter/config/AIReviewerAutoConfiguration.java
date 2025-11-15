package top.yumbo.ai.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yumbo.ai.adaptor.ai.HttpBasedAIAdapter;
import top.yumbo.ai.adaptor.parser.JavaFileParser;
import top.yumbo.ai.adaptor.parser.JavaScriptFileParser;
import top.yumbo.ai.adaptor.parser.PlainTextFileParser;
import top.yumbo.ai.adaptor.parser.PythonFileParser;
import top.yumbo.ai.adaptor.processor.CodeReviewProcessor;
import top.yumbo.ai.api.model.AIConfig;
import top.yumbo.ai.core.AIEngine;
import top.yumbo.ai.core.registry.AdapterRegistry;

/**
 * Auto-configuration for AI Reviewer
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AIReviewerProperties.class)
public class AIReviewerAutoConfiguration {

    @Autowired
    AIReviewerProperties properties;

    @Bean
    public AdapterRegistry adapterRegistry() {
        AIConfig aiConfig = AIConfig.builder()
                .provider(properties.getAi().getProvider())
                .model(properties.getAi().getModel())
                .apiKey(properties.getAi().getApiKey())
                .sysPrompt(properties.getAi().getSysPrompt())
                .userPrompt(properties.getAi().getUserPrompt())
                .endpoint(properties.getAi().getEndpoint())
                .temperature(properties.getAi().getTemperature())
                .maxTokens(properties.getAi().getMaxTokens())
                .timeoutSeconds(properties.getAi().getTimeoutSeconds())
                .maxRetries(properties.getAi().getMaxRetries())
                .build();
        log.info("Initializing AdapterRegistry");
        AdapterRegistry registry = new AdapterRegistry();
        registry.registerParser(new JavaFileParser());
        registry.registerParser(new PythonFileParser());
        registry.registerParser(new JavaScriptFileParser());
        registry.registerParser(new PlainTextFileParser());
        registry.registerAIService(new HttpBasedAIAdapter(aiConfig));
        registry.registerProcessor(new CodeReviewProcessor());
        registry.loadAdaptersFromSPI();
        return registry;
    }

    @Bean
    public AIEngine aiEngine(AdapterRegistry registry) {
        log.info("Initializing AIEngine");
        return new AIEngine(registry);
    }
}
