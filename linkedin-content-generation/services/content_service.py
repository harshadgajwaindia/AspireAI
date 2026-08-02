import os
import asyncio
from dotenv import load_dotenv
import google.generativeai as genai
from langchain_groq import ChatGroq
from services.rag_service import get_similar_posts

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
        print("🚀 Processing context payload via Groq Cloud...")
        primary_model = ChatGroq(model_name="llama-3.3-70b-versatile", groq_api_key=os.getenv("GROQ_API_KEY"), temperature=0.5)
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: primary_model.invoke(prompt_blueprint))
        if response and response.content:
            print("✅ Post successfully generated via Groq Cloud!")
            return response.content.strip()
    except Exception as groq_error:
        print(f"⚠️ Groq Cloud Exception: {groq_error}. Rerouting to Gemini Cloud Fallback...")

    try:
        print("🤖 Initializing fallback: Gemini Cloud...")
        model = genai.GenerativeModel("gemini-3.6-flash")
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: model.generate_content(prompt_blueprint))
        if response and response.text:
            print("✅ Post successfully generated via Gemini Cloud Fallback!")
            return response.text.strip()
    except Exception as gemini_error:
        raise RuntimeError(f"Both AI cloud providers failed: {gemini_error}")

async def get_photo_suggestions(generated_post: str) -> list:
    """Generates 3 short, clean photo/visual suggestions to pair with the generated post."""
    suggestion_prompt = f"""Based on this LinkedIn post, suggest exactly 3 photo or visual ideas.

Post:
"{generated_post}"

Rules:
- Each suggestion must be ONE short sentence, under 15 words.
- No sub-bullets, no explanations, no asterisks, no bold text.
- Just the plain idea itself.

Format your response as exactly 3 lines, nothing else:
1. [idea]
2. [idea]
3. [idea]"""

    try:
        fallback_model = ChatGroq(model_name="llama-3.3-70b-versatile", groq_api_key=os.getenv("GROQ_API_KEY"), temperature=0.7)
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, lambda: fallback_model.invoke(suggestion_prompt))
        raw_text = response.content.strip()

        # Split into clean list items, one per line
        suggestions = []
        for line in raw_text.split("\n"):
            cleaned = line.strip()
            if cleaned and cleaned[0].isdigit():
                # Remove leading "1. " / "2. " etc.
                cleaned = cleaned.split(".", 1)[-1].strip()
            if cleaned:
                suggestions.append(cleaned)

        return suggestions[:3] if suggestions else ["A clean workspace photo or milestone graphic showing key achievements."]
    except Exception:
        return ["A clean workspace photo or milestone graphic showing key achievements."]