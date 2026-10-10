import json
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Header
from fastapi.middleware.cors import CORSMiddleware #允许跨域配置

from app.service import agent_service
from app.config import settings
from app.memory.redisCheckpoint import init_checkpointer
from app.config.schemas import (ChatRequest,ChatResponse,ResumeRequest,)
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_checkpointer()
    yield


app = FastAPI(
    title="Stock Agent Service",
    version="0.2.0",
    lifespan=lifespan,
)


def main() -> None:
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
    )


app.add_middleware( #添加 CORS 中间件
    CORSMiddleware,
    allow_origins=["*"], #允许所有前端
    allow_credentials=False, #不允许浏览器携带的cookie
    allow_methods=["*"], #允许所有的http方法
    allow_headers=["*"], #允许所有请求头
)


@app.get("/agent/health")
async def health():
    return {
        "service": settings.app_name,
        "status": "UP",
        "mode": "langchain" if agent_service.model else "mock",
    }


def _to_response(answer: str, conversation_id: str, mode: str) -> ChatResponse:
    """mode=pending_approval 时，把审核信息从 answer(JSON) 解析到结构化字段。"""
    if mode == "pending_approval":
        payload = json.loads(answer)
        return ChatResponse(
            conversation_id=conversation_id,
            mode=mode,
            status="pending_approval",
            approval_request=payload.get("approval_request"),
        )

    return ChatResponse(
        answer=answer,
        conversation_id=conversation_id,
        mode=mode,
    )


@app.post("/agent/chat", response_model=ChatResponse)
async def chat(
    request: ChatRequest,
    authorization: str | None = Header(default=None),
):
    answer, conversation_id, mode = await agent_service.chat(
        message=request.message,
        conversation_id=request.conversation_id,
        authorization=authorization,
    )

    return _to_response(answer, conversation_id, mode)

@app.post("/agent/chat/resume", response_model=ChatResponse)
async def resume_chat(
    request: ResumeRequest,
    authorization: str | None = Header(default=None),
):
    answer, conversation_id, mode = await agent_service.resume(
        conversation_id=request.conversation_id,
        decision=request.decision,
        message=request.message,
        edited_action=request.edited_action,
        authorization=authorization,
    )

    return _to_response(answer, conversation_id, mode)
