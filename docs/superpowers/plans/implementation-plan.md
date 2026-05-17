# Implementation Plan: mindbridge-chat-engine

## Execution Order

### Step 1: Parent POM (`T1`)

Create `d:\MindBridge\pom.xml`:
- GroupId: `com.mindbridge`, ArtifactId: `mindbridge-parent`
- Packaging: `pom`
- Modules: common, model, rag, chat, app
- Spring Boot 3.5.x parent + Spring AI BOM
- Java 17, UTF-8

### Step 2: mindbridge-common (`T2`)

Generate the module skeleton and shared code:
- `pom.xml` with JPA + MySQL deps
- Entities: `Session`, `Message`, `KnowledgeDocument` (JPA-annotated)
- DTOs: `ChatRequest`, `ChatResponse`, `SessionCreateRequest`
- Enums: `MessageRole`, `ModelType`
- `MindBridgeException` extends `RuntimeException`
- Spring Data JPA repositories for each entity

### Step 3: mindbridge-model (`T3`)

- `pom.xml` depends on common + Spring AI Ollama starter
- `ModelProvider` interface
- `OllamaProvider` using `OllamaChatModel`
- `DeepSeekProvider` using Spring's `RestClient` + conditional on API key
- `ModelRouter` with fallback logic

### Step 4: mindbridge-rag (`T4`)

- `pom.xml` depends on common + Spring AI Chroma vector store
- `ChromaConfig` connects to Docker-hosted Chroma
- `RagService` with document chunking + embedding + search
- `KnowledgeController` REST endpoints

### Step 5: mindbridge-chat (`T5`)

- `pom.xml` depends on common + model + rag + WebFlux
- `ChatService` orchestrates: context load → RAG search → model stream → persist
- `ChatController` SSE streaming endpoint
- `SessionController` session CRUD + message history

### Step 6: mindbridge-app (`T6`)

- `pom.xml` depends on all modules + Spring Boot starter
- `MindBridgeApplication` main class
- `application.yml` full config
- `docker-compose.yml` for Chroma

### Step 7: Environment Setup (`T7-T9`)

- Docker: `compose up -d` → verify Chroma
- Ollama: locate binary → `ollama serve` → `ollama pull qwen3`
- MySQL: verify running → create `mindbridge` schema

### Step 8: Build & Verify (`T10-T11`)

- `mvn clean package` from parent POM
- Start app → test all endpoints via curl/httpie
- Smoke test: session → stream → knowledge upload → verify

## Testing Strategy

Each module tested in isolation before integration:
- **common**: No tests needed (pure POJOs)
- **model**: Unit test `ModelRouter.fallback` logic; integration test OllamaProvider (requires running Ollama)
- **rag**: Integration test vs Chroma Docker container
- **chat**: `@WebFluxTest` for SSE controller; integration test for full flow

## Risk Items

| Risk | Mitigation |
|------|----------|
| Ollama not found | Locate binary first, configure path explicitly |
| Chroma Docker pull fails | Use `chromadb/chroma:0.5.x` as fallback |
| Spring AI version mismatch | Pin to BOM version, avoid snapshot deps |
| MySQL connection refused | Check service status, verify credentials |
