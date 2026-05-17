# Design: mindbridge-chat-engine

## Architecture Overview

```
┌──────────────────────────────────────────────────┐
│                  mindbridge-app                   │
│              (Spring Boot Launcher)               │
├──────────────────────────────────────────────────┤
│  mindbridge-chat                                  │
│  ┌──────────────┐  ┌──────────────────────────┐  │
│  │ ChatController│  │     ChatService          │  │
│  │ (REST + SSE) │  │  session mgmt + orchestrate │
│  └──────────────┘  └──────────────────────────┘  │
├───────────────┬──────────────────────────────────┤
│ mindbridge-rag│  mindbridge-model                │
│ ┌───────────┐ │  ┌────────────┐ ┌────────────┐  │
│ │RagService │ │  │ModelRouter │ │OllamaProv  │  │
│ │+ Chroma   │ │  │.route(name)│ │DeepSeekProv│  │
│ └───────────┘ │  └────────────┘ └────────────┘  │
├───────────────┴──────────────────────────────────┤
│              mindbridge-common                    │
│       entities / DTOs / enums / utils            │
└──────────────────────────────────────────────────┘
                  │          │
           MySQL  │    Chroma│ (Docker)
                  │          │
```

## Module Details

### mindbridge-common

Shared artifacts used by all modules:

```
com.mindbridge.common
├── entity/
│   ├── Session.java
│   ├── Message.java
│   └── KnowledgeDocument.java
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── SessionCreateRequest.java
│   └── UploadRequest.java
├── enums/
│   ├── MessageRole.java   (USER, ASSISTANT, SYSTEM)
│   └── ModelType.java      (OLLAMA, DEEPSEEK)
└── exception/
    └── MindBridgeException.java
```

### mindbridge-model

Multi-model strategy pattern:

```java
public interface ModelProvider {
    Flux<String> stream(Prompt prompt);
    String call(Prompt prompt);
    ModelType getType();
    boolean isAvailable();
}

@Service
public class ModelRouter {
    private final Map<ModelType, ModelProvider> providers;

    public ModelProvider select(ModelType type) { ... }
    public ModelProvider getDefault() { ... } // Ollama
}
```

Spring AI configuration:
- `OllamaProvider` → `spring.ai.ollama.*` (qwen3, localhost:11434)
- `DeepSeekProvider` → custom REST client to DeepSeek V4 API
- `ModelRouter` → facade, selects provider by type

### mindbridge-rag

Chroma integration via Spring AI:

```java
@Service
public class RagService {
    private final ChromaVectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public void ingest(String docId, String content) { ... }
    public List<Document> search(String query, int topK) { ... }
    public void delete(String docId) { ... }
}
```

- Chroma runs via Docker on `localhost:8000`
- Collection: `mindbridge_knowledge`
- Before chat, search Chroma for top-3 similar chunks
- Inject as system prompt context

### mindbridge-chat

Core orchestration:

```java
@RestController
@RequestMapping("/api")
public class ChatController {

    @PostMapping(value = "/chat/stream/{sessionId}",
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @PathVariable Long sessionId,
            @RequestBody ChatRequest request) { ... }
}
```

Data flow:
1. Resolve session → load recent messages as context
2. `RagService.search(query)` → top-3 relevant knowledge chunks
3. `ModelRouter.select(modelType).stream(prompt)` → SSE Flux
4. Save user message + assistant response to MySQL

## Database (MySQL)

```sql
CREATE TABLE sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL DEFAULT 1,
  title VARCHAR(255),
  model VARCHAR(50) DEFAULT 'qwen3',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  role ENUM('USER', 'ASSISTANT', 'SYSTEM') NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (session_id) REFERENCES sessions(id)
);

CREATE TABLE knowledge_documents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  content TEXT,
  file_type VARCHAR(50),
  vectorized TINYINT DEFAULT 0,
  chunk_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## Docker Compose (Chroma)

```yaml
# docker-compose.yml at project root
services:
  chroma:
    image: chromadb/chroma:latest
    ports:
      - "8000:8000"
    volumes:
      - chroma_data:/chroma/data
```

## API Contract

### Streaming Chat
```
POST /api/chat/stream/{sessionId}
Content-Type: application/json
{
  "content": "最近总是失眠，心情很差",
  "model": "qwen3"   // optional, defaults to qwen3
}

Response: text/event-stream
data: {"token": "失眠", "done": false}
data: {"token": "是", "done": false}
...
data: {"token": "", "done": true, "messageId": 42}
```

### Sessions
```
GET    /api/sessions                    → List sessions
POST   /api/sessions                    → Create session
DELETE /api/sessions/{id}               → Delete session
GET    /api/sessions/{id}/messages      → Get messages
```

### Knowledge
```
POST   /api/knowledge/upload            → Upload document (text/file)
GET    /api/knowledge/list              → List documents
DELETE /api/knowledge/{id}              → Remove document
```

## Config (application.yml)

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: qwen3
        options:
          temperature: 0.7
    vectorstore:
      chroma:
        collection-name: mindbridge_knowledge
        url: http://localhost:8000

mindbridge:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:}
    base-url: https://api.deepseek.com/v1
    model: deepseek-chat
```
