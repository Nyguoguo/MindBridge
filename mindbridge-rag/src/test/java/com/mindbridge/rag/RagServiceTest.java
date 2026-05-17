package com.mindbridge.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagServiceTest {

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(null, null);
    }

    @Test
    void shouldSplitTextIntoChunks() {
        String text = "失眠是一种常见的睡眠障碍，表现为入睡困难、睡眠维持困难或早醒。" +
                "长期的失眠会导致注意力不集中、记忆力下降，甚至引发焦虑和抑郁。" +
                "对于大学生来说，失眠的主要原因包括学业压力、人际关系和就业焦虑。" +
                "认知行为疗法是治疗失眠最有效的心理干预方法之一。" +
                "规律作息、避免咖啡因、适度运动也有助于改善睡眠质量。";

        List<String> chunks = ragService.chunkText(text, 30, 5);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.size()).isGreaterThanOrEqualTo(3);
        // Each chunk should have some overlap with next
        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(text.length());
        }
    }

    @Test
    void shouldChunkShortTextIntoSingleChunk() {
        String text = "简短测试文本";

        List<String> chunks = ragService.chunkText(text, 100, 10);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(text);
    }

    @Test
    void shouldHandleEmptyText() {
        List<String> chunks = ragService.chunkText("", 30, 5);

        assertThat(chunks).isEmpty();
    }

    @Test
    void shouldHandleNullText() {
        List<String> chunks = ragService.chunkText(null, 30, 5);

        assertThat(chunks).isEmpty();
    }
}
