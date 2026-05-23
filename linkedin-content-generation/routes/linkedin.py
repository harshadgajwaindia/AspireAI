import io
from pypdf import PdfReader
from fastapi import APIRouter, HTTPException, Form, UploadFile, File
from typing import Optional
from services.content_service import generate_linkedin_post, get_photo_suggestions

router = APIRouter()

@router.post("/generate")
async def generate_post(
    manual_text: str = Form(...),
    doc_file: Optional[UploadFile] = File(None)
):
    try:
        extracted_pdf_text = ""
        
        # If the user uploaded a PDF file, read and extract text from it cleanly
        if doc_file and doc_file.filename.endswith('.pdf'):
            print(f"📁 Reading uploaded PDF file: {doc_file.filename}")
            
            # Read file bytes into memory
            pdf_bytes = await doc_file.read()
            
            # Load the bytes into PdfReader using a binary stream wrapper
            pdf_stream = io.BytesIO(pdf_bytes)
            reader = PdfReader(pdf_stream)
            
            # Extract text page by page
            text_pages = []
            for page in reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text_pages.append(page_text)
            
            extracted_pdf_text = "\n".join(text_pages).strip()
            print(f"✅ Successfully extracted {len(extracted_pdf_text)} text characters from PDF.")

        # 1. Call the core post generator service
        generated_text = await generate_linkedin_post(
            user_instruction=manual_text,
            extracted_doc_text=extracted_pdf_text
        )
        
        # 2. Get photo layout suggestions
        photo_data = await get_photo_suggestions(generated_text)
        
        return {
            "status": "success",
            "post": generated_text,
            "photo_suggestions": photo_data
        }
        
    except Exception as e:
        print(f"❌ Router Error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to process request: {str(e)}")