import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routes.linkedin import router as linkedin_router
from services.rag_service import load_knowledge_base

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Server starting — loading ChromaDB...")
    load_knowledge_base()
    print("Ready!")
    yield
    print("Shutting down...")

app = FastAPI(title="LinkedIn Content Generator", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(linkedin_router, prefix="/api/linkedin")

@app.get("/")
def root():
    return {"message": "LinkedIn Content Generator is running!"}

if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)