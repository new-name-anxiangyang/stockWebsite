import sys

from app.RAG.mineru_parser import parse_document
from app.RAG.text_splitter import split_documents
from app.RAG.vector_store import get_vector_store


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit(
            "用法: python -m app.RAG.index_doc <文件路径>"
        )

    documents = parse_document(sys.argv[1])
    chunks = split_documents(documents)

    if not chunks:
        raise RuntimeError("文档解析后没有可写入的文本内容")

    ids = get_vector_store().add_documents(chunks)
    print(f"已写入 Milvus 的文本块数量: {len(ids)}")


if __name__ == "__main__":
    main()
