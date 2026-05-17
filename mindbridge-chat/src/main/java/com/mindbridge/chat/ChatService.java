package com.mindbridge.chat;

import com.mindbridge.common.entity.Message;
import com.mindbridge.common.entity.Session;
import com.mindbridge.common.enums.MessageRole;
import com.mindbridge.common.enums.ModelType;
import com.mindbridge.common.event.ChatCompletedEvent;
import com.mindbridge.common.exception.MindBridgeException;
import com.mindbridge.common.repository.MessageRepository;
import com.mindbridge.common.repository.SessionRepository;
import com.mindbridge.model.ModelRouter;
import com.mindbridge.rag.RagService;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final SessionRepository sessionRepo;
    private final MessageRepository messageRepo;
    private final ModelRouter modelRouter;
    private final RagService ragService;
    private final ApplicationEventPublisher eventPublisher;

    public ChatService(SessionRepository sessionRepo, MessageRepository messageRepo,
                       ModelRouter modelRouter, RagService ragService,
                       ApplicationEventPublisher eventPublisher) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.modelRouter = modelRouter;
        this.ragService = ragService;
        this.eventPublisher = eventPublisher;
    }

    public Flux<String> streamChat(Long sessionId, String userContent, String modelName) {
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new MindBridgeException("Session not found: " + sessionId));

        ModelType modelType = modelName != null && modelName.equalsIgnoreCase("deepseek")
                ? ModelType.DEEPSEEK : ModelType.OLLAMA;

        List<Message> recentMessages = messageRepo.findTop10BySessionIdOrderByCreatedAtDesc(sessionId);
        Collections.reverse(recentMessages);

        String context = buildContext(recentMessages);
        List<Document> ragDocs = ragService.search(userContent, 3);
        String ragContext = ragDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        if (!ragContext.isEmpty()) {
            context += "\n参考以下知识:\n" + ragContext;
        }

        if (!context.isEmpty()) {
            context = "以下是之前的对话上下文:\n" + context + "\n用户当前消息: ";
        }

        String finalContext = context;
        Prompt prompt = new Prompt(finalContext + userContent);

        StringBuilder fullResponse = new StringBuilder();
        return modelRouter.select(modelType).stream(prompt)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    Message userMsg = new Message();
                    userMsg.setSessionId(sessionId);
                    userMsg.setRole(MessageRole.USER);
                    userMsg.setContent(userContent);
                    messageRepo.save(userMsg);

                    Message assistantMsg = new Message();
                    assistantMsg.setSessionId(sessionId);
                    assistantMsg.setRole(MessageRole.ASSISTANT);
                    assistantMsg.setContent(fullResponse.toString());
                    messageRepo.save(assistantMsg);

                    eventPublisher.publishEvent(
                            new ChatCompletedEvent(sessionId, session.getUserId(),
                                    userContent, fullResponse.toString()));
                });
    }

    private String buildContext(List<Message> messages) {
        return messages.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }
}
