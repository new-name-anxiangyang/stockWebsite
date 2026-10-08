# Stock Agent Service

基于 FastAPI 和 LangChain 构建的股票智能 Agent 服务，为股票行情网站提供自然语言问答能力。

当前 Agent 支持：

- 通过大模型回答股票、行情和市场信息问题；
- 调用 Java 股票服务查询股票历史行情；
- 使用 Tavily 搜索最新新闻和市场资讯；
- 通过 `conversation_id` 标识会话；
- 将登录请求中的 JWT 透传给股票服务；
- 在未配置大模型密钥时自动降级为 Mock 模式。

> 当前版本尚未实现聊天记录持久化、RAG 知识库和 MCP Server。相关能力可以在后续版本中扩展。

## 1. 系统架构

```text
浏览器
   │
   ▼
Nginx /api/agent/**
   │
   ▼
FastAPI Agent Service :8000
   │
   ├── LangChain Agent
   ├── OpenAI-compatible LLM
   ├── Tavily 新闻搜索
   └── query_stock 工具
           │
           ▼
      Java Gateway :8082
           │
           ▼
      stock-service
```

Agent 调用股票服务时会携带前端传入的 `Authorization` 请求头，因此股票服务仍然通过 Gateway 统一进行 JWT 鉴权。

## 2. 技术栈

- Python 3.13+
- FastAPI
- Uvicorn
- Pydantic Settings
- LangChain
- LangChain OpenAI
- LangChain Tavily
- httpx

## 3. 目录结构

```text
agent/
├── main.py                         # 示例入口
├── pyproject.toml                  # uv 项目配置
├── uv.lock                         # 依赖锁定文件
└── src/app/
    ├── main.py                     # FastAPI 应用入口
    ├── config/
    │   ├── settings.py             # 环境变量配置
    │   └── schemas.py              # 请求和响应模型
    └── service/
        └── agent_service.py        # Agent、工具和对话逻辑
```

## 4. 环境要求

- Python 3.13 或更高版本；
- Java Gateway 运行在 `127.0.0.1:8082`；
- 可选：OpenAI-compatible LLM API Key；
- 可选：Tavily API Key。

## 5. 安装依赖

使用 `uv`：

```powershell
uv sync
```

也可以使用 pip：

```powershell
pip install -r src/app/requirements.txt
```

## 6. 配置环境变量

在 `agent/.env` 中配置：

```env
APP_NAME=agent-service
APP_HOST=0.0.0.0
APP_PORT=8000

# OpenAI-compatible 模型配置
LLM_API_KEY=你的api密钥
LLM_BASE_URL=https://api.deepseek.com
LLM_MODEL=deepseek-chat

# Tavily 新闻搜索，可选
TAVILY_API_KEY=your-tavily-api-key
```

配置项说明：

| 配置项 | 必填 | 说明 |
|---|---|---|
| `APP_NAME` | 否 | 服务名称，默认 `agent-service` |
| `APP_HOST` | 否 | 监听地址，默认 `0.0.0.0` |
| `APP_PORT` | 否 | 监听端口，默认 `8000` |
| `LLM_API_KEY` | 否 | 大模型 API Key，不配置时使用 Mock 模式 |
| `LLM_BASE_URL` | 否 | OpenAI-compatible API 地址 |
| `LLM_MODEL` | 否 | 模型名称，默认 `deepseek-chat` |
| `TAVILY_API_KEY` | 否 | Tavily 搜索 API Key |

不要将 `.env`、API Key 或其他密钥提交到 Git。

## 7. 启动服务

在 `agent` 目录执行：

```powershell
uv run uvicorn app.main:app --app-dir src --host 0.0.0.0 --port 8000 --reload
```

如果不使用 `uv`：

```powershell
uvicorn app.main:app --app-dir src --host 0.0.0.0 --port 8000 --reload
```

服务启动后：

- Swagger 文档：`http://localhost:8000/docs`
- 健康检查：`http://localhost:8000/agent/health`

## 8. HTTP 接口

### 8.1 健康检查

```http
GET /agent/health
```

LangChain 模式：

```json
{
  "service": "agent-service",
  "status": "UP",
  "mode": "langchain"
}
```

未配置 `LLM_API_KEY` 时：

```json
{
  "service": "agent-service",
  "status": "UP",
  "mode": "mock"
}
```

