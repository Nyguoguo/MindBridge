package com.mindbridge.chat;

import com.mindbridge.common.entity.Message;
import com.mindbridge.common.entity.Session;
import com.mindbridge.common.enums.MessageRole;
import com.mindbridge.common.enums.ModelType;
import com.mindbridge.common.repository.MessageRepository;
import com.mindbridge.common.repository.SessionRepository;
import com.mindbridge.model.ModelProvider;
import com.mindbridge.model.ModelRouter;
import com.mindbridge.rag.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatService chatService;
    private SessionRepository sessionRepo;
    private MessageRepository messageRepo;
    private ModelRouter modelRouter;
    private ModelProvider ollamaProvider;
    private RagService ragService;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        sessionRepo = mock(SessionRepository.class);
        messageRepo = mock(MessageRepository.class);
        modelRouter = mock(ModelRouter.class);
        ollamaProvider = mock(ModelProvider.class);
        ragService = mock(RagService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        when(modelRouter.getDefault()).thenReturn(ollamaProvider);
        when(modelRouter.select(any())).thenReturn(ollamaProvider);
        when(ollamaProvider.getType()).thenReturn(ModelType.OLLAMA);

        chatService = new ChatService(sessionRepo, messageRepo, modelRouter, ragService, eventPublisher);
    }

    @Test
    void shouldSaveUserMessageAndAssistantResponse() {
        Session session = new Session();
        session.setId(1L);
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(messageRepo.findTop10BySessionIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(ragService.search(anyString(), anyInt())).thenReturn(List.of());
        when(ollamaProvider.stream(any()))
                .thenReturn(Flux.just("Hello", " world"));
        when(messageRepo.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        Flux<String> result = chatService.streamChat(1L, "你好", "qwen3");

        StepVerifier.create(result)
                .expectNext("Hello")
                .expectNext(" world")
                .verifyComplete();

        verify(messageRepo, times(2)).save(any(Message.class));
        verify(modelRouter).select(ModelType.OLLAMA);
    }
}
