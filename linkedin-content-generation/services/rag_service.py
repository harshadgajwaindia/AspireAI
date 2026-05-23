import os
import json
from langchain_chroma import Chroma
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_core.documents import Document
from dotenv import load_dotenv

load_dotenv()

CHROMA_DB_PATH = "./chromadb_store"
SAMPLE_POSTS_PATH = "./data/sample_posts.json"

def get_embeddings():
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("GEMINI_API_KEY missing!")
    return GoogleGenerativeAIEmbeddings(
        model="models/gemini-embedding-001",
        google_api_key=api_key
    )

def load_knowledge_base():
    try:
        embeddings = get_embeddings()
        if os.path.exists(CHROMA_DB_PATH):
            print("ChromaDB already loaded.")
            return Chroma(
                persist_directory=CHROMA_DB_PATH,
                embedding_function=embeddings
            )
        print("Loading knowledge base for first time...")
        with open(SAMPLE_POSTS_PATH, "r", encoding="utf-8") as f:
            posts = json.load(f)
        documents = [
            Document(
                page_content=post["text"],
                metadata={"type": post["type"]}
            )
            for post in posts
        ]
        vectorstore = Chroma.from_documents(
            documents=documents,
            embedding=embeddings,
            persist_directory=CHROMA_DB_PATH
        )
        print(f"Loaded {len(documents)} posts into ChromaDB!")
        return vectorstore
    except Exception as e:
        print(f"⚠️ Warning: Could not initialize ChromaDB Knowledge Base: {e}")
        return None

def get_similar_posts(query: str, achievement_type: str, k: int = 2) -> list:
    try:
        embeddings = get_embeddings()
        vectorstore = Chroma(
            persist_directory=CHROMA_DB_PATH,
            embedding_function=embeddings
        )
        results = vectorstore.similarity_search(
            query=query,
            k=k,
            filter={"type": achievement_type}
        )
        return [doc.page_content for doc in results]
    except Exception as e:
        # If Gemini Embeddings API is blocked or rate limited, return empty list instead of crashing
        print(f"⚠️ RAG embedding lookup failed ({type(e).__name__}). Proceeding without database context examples...")
        return []