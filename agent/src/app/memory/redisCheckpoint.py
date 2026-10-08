from langgraph.checkpoint.redis.aio import AsyncRedisSaver

from app.config import settings

# 短期记忆检查点：按 thread_id 保存每个会话的消息状态。
# 必须使用异步版 AsyncRedisSaver，因为 Agent 通过 ainvoke 异步调用。
checkpointer = AsyncRedisSaver(settings.redis_url)


async def init_checkpointer() -> None:
    await checkpointer.asetup()
