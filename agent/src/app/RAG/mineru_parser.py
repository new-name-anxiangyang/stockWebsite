from pathlib import Path

from langchain_core.documents import Document
from mineru.parser import parse


SUPPORTED_SUFFIXES = {
    ".pdf",
    ".doc",
    ".docx",
    ".ppt",
    ".pptx",
    ".xls",
    ".xlsx",
    ".png",
    ".jpg",
    ".jpeg",
}


def parse_document(file_path: str | Path) -> list[Document]:
    path = Path(file_path)

    if not path.exists():
        raise FileNotFoundError(f"文件不存在: {path}")

    if path.suffix.lower() not in SUPPORTED_SUFFIXES:
        raise ValueError(f"暂不支持的文件类型: {path.suffix}")

    result = parse(
        str(path),
        tier="flash",
    )

    markdown = result.markdown().strip()

    if not markdown:
        return []

    return [
        Document(
            page_content=markdown,
            metadata={
                "source": str(path),
                "file_name": path.name,
                "file_type": path.suffix.lower(),
                "parser": "mineru",
            },
        )
    ]