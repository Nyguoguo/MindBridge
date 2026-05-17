# MindBridge Design

## System Architecture

### Maven Multi-Module Structure

```
d:\MindBridge/
├── mindbridge-common/       # Shared entities, DTOs, utils
├── mindbridge-model/        # Multi-model strategy (Ollama/DeepSeek)
├── mindbridge-chat/         # Streaming chat + session management
├── mindbridge-rag/          # RAG knowledge base + Chroma
├── mindbridge-app/          # Spring Boot launcher
├── docs/
│   └── superpowers/
│       ├── specs/           # Design specs
│       └── plans/           # Implementation plans
├── pom.xml                  # Parent POM
```

### Module Dependencies

```
mindbridge-app
  ├── mindbridge-chat
  │     ├── mindbridge-model
  │     └── mindbridge-rag
  ├── mindbridge-model
  │     └── mindbridge-common
  └── mindbridge-rag
        └── mindbridge-common
```

### Tech Stack (Phase 1)

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x + Spring AI |
| Models | Ollama qwen3, DeepSeek V4 API |
| Streaming | Spring WebFlux (SSE via Flux) |
| Vector DB | Chroma (Docker) |
| Relational DB | MySQL 8.0 (local) |
| ORM | Spring Data JPA |
| Build | Maven |

### API Design

**Chat endpoints:**
- `POST /api/chat/stream/{sessionId}` — Streaming chat (SSE, Flux)
- `POST /api/chat/send` — Non-streaming chat
- `GET /api/sessions` — List sessions
- `POST /api/sessions` — Create session
- `DELETE /api/sessions/{id}` — Delete session
- `GET /api/sessions/{id}/messages` — Get messages in session

**Knowledge base endpoints:**
- `POST /api/knowledge/upload` — Upload documents
- `GET /api/knowledge/list` — List knowledge items
- `DELETE /api/knowledge/{id}` — Remove knowledge item

### Model Strategy Pattern

```
ModelProvider (interface)
  ├── OllamaProvider     → Ollama qwen3 (local)
  └── DeepSeekProvider   → DeepSeek V4 API (remote)

ModelRouter
  ├── selectProvider(modelName)
  └── default: qwen3 (Ollama)
```

### Data Flow (Streaming Chat)

```
User → React UI → POST /api/chat/stream/{sessionId}
  → ChatService
    → RagService.search(similar docs from Chroma)
    → ModelService.chat(query + context)
    → Flux<String> SSE stream back to client
  → Save to MySQL (session_id, messages)
```

### Database Schema (MySQL)

```sql
-- sessions
CREATE TABLE sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255),
  model VARCHAR(50) DEFAULT 'qwen3',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- messages
CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  role ENUM('user', 'assistant', 'system') NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (session_id) REFERENCES sessions(id)
);

-- knowledge_documents
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

### Chroma Collection

- Collection name: `mindbridge_knowledge`
- Embedding: Spring AI's built-in embedding model (Ollama)
- Chunk size: 500 tokens, overlap 50

### Phase 1 Scope

1. Maven multi-module project scaffolding
2. Common module: entities, DTOs
3. Model module: OllamaProvider + DeepSeekProvider (reserved)
4. RAG module: Chroma integration, document ingestion, similarity search
5. Chat module: Flux streaming API, session management
6. App module: Spring Boot assembly

**Out of scope for Phase 1:**
- Spring Security / RBAC
- LoRA fine-tuning
- MCP Excel + Email
- React frontend

### React Frontend (Separate Project, Phase 2)

```
d:\mindbridge-web/  (next to MindBridge)
├── src/
│   ├── components/     # Chat UI, Sidebar, MessageList
│   ├── pages/          # Home, Admin
│   ├── hooks/          # useSSEStream, useFetch
│   └── services/       # API client
├── package.json
```
