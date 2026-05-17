package com.mindbridge.chat;

import com.mindbridge.common.dto.SessionCreateRequest;
import com.mindbridge.common.entity.Message;
import com.mindbridge.common.entity.Session;
import com.mindbridge.common.repository.MessageRepository;
import com.mindbridge.common.repository.SessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository sessionRepo;
    private final MessageRepository messageRepo;

    public SessionController(SessionRepository sessionRepo, MessageRepository messageRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
    }

    @GetMapping
    public ResponseEntity<List<Session>> list() {
        return ResponseEntity.ok(sessionRepo.findByUserIdOrderByUpdatedAtDesc(1L));
    }

    @PostMapping
    public ResponseEntity<Session> create(@RequestBody SessionCreateRequest request) {
        Session session = new Session();
        session.setTitle(request.getTitle());
        session.setModel(request.getModel() != null ? request.getModel() : "qwen3");
        return ResponseEntity.ok(sessionRepo.save(session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        messageRepo.deleteBySessionId(id);
        sessionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageRepo.findBySessionIdOrderByCreatedAtAsc(id));
    }
}
