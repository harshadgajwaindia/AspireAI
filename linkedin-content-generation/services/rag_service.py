import json
import os

SAMPLE_POSTS_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "sample_posts.json")

_posts_cache = None

def load_knowledge_base():
    """Loads sample posts into memory once at startup — no external DB, no embedding calls."""
    global _posts_cache
    with open(SAMPLE_POSTS_PATH, "r", encoding="utf-8") as f:
        _posts_cache = json.load(f)
    print(f"Loaded {len(_posts_cache)} posts into in-memory knowledge base!")

def get_similar_posts(user_instruction: str, k: int = 2) -> list:
    """
    Simple in-memory RAG: matches the user's achievement description
    against known post types using keyword detection, and returns
    the best-matching example posts as style references.
    """
    if _posts_cache is None:
        load_knowledge_base()

    text_lower = user_instruction.lower()

    keyword_map = {
        "certificate": ["certificate", "certification", "certified", "course", "program"],
        "project": ["project", "built", "developed", "leetcode", "dsa", "app", "system"],
        "event": ["hackathon", "event", "conference", "summit", "attended"],
        "internship": ["internship", "intern", "offer", "hired"],
        "learning": ["learned", "learning", "days of", "challenge", "streak"],
    }

    matched_type = None
    for post_type, keywords in keyword_map.items():
        if any(kw in text_lower for kw in keywords):
            matched_type = post_type
            break

    if matched_type:
        matches = [p for p in _posts_cache if p["type"] == matched_type]
        if matches:
            return matches[:k]

    # Fallback: no clear match, just return the first k posts as general style reference
    return _posts_cache[:k]