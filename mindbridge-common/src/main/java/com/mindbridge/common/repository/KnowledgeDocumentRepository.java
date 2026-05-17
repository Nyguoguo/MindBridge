package com.mindbridge.common.repository;

import com.mindbridge.common.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findAllByOrderByCreatedAtDesc();

    List<KnowledgeDocument> findByVectorizedTrue();
}
