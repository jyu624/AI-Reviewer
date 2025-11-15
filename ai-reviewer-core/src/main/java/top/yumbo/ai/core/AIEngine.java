package top.yumbo.ai.core;

import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.api.ai.IAIService;
import top.yumbo.ai.api.model.AIResponse;
import top.yumbo.ai.api.model.PreProcessedData;
import top.yumbo.ai.api.model.ProcessResult;
import top.yumbo.ai.api.parser.IFileParser;
import top.yumbo.ai.api.processor.IResultProcessor;
import top.yumbo.ai.common.exception.AIReviewerException;
import top.yumbo.ai.core.context.ExecutionContext;
import top.yumbo.ai.core.filter.FileFilter;
import top.yumbo.ai.core.registry.AdapterRegistry;
import top.yumbo.ai.core.scanner.FileScanner;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Main AI Engine for orchestrating the entire processing pipeline
 */
@Slf4j
public class AIEngine {
    protected final AdapterRegistry registry;
    protected final FileScanner fileScanner;
    protected final FileFilter fileFilter;
    protected ExecutorService executorService;

    public AIEngine(AdapterRegistry registry) {
        this.registry = registry;
        this.fileScanner = new FileScanner();
        this.fileFilter = new FileFilter();
    }

    /**
     * Execute the AI review process
     */
    public ProcessResult execute(ExecutionContext context) {
        context.setExecutionId(UUID.randomUUID().toString());
        context.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        log.info("Starting AI Engine execution: {}", context.getExecutionId());

        try {
            // Initialize thread pool
            this.executorService = Executors.newFixedThreadPool(context.getThreadPoolSize());

            // Step 1: Scan files
            long scanStartMs = System.currentTimeMillis();
            List<Path> files = fileScanner.scan(context.getTargetDirectory());
            long scanTimeMs = System.currentTimeMillis() - scanStartMs;
            log.debug("File scanning took {} ms", scanTimeMs);

            // Step 2: Filter files
            long filterStartMs = System.currentTimeMillis();
            List<Path> filteredFiles = fileFilter.filter(files,
                    context.getIncludePatterns(),
                    context.getExcludePatterns());
            long filterTimeMs = System.currentTimeMillis() - filterStartMs;
            log.debug("File filtering took {} ms", filterTimeMs);

            // Step 3: Parse files
            long parseStartMs = System.currentTimeMillis();
            List<PreProcessedData> preprocessedDataList = parseFiles(filteredFiles);
            long parseTimeMs = System.currentTimeMillis() - parseStartMs;
            context.setParsingTimeMs(parseTimeMs);
            log.info("File parsing took {} ms", parseTimeMs);

            // Step 4: Invoke AI service
            long aiStartMs = System.currentTimeMillis();
            List<AIResponse> aiResponses = invokeAI(preprocessedDataList, context);
            long aiTimeMs = System.currentTimeMillis() - aiStartMs;
            context.setAiInvocationTimeMs(aiTimeMs);
            log.info("AI invocation took {} ms", aiTimeMs);

            // Prepare timing information for processor
            if (context.getProcessorConfig().getCustomParams() == null) {
                context.getProcessorConfig().setCustomParams(new java.util.HashMap<>());
            }
            context.getProcessorConfig().getCustomParams().put("executionId", context.getExecutionId());
            context.getProcessorConfig().getCustomParams().put("scanTimeMs", scanTimeMs);
            context.getProcessorConfig().getCustomParams().put("filterTimeMs", filterTimeMs);
            context.getProcessorConfig().getCustomParams().put("parsingTimeMs", parseTimeMs);
            context.getProcessorConfig().getCustomParams().put("aiInvocationTimeMs", aiTimeMs);

            // Step 5: Process results
            long processStartMs = System.currentTimeMillis();
            ProcessResult result = processResults(aiResponses, context);
            long processTimeMs = System.currentTimeMillis() - processStartMs;
            context.setResultProcessingTimeMs(processTimeMs);
            log.info("Result processing took {} ms", processTimeMs);

            // Calculate total time
            context.setEndTime(LocalDateTime.now());


            // Update processor config with final timing
            context.getProcessorConfig().getCustomParams().put("resultProcessingTimeMs", processTimeMs);

            // Add timing information to result metadata
            if (result.getMetadata() == null) {
                result.setMetadata(new java.util.HashMap<>());
            }
            result.getMetadata().put("executionId", context.getExecutionId());
            result.getMetadata().put("parsingTimeMs", parseTimeMs);
            result.getMetadata().put("aiInvocationTimeMs", aiTimeMs);
            result.getMetadata().put("resultProcessingTimeMs", processTimeMs);
            result.getMetadata().put("scanTimeMs", scanTimeMs);
            result.getMetadata().put("filterTimeMs", filterTimeMs);

            log.info("AI Engine execution completed: {} ( parsing: {} ms, AI: {} ms, processing: {} ms)",
                    context.getExecutionId(), parseTimeMs, aiTimeMs, processTimeMs);
            return result;

        } catch (Exception e) {
            log.error("AI Engine execution failed: {}", context.getExecutionId(), e);
            context.setEndTime(LocalDateTime.now());

            return ProcessResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .metadata(java.util.Map.of(
                            "executionId", context.getExecutionId()
                    ))
                    .build();
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    /**
     * Parse files using registered parsers
     */
    public List<PreProcessedData> parseFiles(List<Path> files) throws InterruptedException, ExecutionException {
        log.info("Parsing {} files", files.size());
        List<Future<PreProcessedData>> futures = new ArrayList<>();
        for (Path file : files) {
            Future<PreProcessedData> future = executorService.submit(() -> {
                IFileParser parser = registry.getParser(file.toFile())
                        .orElseThrow(() -> new AIReviewerException("No parser found for file: " + file));
                try {
                    return parser.parse(file.toFile());
                } catch (Exception e) {
                    log.error("Failed to parse file: {}", file, e);
                    throw new AIReviewerException("Parse failed: " + file, e);
                }
            });
            futures.add(future);
        }
        List<PreProcessedData> results = new ArrayList<>();
        for (Future<PreProcessedData> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Failed to get parse result", e);
            }
        }
        log.info("Successfully parsed {} files", results.size());
        return results;
    }

    /**
     * Invoke AI service
     */
    public List<AIResponse> invokeAI(List<PreProcessedData> dataList, ExecutionContext context)
            throws InterruptedException, ExecutionException {
        log.info("Invoking AI service for {} items", dataList.size());
        IAIService aiService = registry.getAIService(context.getAiConfig().getProvider())
                .orElseThrow(() -> new AIReviewerException("AI service not found: " +
                        context.getAiConfig().getProvider()));
        List<Future<AIResponse>> futures = new ArrayList<>();
        for (PreProcessedData data : dataList) {
            Future<AIResponse> future = executorService.submit(() -> {
                try {
                    return aiService.invoke(data, context.getAiConfig());
                } catch (Exception e) {
                    log.error("AI invocation failed", e);
                    throw new AIReviewerException("AI invocation failed", e);
                }
            });
            futures.add(future);
        }
        List<AIResponse> results = new ArrayList<>();
        for (Future<AIResponse> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Failed to get AI response", e);
            }
        }
        log.info("Received {} AI responses", results.size());
        return results;
    }

    /**
     * Process results
     */
    public ProcessResult processResults(List<AIResponse> responses, ExecutionContext context) throws Exception {
        log.info("Processing {} AI responses", responses.size());

        IResultProcessor processor = registry.getProcessor(context.getProcessorConfig().getProcessorType())
                .orElseThrow(() -> new AIReviewerException("Processor not found: " +
                        context.getProcessorConfig().getProcessorType()));

        return processor.process(responses, context.getProcessorConfig());
    }
}
