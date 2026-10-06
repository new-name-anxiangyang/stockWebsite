import logging

from fastapi import FastAPI, Header
from fastapi.middleware.cors import CORSMiddleware

from app.service import agent_service
from app.config import settings
from app.config.schemas import ChatRequest, ChatResponse

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)


app = FastAPI(
    title="Stock Agent Service",
    version="0.2.0",
)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/agent/health")
async def health():
    return {
        "service": settings.app_name,
        "status": "UP",
        "mode": "langchain" if agent_service.model else "mock",
    }


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

    return ChatResponse(
        answer=answer,
        conversation_id=conversation_id,
        mode=mode,
    )