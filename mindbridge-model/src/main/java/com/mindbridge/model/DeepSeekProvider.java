package com.mindbridge.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindbridge.common.enums.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DeepSeekProvider implements ModelProvider {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekProvider.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public DeepSeekProvider(
            @Value("${mindbridge.deepseek.api-key:}") String apiKey,
            @Value("${mindbridge.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${mindbridge.deepseek.model:deepseek-chat}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.debug("DeepSeekProvider initialized: baseUrl={}, model={}, available={}", baseUrl, model, isAvailable());
    }

    @Override
    public Flux<String> stream(Prompt prompt) {
        if (!isAvailable()) {
            log.warn("DeepSeek not available, returning empty");
            return Flux.empty();
        }
        List<Map<String, String>> messages = prompt.getInstructions().stream()
                .map(msg -> Map.of("role", "user", "content", msg.getText()))
                .collect(Collectors.toList());
        log.debug("DeepSeek streaming request: model={}, messages={}", model, messages);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "stream", true);

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.debug("SSE chunk: [{}]", chunk.replace("\n", "\\n").replace("\r", "\\r")))
                .flatMap(chunk -> Flux.fromArray(chunk.split("\\r?\\n")))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.equals("[DONE]"))
                .doOnNext(line -> log.debug("SSE line: {}", line.substring(0, Math.min(80, line.length()))))
                .map(line -> {
                    String token = extractDeltaContent(line);
                    log.debug("Extracted token: '{}'", token);
                    return token;
                })
                .filter(token -> !token.isEmpty())
                .doOnError(e -> log.error("DeepSeek stream error: {}", e.getMessage(), e));
    }

    @Override
    public String call(Prompt prompt) {
        if (!isAvailable()) {
            return "";
        }
        List<Map<String, String>> messages = prompt.getInstructions().stream()
                .map(msg -> Map.of("role", "user", "content", msg.getText()))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "stream", false);

        log.debug("DeepSeek call: model={}, messages={}", model, messages);
        try {
            String result = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::extractContent)
                    .block();
            log.debug("DeepSeek response length: {}", result != null ? result.length() : 0);
            return result != null ? result : "";
        } catch (Exception e) {
            log.error("DeepSeek call failed: {}", e.getMessage(), e);
            return "";
        }
    }

    private String extractDeltaContent(String line) {
        try {
            String json = line.startsWith("data: ") ? line.substring(6) : line;
            return objectMapper.readTree(json)
                    .path("choices").path(0).path("delta").path("content")
                    .asText();
        } catch (Exception e) {
            return "";
        }
    }

    private String extractContent(String response) {
        try {
            return objectMapper.readTree(response)
                    .path("choices").path(0).path("message").path("content")
                    .asText();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public ModelType getType() {
        return ModelType.DEEPSEEK;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("${");
    }
}
