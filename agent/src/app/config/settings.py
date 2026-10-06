from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

# agent/src 目录，.env 所在位置
BASE_DIR = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    app_name: str = "agent-service"
    app_host: str = "0.0.0.0"
    app_port: int = 8000

    llm_api_key: str = ""
    llm_base_url: str = "https://api.deepseek.com"
    llm_model: str = "deepseek-chat"

    tavily_api_key: str = ""

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