### 8.2 Agent 对话

```http
POST /agent/chat
Authorization: Bearer <JWT>
Content-Type: application/json
```

请求体：

```json
{
  "message": "帮我查询 AAPL 最近的行情",
  "conversation_id": "optional-conversation-id"
}
```

响应体：

```json
{
  "answer": "……",
  "conversation_id": "generated-or-existing-id",
  "mode": "langchain"
}
```

字段说明：

- `message`：用户问题，长度为 1～4000 个字符；
- `conversation_id`：可选的会话标识。未传入时服务会生成 UUID；
- `answer`：Agent 最终回答；
- `mode`：当前回答模式，可能为 `langchain` 或 `mock`。

## 9. Agent 工具

### `query_stock`

用于查询股票历史行情。

调用流程：

```text
LangChain Agent
    ↓
query_stock(symbol)
    ↓
GET http://127.0.0.1:8082/stocks/{symbol}
    ↓
Java stock-service
```

股票代码支持 1～12 位大写字母、数字、点号和短横线，例如：

```text
AAPL
TSLA
BRK.B
```

工具会处理以下情况：

- 股票代码格式错误；
- 股票服务返回 401；
- 股票服务返回其他错误状态；
- 股票服务没有返回数据；
- 股票服务网络连接失败。

### Tavily 搜索工具

当配置 `TAVILY_API_KEY` 后，Agent 可以使用 Tavily 查询：

- 最新公司新闻；
- 市场动态；
- 政策变化；
- 行业资讯。

如果没有配置 Tavily Key，新闻搜索工具不会加入 Agent 的工具列表。

## 10. Mock 模式

当没有配置 `LLM_API_KEY`，或者大模型初始化失败时，服务会自动进入 Mock 模式。

Mock 模式的作用是：

- 验证 FastAPI 接口是否正常；
- 验证前端请求和响应格式；
- 在没有大模型 Key 的开发环境中完成联调。

Mock 模式不会真正调用大模型，也不会执行股票查询和新闻搜索工具。

## 11. 与前端和 Java 服务联调

前端通过 Nginx 调用：

```text
POST /api/agent/chat
```

Nginx 将请求转发到：

```text
http://127.0.0.1:8000/agent/chat
```

完整链路：

```text
浏览器
  → Nginx /api/agent/chat
  → FastAPI Agent Service /agent/chat
  → Java Gateway /stocks/{symbol}
  → stock-service
```

调用股票工具时需要前端传入有效的：

```http
Authorization: Bearer <JWT>
```

## 12. 测试示例

健康检查：

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8000/agent/health"
```

Agent 对话：

```powershell
$body = @{
    message = "帮我分析一下 AAPL 最近的表现"
    conversation_id = $null
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8000/agent/chat" `
    -Headers @{ Authorization = "Bearer $token" } `
    -ContentType "application/json" `
    -Body $body
```

## 13. 当前限制

- `conversation_id` 目前只用于标识会话，聊天记录尚未持久化；
- Agent 每次调用只接收当前用户消息，暂未加载历史消息；
- 尚未接入 RAG 向量知识库；
- 尚未提供 MCP Server；
- CORS 当前允许所有来源，仅适合开发环境；
- Agent 服务本身没有独立的用户数据库，用户身份依赖 Java 认证服务签发的 JWT。

## 14. 后续规划

### 持久化记忆

- 使用 MySQL 或 PostgreSQL 保存会话和消息；
- 使用 Redis 缓存最近对话；
- 增加会话摘要和用户长期记忆；
- 按 `user_id` 隔离不同用户的会话数据。

### RAG 知识库

- 接入 Qdrant 或 pgvector；
- 支持上传财报、公告和行业研究报告；
- 对文档进行切分、向量化和检索；
- 在回答中返回资料来源。

### MCP 工具协议

- 使用 MCP Python SDK 暴露股票查询工具；
- 将新闻搜索和知识库检索封装为 MCP 工具；
- 统一工具的鉴权、错误处理和调用日志。

## 15. 安全建议

- 不要提交 `.env` 和任何 API Key；
- 生产环境关闭 `--reload`；
- 将 CORS 从 `*` 改为前端实际域名；
- 限制 Agent 可访问的内部服务地址；
- 对用户输入、工具参数和外部搜索结果进行校验；
- 对投资相关回答增加风险提示，不输出确定性投资承诺。
