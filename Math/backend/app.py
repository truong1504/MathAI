from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import os
from dotenv import load_dotenv
import base64
from PIL import Image
import io

load_dotenv()

app = Flask(__name__)
CORS(app)

# Cấu hình Gemini
api_key = os.getenv("GEMINI_API_KEY")
if not api_key:
    print("❌ CẢNH BÁO: Không tìm thấy GEMINI_API_KEY trong .env")
else:
    print(f"✅ API Key loaded: {api_key[:20]}...")
    
genai.configure(api_key=api_key)

# Thử nhiều models để tìm model có quota free
MODELS_TO_TRY = [
    "gemini-1.5-flash",           # Model phổ biến nhất cho free tier
    "gemini-1.5-flash-latest",    # Latest của 1.5
    "gemini-1.5-pro",             # Pro version
    "gemini-pro",                 # Classic model
    "gemini-2.5-flash",           # Mới nhất
    "gemini-flash-latest",        # Latest wrapper
    "gemini-2.0-flash-lite",      # Lite version
]

MODEL_NAME = None
print("\n🔍 Đang tìm model khả dụng...")
for model_name in MODELS_TO_TRY:
    try:
        print(f"   Thử model: {model_name}...", end=" ")
        test_model = genai.GenerativeModel(model_name)
        # Test với request MINIMAL
        test_response = test_model.generate_content("1+1")
        MODEL_NAME = model_name
        print(f"✅ THÀNH CÔNG!")
        print(f"✅ Sử dụng model: {MODEL_NAME}\n")
        break
    except Exception as e:
        error_msg = str(e)
        if "429" in error_msg or "quota" in error_msg.lower():
            print(f"❌ Hết quota")
        elif "404" in error_msg:
            print(f"❌ Không tồn tại")
        else:
            print(f"❌ Lỗi: {error_msg[:50]}")

if not MODEL_NAME:
    print("\n❌ TẤT CẢ MODELS ĐỀU KHÔNG KHẢ DỤNG!")
    print("📋 Nguyên nhân có thể:")
    print("   1. API key hết quota (đợi 24h)")
    print("   2. API key không hợp lệ")
    print("   3. Project chưa enable Gemini API")
    print("\n🔧 Giải pháp:")
    print("   1. Tạo API key MỚI tại: https://aistudio.google.com/apikey")
    print("   2. Hoặc đợi đến ngày mai để quota reset")
    print("   3. Kiểm tra usage tại: https://ai.dev/usage\n")

@app.route('/solve', methods=['POST'])
def solve_math():
    try:
        if not MODEL_NAME:
            return jsonify({
                "success": False,
                "error": "Không có model khả dụng. Vui lòng tạo API key mới tại https://aistudio.google.com/apikey"
            }), 503

        print("\n=== NHẬN REQUEST MỚI ===")
        data = request.json
        print(f"Request data keys: {data.keys() if data else 'None'}")
        
        math_text = data.get('text', '')
        image_base64 = data.get('image', None)
        
        print(f"Math text: {math_text}")
        print(f"Image present: {image_base64 is not None}")

        # Khởi tạo model
        model = genai.GenerativeModel(MODEL_NAME)

        # Prompt hệ thống - NGẮN GỌN
        system_prompt = """Bạn là trợ lý giải toán. 
Trả lời NGẮN GỌN, đi thẳng vào đáp án.

Quy tắc:
- Bài toán đơn giản: Chỉ đưa đáp án
- Bài toán phức tạp: Giải tóm tắt 2-3 bước rồi kết luận
- Không dài dòng, không lặp lại đề bài
- Trả lời bằng tiếng Việt"""

        parts = []

        # Xử lý ảnh nếu có
        if image_base64:
            try:
                # Loại bỏ ký tự xuống dòng
                image_base64_clean = image_base64.replace('\n', '').replace('\r', '').strip()
                print(f"Image base64 length: {len(image_base64_clean)}")
                
                image_bytes = base64.b64decode(image_base64_clean)
                print(f"Decoded image size: {len(image_bytes)} bytes")
                
                image = Image.open(io.BytesIO(image_bytes))
                print(f"Image format: {image.format}, size: {image.size}")
                
                parts.append(image)
            except Exception as img_error:
                print(f"❌ Lỗi xử lý ảnh: {img_error}")
                return jsonify({
                    "success": False,
                    "error": f"Lỗi xử lý ảnh: {str(img_error)}"
                }), 400

        # Tạo prompt
        if math_text:
            prompt = f"{system_prompt}\n\nBài toán: {math_text}"
        else:
            prompt = f"{system_prompt}\n\nHãy phân tích và giải bài toán trong ảnh."
            
        parts.insert(0, prompt)

        print(f"Số phần tử gửi tới Gemini: {len(parts)}")
        print(f"Sử dụng model: {MODEL_NAME}")
        
        # Gọi Gemini
        print("Đang gọi Gemini API...")
        response = model.generate_content(parts)
        print(f"✅ Gemini response length: {len(response.text)} chars")

        return jsonify({
            "success": True,
            "solution": response.text
        })

    except Exception as e:
        print(f"❌ LỖI SERVER: {str(e)}")
        import traceback
        traceback.print_exc()
        
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        "status": "OK", 
        "message": "Server đang chạy",
        "model": MODEL_NAME
    })


if __name__ == "__main__":
    print("🚀 Server đang khởi động...")
    print(f"📱 Android Emulator: http://10.0.2.2:5000")
    print(f"💻 Máy thật: http://192.168.100.8:5000")
    app.run(debug=True, host="0.0.0.0", port=5000)