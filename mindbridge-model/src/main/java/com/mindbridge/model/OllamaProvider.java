package com.mindbridge.model;

import com.mindbridge.common.enums.ModelType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OllamaProvider implements ModelProvider {

    @Override
    public Flux<String> stream(Prompt prompt) {
        return Flux.empty();
    }

    @Override
    public String call(Prompt prompt) {
        return "";
    }

    @Override
    public ModelType getType() {
        return ModelType.OLLAMA;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
