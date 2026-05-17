package com.mindbridge.model;

import com.mindbridge.common.enums.ModelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRouterTest {

    private ModelRouter router;

    @BeforeEach
    void setUp() {
        DeepSeekProvider deepseek = new DeepSeekProvider(
                "sk-test-key", "https://api.deepseek.com", "deepseek-chat");
        router = new ModelRouter(List.of(deepseek));
    }

    @Test
    void shouldReturnDeepSeekAsDefaultWhenOnlyProvider() {
        ModelProvider provider = router.getDefault();

        assertThat(provider).isNotNull();
        assertThat(provider.getType()).isEqualTo(ModelType.DEEPSEEK);
    }

    @Test
    void shouldReturnDeepSeekWhenDeepSeekRequested() {
        ModelProvider provider = router.select(ModelType.DEEPSEEK);

        assertThat(provider).isNotNull();
        assertThat(provider.getType()).isEqualTo(ModelType.DEEPSEEK);
    }

    @Test
    void shouldFallbackToDefaultWhenOllamaRequestedButUnavailable() {
        ModelProvider provider = router.select(ModelType.OLLAMA);

        assertThat(provider).isNotNull();
        assertThat(provider.getType()).isEqualTo(ModelType.DEEPSEEK);
    }
}
