# Proposal: mindbridge-chat-engine

## Summary

Build MindBridge's core chat engine: a Spring AI-powered psychological counseling assistant that combines multi-model LLM dialogue with RAG knowledge retrieval, streaming responses via Server-Sent Events, and persistent session management.

## Problem

Psychological counselors need an AI assistant that can:
1. Conduct streaming conversations with students using local/remote LLMs
2. Retrieve relevant counseling knowledge from uploaded documents during conversations
3. Maintain conversation context across sessions for continuity

Without this core engine, higher-level features (risk detection, Excel export, LoRA fine-tuning) have no foundation.

## Scope (Phase 1)

**In scope:**
- Maven multi-module Spring Boot project
- Multi-model strategy: Ollama qwen3 (primary) + DeepSeek V4 (reserved)
- Streaming chat API via WebFlux/SSE
- Session CRUD + message history persistence (MySQL)
- RAG knowledge base: document upload, Chroma vectorization, semantic search
- Docker Compose for Chroma

**Out of scope:**
- Spring Security authentication/authorization
- LoRA model fine-tuning
- MCP Excel export + email alerting
- React frontend

## Stakeholders

- Developers building on this platform
- Future: psychological counselors and students

## Success Criteria

- [ ] `POST /api/chat/stream/{sessionId}` returns SSE stream from qwen3
- [ ] Knowledge documents uploaded → auto vectorized → retrievable in chat context
- [ ] Sessions persist with full message history across restarts
- [ ] DeepSeek provider is callable by switching config (no code change)
