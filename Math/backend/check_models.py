import google.generativeai as genai
from dotenv import load_dotenv
import os

load_dotenv()

genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

print("=== DANH SÁCH MODELS CÓ SẴN ===\n")

for model in genai.list_models():
    if 'generateContent' in model.supported_generation_methods:
        print(f"✅ Model: {model.name}")
        print(f"   Display name: {model.display_name}")
        print(f"   Description: {model.description}")
        print()