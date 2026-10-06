from __future__ import annotations

import json
import logging
import re
from uuid import uuid4

import httpx
from langchain_core.tools import tool

from app.config import settings

logger = logging.getLogger("agent_service")


class AgentService:
    def __init__(self) -> None:
        self.model = self._build_model()

        if self.model is not None:
            logger.info("LLM 模型初始化成功：%s", settings.llm_model)
        else:
            logger.warning("未配置 LLM_API_KEY，Agent 进入 Mock 模式")

    def _build_model(self):
        """
        创建大模型。

        没有配置 LLM_API_KEY 时返回 None，
        Agent 使用 Mock 模式运行。
        """
        if not settings.llm_api_key:
            return None

        try:
            from langchain_openai import ChatOpenAI

            return ChatOpenAI(
                api_key=settings.llm_api_key,
                base_url=settings.llm_base_url,
                model=settings.llm_model,
                temperature=0.2,
            )

        except Exception as exc:
            logger.error("大模型初始化失败，将使用 Mock 模式：%s", exc)
            return None

    def _build_tools(self, authorization: str | None):
        tools = []

        if authorization:
            tools.append(self._build_stock_tool(authorization))

        if settings.tavily_api_key:
            from langchain_tavily import TavilySearch

            tavily_tool = TavilySearch(
                max_results=5,
                topic="news",
                tavily_api_key=settings.tavily_api_key,
            )

            tools.append(tavily_tool)

        return tools

    @staticmethod
    def _build_stock_tool(authorization: str):
        @tool
        async def query_stock(symbol: str) -> str:
            """
            查询股票历史行情。

            参数：
            symbol: 股票代码，例如 AAPL、TSLA、MSFT。
            """

            symbol = symbol.strip().upper()

            if not re.fullmatch(r"[A-Z0-9.-]{1,12}", symbol):
                return "股票代码格式不正确。"

            url = f"http://127.0.0.1:8082/stocks/{symbol}"

            headers = {
                "Authorization": authorization,
            }

            try:
                async with httpx.AsyncClient(timeout=30) as client:
                    response = await client.get(
                        url,
                        headers=headers,
                    )

                if response.status_code == 401:
                    return "股票服务鉴权失败，请重新登录。"

                if response.status_code != 200:
                    return (
                        f"股票服务调用失败，状态码："
                        f"{response.status_code}"
                    )

                data = response.json()

                if not data:
                    return f"没有查询到 {symbol} 的行情数据。"

                latest_data = data[-10:]

                return json.dumps(
                    {
                        "symbol": symbol,
                        "count": len(data),
                        "latest": latest_data,
                    },
                    ensure_ascii=False,
                    default=str,
                )

            except httpx.RequestError as exc:
                return f"股票服务暂时不可用：{exc}"

        return query_stock

    def _build_agent(self, authorization: str | None):
        if self.model is None:
            return None

        from langchain.agents import create_agent

        tools = self._build_tools(authorization)

        system_prompt = """
你是股票网站中的智能分析助手。

你的职责：

1. 回答用户关于股票、行情和市场信息的问题；
2. 用户询问具体股票历史行情时，必须优先调用 query_stock 工具；
3. 用户询问最新新闻、公司动态、政策变化或实时市场信息时，必须调用 Tavily 搜索工具；
4. 不要把旧知识当成实时信息；
5. 搜索结果不足时，要明确告诉用户；
6. 回答实时信息时，尽量列出信息来源；
7. 不要编造股票数据、新闻或来源；
8. 对投资建议进行风险提示；
9. 回答要简洁、清晰，并尽量使用中文。

工具选择规则：

- “查询 AAPL 行情”使用 query_stock；
- “AAPL 最近有什么新闻”使用 Tavily 搜索；
- “最近新能源板块有什么消息”使用 Tavily 搜索；
- “什么是市盈率”可以直接回答；
- 同时涉及历史行情和实时新闻时，同时调用股票工具和 Tavily。
"""

        return create_agent(
            model=self.model,
            tools=tools,
            system_prompt=system_prompt,
        )

    async def chat(
        self,
        message: str,
        conversation_id: str | None = None,
        authorization: str | None = None,
    ) -> tuple[str, str, str]:
        conversation_id = conversation_id or str(uuid4())

        logger.info(
            "收到消息 conversation_id=%s length=%d has_auth=%s",
            conversation_id,
            len(message),
            bool(authorization),
        )

        agent = self._build_agent(authorization)

        if agent is None:
            logger.info("使用 Mock 模式回答 conversation_id=%s", conversation_id)
            answer = self._mock_answer(message)
            return answer, conversation_id, "mock"

        result = await agent.ainvoke(
            {
                "messages": [
                    {
                        "role": "user",
                        "content": message,
                    }
                ]
            }
        )

        answer = self._extract_answer(result)

        logger.info("LangChain 回答完成 conversation_id=%s", conversation_id)

        return answer, conversation_id, "langchain"

    @staticmethod
    def _extract_answer(result: dict) -> str:
        messages = result.get("messages", [])

        if not messages:
            return "Agent 没有返回有效内容。"

        last_message = messages[-1]
        content = getattr(last_message, "content", "")

        if isinstance(content, str):
            return content

        if isinstance(content, list):
            text_parts = []

            for item in content:
                if isinstance(item, dict):
                    if item.get("type") == "text":
                        text_parts.append(item.get("text", ""))

                elif isinstance(item, str):
                    text_parts.append(item)

            return "\n".join(text_parts).strip()

        return str(content)

    @staticmethod
    def _mock_answer(message: str) -> str:
        return (
            "当前 Agent 运行在 Mock 模式。\n\n"
            f"你发送的问题是：{message}\n\n"
            "请在 .env 中配置 LLM_API_KEY，"
            "启用真实 LangChain Agent。"
        )


agent_service = AgentService()