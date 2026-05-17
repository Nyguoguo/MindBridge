package com.mindbridge.chat;

import com.mindbridge.common.dto.ChatRequest;
import com.mindbridge.common.dto.SseEvent;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SseEvent>> stream(
            @PathVariable Long sessionId,
            @RequestBody ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "qwen3";
        return chatService.streamChat(sessionId, request.getContent(), model)
                .map(token -> ServerSentEvent.<SseEvent>builder()
                        .data(new SseEvent(token, false))
                        .build())
                .concatWith(Mono.just(
                        ServerSentEvent.<SseEvent>builder()
                                .data(new SseEvent("", true))
                                .build()));
    }

    @PostMapping("/send")
    public Mono<String> send(@RequestBody ChatRequest request) {
        return chatService.streamChat(1L, request.getContent(), request.getModel())
                .collectList()
                .map(tokens -> String.join("", tokens));
    }
}
