package com.mindbridge.common.repository;

import com.mindbridge.common.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<Message> findTop10BySessionIdOrderByCreatedAtDesc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
