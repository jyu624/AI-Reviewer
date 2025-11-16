package top.yumbo.ai.core.registry;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.api.ai.IAIService;
import top.yumbo.ai.api.parser.IFileParser;
import top.yumbo.ai.api.processor.IResultProcessor;
import top.yumbo.ai.api.source.FileSourceConfig;
import top.yumbo.ai.api.source.IFileSource;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter registry for managing parsers, AI services, processors, and file sources
 */
@Slf4j
public class AdapterRegistry {
    private final Map<String, IFileParser> parsers = new ConcurrentHashMap<>();
    private final Map<String, IAIService> aiServices = new ConcurrentHashMap<>();
    private final Map<String, IResultProcessor> processors = new ConcurrentHashMap<>();
    private final Map<String, IFileSource> fileSources = new ConcurrentHashMap<>();

    /**
     * Register a file parser
     */
    public void registerParser(IFileParser parser) {
        parsers.put(parser.getParserName(), parser);
        log.info("Registered parser: {}", parser.getParserName());
    }

    /**
     * Register an AI service
     */
    public void registerAIService(IAIService aiService) {
        aiServices.put(aiService.getProviderName(), aiService);
        log.info("Registered AI service: {}", aiService.getProviderName());
    }

    /**
     * Register a result processor
     */
    public void registerProcessor(IResultProcessor processor) {
        processors.put(processor.getProcessorType(), processor);
        log.info("Registered processor: {}", processor.getProcessorType());
    }

    /**
     * Register a file source
     */
    public void registerFileSource(IFileSource fileSource) {
        fileSources.put(fileSource.getSourceName(), fileSource);
        log.info("Registered file source: {}", fileSource.getSourceName());
    }

    /**
     * Get parser for file
     */
    public Optional<IFileParser> getParser(File file) {
        return parsers.values().stream()
                .filter(parser -> parser.support(file))
                .max(Comparator.comparingInt(IFileParser::getPriority));
    }

    /**
     * Get AI service by provider name
     */
    public Optional<IAIService> getAIService(String providerName) {
        return Optional.ofNullable(aiServices.get(providerName));
    }

    /**
     * Get processor by type
     */
    public Optional<IResultProcessor> getProcessor(String processorType) {
        return Optional.ofNullable(processors.get(processorType));
    }

    /**
     * Get file source for the given configuration
     * Selects the file source with highest priority that supports the config
     */
    public Optional<IFileSource> getFileSource(FileSourceConfig config) {
        return fileSources.values().stream()
                .filter(fs -> fs.support(config))
                .max(Comparator.comparingInt(IFileSource::getPriority));
    }

    /**
     * Get file source by name
     */
    public Optional<IFileSource> getFileSourceByName(String sourceName) {
        return Optional.ofNullable(fileSources.get(sourceName));
    }

    /**
     * Get all registered parsers
     */
    public Collection<IFileParser> getAllParsers() {
        return Collections.unmodifiableCollection(parsers.values());
    }

    /**
     * Get all registered AI services
     */
    public Collection<IAIService> getAllAIServices() {
        return Collections.unmodifiableCollection(aiServices.values());
    }

    /**
     * Get all registered processors
     */
    public Collection<IResultProcessor> getAllProcessors() {
        return Collections.unmodifiableCollection(processors.values());
    }

    /**
     * Get all registered file sources
     */
    public Collection<IFileSource> getAllFileSources() {
        return Collections.unmodifiableCollection(fileSources.values());
    }

    /**
     * Clear registered parsers
     */
    public void clearParsers() {
        parsers.clear();
        log.info("Cleared all registered parsers");
    }

    public void clearAIServices() {
        aiServices.clear();
        log.info("Cleared all registered AI services");
    }

    public void clearProcessors() {
        processors.clear();
        log.info("Cleared all registered processors");
    }

    /**
     * Clear registered file sources
     */
    public void clearFileSources() {
        fileSources.clear();
        log.info("Cleared all registered file sources");
    }

    /**
     * Load adapters using SPI
     */
    public void loadAdaptersFromSPI() {
        // Load parsers
        ServiceLoader<IFileParser> parserLoader = ServiceLoader.load(IFileParser.class);
        parserLoader.forEach(this::registerParser);
        // Load AI services
        ServiceLoader<IAIService> aiServiceLoader = ServiceLoader.load(IAIService.class);
        aiServiceLoader.forEach(this::registerAIService);
        // Load processors
        ServiceLoader<IResultProcessor> processorLoader = ServiceLoader.load(IResultProcessor.class);
        processorLoader.forEach(this::registerProcessor);
        // Load file sources
        ServiceLoader<IFileSource> fileSourceLoader = ServiceLoader.load(IFileSource.class);
        fileSourceLoader.forEach(this::registerFileSource);
        log.info("Loaded adapters from SPI - Parsers: {}, AI Services: {}, Processors: {}, File Sources: {}",
                parsers.size(), aiServices.size(), processors.size(), fileSources.size());
    }
}
