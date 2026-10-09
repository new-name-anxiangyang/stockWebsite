import asyncio

from langchain_mcp_adapters.client import MultiServerMCPClient


async def main():
    client = MultiServerMCPClient(
        {
            "time": {
                "command": "uvx",
                "args": [
                    "mcp-server-time",
                    "--local-timezone=Asia/Shanghai",
                ],
                "transport": "stdio",
            }
        },
        tool_name_prefix=True,
    )

    tools = await client.get_tools()

    for item in tools:
        print(item.name)
        print(item.description)
        print("---")


asyncio.run(main())