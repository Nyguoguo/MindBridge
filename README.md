# MindBridge

Spring AI 驱动的智能心理辅导平台，集成多模型对话、RAG 知识检索、风险预警和 LoRA 微调。

## 架构

```
MindBridge/
├── mindbridge-common/    # 共享实体、DTO、仓库
├── mindbridge-model/     # 多模型策略 (DeepSeek / Ollama)
├── mindbridge-rag/       # RAG 知识库 + 向量嵌入
├── mindbridge-chat/      # Flux SSE 流式对话
├── mindbridge-mcp/       # 风险检测 + Excel + 邮件
├── mindbridge-app/       # Spring Boot 启动模块
├── lora-training/        # LoRA 微调脚本
├── frontend/             # React 前端
└── docker-compose.yml    # Chroma 向量数据库
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5 + Spring AI 1.1 |
| 模型 | DeepSeek V4 / Ollama qwen3 |
| 流式 | Spring WebFlux SSE |
| 向量库 | Chroma (Docker) + SimpleVectorStore |
| 数据库 | MySQL 8.0 |
| 安全 | Spring Security + JWT |
| 前端 | React + Vite |
| 微调 | LoRA (PEFT + Unsloth) |

## 快速启动

### 环境要求

- Java 17+
- Maven 3.9+
- MySQL 8.0
- Docker Desktop
- Node.js 24+

### 1. 启动基础设施

```bash
# MySQL（确保已运行，创建 mindbridge 数据库）
# Docker Chroma
docker compose up -d
```

### 2. 启动后端

```bash
mvn clean package -DskipTests
java -jar mindbridge-app/target/mindbridge-app-0.1.0.jar
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`

### 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 聊天 + 知识库管理 + 数据导出 |
| 学生 | `student` | `student123` | 聊天 |

## 核心功能

- **流式对话**：SSE 实时分词输出，支持 DeepSeek/Ollama 切换
- **RAG 检索**：知识文档上传 → 向量嵌入 → 对话自动检索
- **风险检测**：自动识别心理危机关键词，记录评估等级
- **Excel 导出**：一键导出心理评估数据
- **邮件预警**：HIGH 风险自动触发邮件通知
- **权限隔离**：管理员/用户角色严格分离

## LoRA 微调

```bash
cd lora-training
pip install -r requirements.txt
python generate_data.py    # 生成训练数据
python train.py --epochs 3 # 开始训练（需 GPU 16GB+）
python evaluate.py         # 评估准确率
```
