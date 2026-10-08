from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

#以 settings.py 所在位置向上两级，作为 BASE_DIR。Path(__file__):读取当前文件路径，.resolve()：解析成规范路径，.parents[2]返回src目录文件
BASE_DIR = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    app_name: str = "agent-service" #定义agent服务默认名称
    app_host: str = "0.0.0.0"#定义FastAPI 服务监听的 IP 地址
    app_port: int = 8000#定义端口

    llm_api_key: str = ""#调用大模型 API 的密钥，默认为空
    llm_base_url: str = "https://api.deepseek.com"#LLM API 的基础地址
    llm_model: str = "deepseek-chat"#使用哪个模型

    tavily_api_key: str = ""#Agent的Web Search / 网络搜索能力。

    redis_url: str = "redis://localhost:6379"

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",#路径拼接，避免硬编码
        env_file_encoding="utf-8",#.env 文件使用UTF-8 编码读取
        extra="ignore",#忽略Settings没定义的配置
    )


settings = Settings()#根据右边的Settings 定义生成一个完整的配置对象，并把它保存到 左边的settings 变量里，以后只需要注入左边的变量即可
