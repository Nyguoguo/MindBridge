# Tasks: mindbridge-chat-engine

## Phase 1: Project Scaffolding

### T1: Create Maven parent POM
- Create `pom.xml` with `<packaging>pom</packaging>`
- Define `<modules>`: common, model, chat, rag, app
- Set Spring Boot 3.5.x and Spring AI BOM versions
- Set Java 17

### T2: Create common module
- Create `mindbridge-common/pom.xml` (jar, no parent deps)
- Create Entity classes: `Session`, `Message`, `KnowledgeDocument`
- Create DTOs: `ChatRequest`, `ChatResponse`, `SessionCreateRequest`, `UploadRequest`
- Create Enums: `MessageRole`, `ModelType`
- Create exception class `MindBridgeException`
- Create JPA repositories

### T3: Create model module
- Create `mindbridge-model/pom.xml` (depends on common, spring-ai-ollama)
- Create `ModelProvider` interface
- Create `OllamaProvider` implementation
- Create `DeepSeekProvider` implementation (with availability check)
- Create `ModelRouter` service

### T4: Create rag module
- Create `mindbridge-rag/pom.xml` (depends on common, spring-ai-chroma)
- Create `RagService` with ingest/search/delete methods
- Create chunking logic (500 token chunks, 50 overlap)
- Create `KnowledgeController` (upload/list/delete endpoints)

### T5: Create chat module
- Create `mindbridge-chat/pom.xml` (depends on common, model, rag, webflux)
- Create `ChatService` (orchestrates RAG + Model + Session)
- Create `ChatController` (stream + send endpoints)
- Create `SessionController` (CRUD + messages)
- Create SSE event formatting logic

### T6: Create app module
- Create `mindbridge-app/pom.xml` (depends on all modules, spring-boot-starter)
- Create `MindBridgeApplication.java` main class
- Create `application.yml` with all config
- Create `docker-compose.yml` for Chroma

## Phase 2: Integration & Verification

### T7: Start Chroma
- Run `docker compose up -d`
- Verify Chroma accessible at http://localhost:8000

### T8: Start Ollama
- Locate and start Ollama
- Pull qwen3 model if not present
- Verify Ollama accessible at http://localhost:11434

### T9: Verify MySQL
- Ensure MySQL is running
- Create `mindbridge` database if not exists

### T10: Build and run
- `mvn clean package -f pom.xml`
- Start `mindbridge-app`
- Verify all endpoints respond correctly

### T11: Manual smoke test
- Create session → stream chat message → verify SSE tokens
- Upload knowledge doc → chat again → verify RAG injection
- Verify messages persisted in MySQL
