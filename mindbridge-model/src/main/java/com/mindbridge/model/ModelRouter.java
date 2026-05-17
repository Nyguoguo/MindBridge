package com.mindbridge.model;

import com.mindbridge.common.enums.ModelType;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ModelRouter {

    private final Map<ModelType, ModelProvider> providers;
    private final ModelProvider defaultProvider;

    public ModelRouter(List<ModelProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(ModelProvider::getType, Function.identity()));
        ModelProvider ollama = providers.get(ModelType.OLLAMA);
        if (ollama != null && ollama.isAvailable()) {
            this.defaultProvider = ollama;
        } else {
            this.defaultProvider = providers.values().stream()
                    .filter(ModelProvider::isAvailable)
                    .findFirst()
                    .orElse(ollama);
        }
    }

    public ModelProvider select(ModelType type) {
        ModelProvider provider = providers.get(type);
        if (provider != null && provider.isAvailable()) {
            return provider;
        }
        if (defaultProvider != null && defaultProvider.isAvailable()) {
            return defaultProvider;
        }
        throw new com.mindbridge.common.exception.MindBridgeException("No model provider available");
    }

    public ModelProvider getDefault() {
        if (defaultProvider == null || !defaultProvider.isAvailable()) {
            throw new com.mindbridge.common.exception.MindBridgeException("No model provider available");
        }
        return defaultProvider;
    }
}
