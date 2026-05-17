package com.mindbridge.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class SimpleEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSION = 384;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(computeEmbedding(text), 0));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return computeEmbedding(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSION;
    }

    private float[] computeEmbedding(String text) {
        float[] vector = new float[DIMENSION];
        if (text != null && !text.isEmpty()) {
            for (int i = 0; i < DIMENSION; i++) {
                long hash = ((long) text.hashCode() * (i + 1) * 2654435761L);
                vector[i] = (float) Math.sin(hash * 0.001) * 0.5f + (float) (hash % 100) / 100f;
            }
        }
        return vector;
    }
}
