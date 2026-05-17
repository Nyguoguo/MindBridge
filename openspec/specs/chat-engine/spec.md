# Spec: mindbridge-chat-engine

## Spec 1: Multi-Model Strategy

### 1.1 ModelProvider Interface
- **WHEN** a model type (OLLAMA, DEEPSEEK) is selected
- **THEN** the corresponding ModelProvider implementation handles the request
- Each provider implements `Flux<String> stream(Prompt)` and `String call(Prompt)`

### 1.2 Ollama Provider
- **WHEN** Spring Boot starts
- **AND** `spring.ai.ollama.base-url` points to a running Ollama instance
- **THEN** OllamaProvider reports `isAvailable() == true`
- Calling `stream(prompt)` returns a Flux of tokens from qwen3

### 1.3 DeepSeek Provider (Reserved)
- **WHEN** `DEEPSEEK_API_KEY` environment variable is set
- **THEN** DeepSeekProvider reports `isAvailable() == true`
- **WHEN** `DEEPSEEK_API_KEY` is not set
- **THEN** DeepSeekProvider reports `isAvailable() == false` without throwing

### 1.4 ModelRouter
- **WHEN** `select(DEEPSEEK)` is called but DeepSeek is unavailable
- **THEN** fallback to OllamaProvider (graceful degradation)
- **WHEN** no model is specified
- **THEN** defaults to Ollama qwen3

## Spec 2: Streaming Chat API

### 2.1 Stream Endpoint
- **WHEN** `POST /api/chat/stream/{sessionId}` is called with `{"content": "..."}`
- **THEN** response is `text/event-stream` (SSE)
- Each event contains a JSON `{"token": "...", "done": false}`
- Final event: `{"token": "", "done": true, "messageId": N}`

### 2.2 Session Context
- **WHEN** a chat request is received
- **THEN** the last 10 messages from that session are loaded as conversation context
- Context includes RAG retrieval results appended as system prompt
- The user message and full assistant response are persisted after streaming completes

### 2.3 Error Handling
- **WHEN** Ollama is unreachable
- **THEN** return HTTP 503 with `{"error": "Model service unavailable"}`
- **WHEN** session does not exist
- **THEN** return HTTP 404

## Spec 3: Session Management

### 3.1 Create Session
- **POST /api/sessions** with `{"title": "...", "model": "qwen3"}`
- Returns created session with ID

### 3.2 List Sessions
- **GET /api/sessions** returns all sessions ordered by `updated_at DESC`

### 3.3 Delete Session
- **DELETE /api/sessions/{id}** deletes session and all its messages

### 3.4 Get Messages
- **GET /api/sessions/{id}/messages** returns all messages in chronological order

## Spec 4: RAG Knowledge Base

### 4.1 Document Upload
- **POST /api/knowledge/upload** accepts text content
- Document saved to MySQL → chunked → embedded → stored in Chroma
- Returns document with `vectorized=true` and `chunk_count`

### 4.2 Semantic Search
- **WHEN** a chat message is received
- **THEN** `RagService.search(query, topK=3)` retrieves most relevant chunks from Chroma
- Results injected as system prompt: "参考以下知识: {chunks}"

### 4.3 List Documents
- **GET /api/knowledge/list** returns all documents ordered by `created_at DESC`

### 4.4 Delete Document
- **DELETE /api/knowledge/{id}` removes from MySQL and Chroma

## Spec 5: Infrastructure

### 5.1 Chroma
- Docker container `chromadb/chroma:latest`, port 8000
- Collection auto-created on first use

### 5.2 MySQL
- Spring Data JPA with `ddl-auto: update`
- Connection pool: HikariCP (default)

### 5.3 Docker Compose
- `docker-compose.yml` at project root
- Single `docker compose up -d` starts Chroma
