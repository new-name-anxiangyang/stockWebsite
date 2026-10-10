from functools import lru_cache

from langchain_milvus import Milvus


@lru_cache(maxsize=1)
def get_vector_store() -> Milvus:
    from app.RAG.embeddings import embeddings

    return Milvus(
        embedding_function=embeddings,
        connection_args={
            "uri": "http://localhost:19530",
        },
        collection_name="stock_knowledge",
        drop_old=False,
    )
