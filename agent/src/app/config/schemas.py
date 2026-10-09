from typing import Any,Literal

from pydantic import BaseModel, Field

class ChatRequest(BaseModel):
    message: str = Field(
        ...,#表示必填项，没有默认值
        min_length=1,
        max_length=4000,
        description="用户发送的问题",
    )
    conversation_id: str | None = Field( #这个字段类型可以是 str，也可以是 None，后期可用于短期和长期记忆
        default=None,
        description="会话 ID",
    )

class ChatResponse(BaseModel):
    answer: str | None = None
    conversation_id: str
    mode: str
    status: Literal["completed", "pending_approval", "rejected"] = "completed"
    approval_request: dict[str, Any] | None = None

class ResumeRequest(BaseModel):
    conversation_id: str
    decision: Literal["approve", "edit", "reject", "respond"]
    message: str | None = None
    edited_action: dict[str, Any] | None = None