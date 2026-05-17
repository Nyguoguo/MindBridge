package com.mindbridge.rag;

import com.mindbridge.common.dto.KnowledgeUploadRequest;
import com.mindbridge.common.entity.KnowledgeDocument;
import com.mindbridge.common.repository.KnowledgeDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeDocumentRepository docRepo;
    private final RagService ragService;

    public KnowledgeController(KnowledgeDocumentRepository docRepo, RagService ragService) {
        this.docRepo = docRepo;
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public ResponseEntity<KnowledgeDocument> upload(@RequestBody KnowledgeUploadRequest request) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent());
        doc.setFileType(request.getFileType());
        doc = docRepo.save(doc);

        if (ragService.isReady()) {
            ragService.ingest(doc.getId().toString(), request.getContent());
            doc.setVectorized(true);
            doc.setChunkCount(ragService.chunkText(request.getContent(), 500, 50).size());
        }
        doc = docRepo.save(doc);

        return ResponseEntity.ok(doc);
    }

    @GetMapping("/list")
    public ResponseEntity<List<KnowledgeDocument>> list() {
        return ResponseEntity.ok(docRepo.findAllByOrderByCreatedAtDesc());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        docRepo.findById(id).ifPresent(doc -> {
            ragService.deleteDocuments(doc.getId().toString());
            docRepo.delete(doc);
        });
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
