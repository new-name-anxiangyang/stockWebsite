from pathlib import Path

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

# agent_service 项目根目录，用于稳定定位 .env（不依赖当前工作目录）
BASE_DIR = Path(__file__).resolve().parents[4]


class Settings(BaseSettings):
    app_name: str = "agent-service"
    app_host: str = "0.0.0.0"
    app_port: int = 8000

    llm_api_key: str = Field(default="", validation_alias="DEEPSEEK_API_KEY")
    llm_base_url: str = "https://api.deepseek.com"
    llm_model: str = "deepseek-chat"

    tavily_api_key: str = ""

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
