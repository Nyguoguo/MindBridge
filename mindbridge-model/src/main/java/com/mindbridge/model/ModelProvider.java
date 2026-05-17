package com.mindbridge.model;

import com.mindbridge.common.enums.ModelType;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface ModelProvider {

    Flux<String> stream(Prompt prompt);

    String call(Prompt prompt);

    ModelType getType();

    boolean isAvailable();
}
