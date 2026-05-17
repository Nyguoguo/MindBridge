package com.mindbridge.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public RagService(
            @Autowired(required = false) VectorStore vectorStore,
            @Autowired(required = false) EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    public boolean isReady() {
        return vectorStore != null && embeddingModel != null;
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        if (text.length() <= chunkSize) {
            return List.of(text);
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += (chunkSize - overlap);
            if (start >= text.length()) {
                break;
            }
        }
        return chunks;
    }

    public void ingest(String docId, String content) {
        if (!isReady()) {
            log.warn("RAG not ready, skipping ingest for docId={}", docId);
            return;
        }
        List<String> chunks = chunkText(content, 500, 50);
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            documents.add(new Document(chunks.get(i),
                    java.util.Map.of("docId", docId, "chunkIndex", String.valueOf(i))));
        }
        vectorStore.add(documents);
    }

    public List<Document> search(String query, int topK) {
        if (!isReady()) {
            log.debug("RAG not ready, returning empty search results");
            return Collections.emptyList();
        }
        try {
            return vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(topK).build());
        } catch (Exception e) {
            log.warn("RAG search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void deleteDocuments(String docId) {
        if (!isReady()) {
            return;
        }
        vectorStore.delete("docId == \"" + docId + "\"");
    }
}
