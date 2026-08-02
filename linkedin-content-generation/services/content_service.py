import os
import asyncio
from dotenv import load_dotenv
import google.generativeai as genai
from langchain_groq import ChatGroq

load_dotenv()

if os.getenv("GEMINI_API_KEY"):
    genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

async def generate_linkedin_post(user_instruction: str, extracted_doc_text: str) -> str:
    """
    Generates a high-impact LinkedIn post using a dual-cloud strategy.
    """
    prompt_blueprint = f"""
    You are an expert personal branding executive and content strategist.
    Your mission is to transform raw user achievements into captivating, high-performing LinkedIn posts.
    
    Here is the background reference context extracted from the user's uploaded document:
    ---
    {extracted_doc_text}
    ---
    
    Using that context, write a compelling LinkedIn post for this new achievement:
    "{user_instruction}"
    
    Formatting Guidelines:
    - Write an attention-grabbing hook at the very beginning.
    - Break up text into short, highly scannable sentences with white space.
    - Integrate a few authentic, clean bullet points for readability.
    - End with a professional takeaway or call-to-action line.
    - Do not use fake or spammy hashtags. Keep it clean and authentic.
    """

    try:
        print("🤖 Processing context payload via Gemini Cloud...")
        model = genai.GenerativeModel("gemini-3.6-flash")
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: model.generate_content(prompt_blueprint))
        if response and response.text:
            print("✅ Post successfully generated via Gemini Cloud!")
            return response.text.strip()
    except Exception as gemini_error:
        print(f"⚠️ Gemini Cloud Exception: {gemini_error}. Rerouting to Groq Cloud Fallback...")

    try:
        print("🚀 Initializing flagship fallback: llama-3.3-70b-versatile...")
        fallback_model = ChatGroq(model_name="llama-3.3-70b-versatile", groq_api_key=os.getenv("GROQ_API_KEY"), temperature=0.5)
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: fallback_model.invoke(prompt_blueprint))
        print("✅ Post successfully generated via Groq Cloud Fallback!")
        return response.content.strip()
    except Exception as groq_error:
        raise RuntimeError(f"Both AI cloud providers failed: {groq_error}")

async def get_photo_suggestions(generated_post: str) -> str:
    """Generates complementary image/photo concepts to pair with the generated post."""
    suggestion_prompt = f"Based on the following LinkedIn post, suggest 2-3 professional image/carousel ideas: '{generated_post}'"
    try:
        fallback_model = ChatGroq(model_name="llama-3.3-70b-versatile", groq_api_key=os.getenv("GROQ_API_KEY"), temperature=0.7)
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: fallback_model.invoke(suggestion_prompt))
        return response.content.strip()
    except Exception:
        return "📸 Photo Concept: A clean workspace overview or a professional milestone graphic displaying key project metrics."