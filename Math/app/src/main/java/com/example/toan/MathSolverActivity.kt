package com.example.toan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.toan.api.RetrofitClient
import com.example.toan.model.MathRequest
import com.example.toan.utils.ImagePickerHelper
import com.example.toan.utils.SpeechRecognitionHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MathSolverActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MathSolverActivity"
    }

    private lateinit var inputType: String
    private var etMathInput: TextInputEditText? = null
    private var btnSolve: MaterialButton? = null
    private var btnVoiceInput: MaterialButton? = null
    private var btnImageInput: MaterialButton? = null
    private var tvResult: TextView? = null
    private var progressBar: ProgressBar? = null
    private var scrollViewResult: ScrollView? = null

    private lateinit var speechHelper: SpeechRecognitionHelper
    private lateinit var imagePickerHelper: ImagePickerHelper

    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_solver)

        inputType = intent.getStringExtra("INPUT_TYPE") ?: "text"

        initViews()
        setupListeners()
        configureInputType()

        speechHelper = SpeechRecognitionHelper(this) { recognizedText ->
            etMathInput?.setText(recognizedText)
        }

        imagePickerHelper = ImagePickerHelper(this)
    }

    private fun initViews() {
        etMathInput = findViewById(R.id.etMathInput)
        btnSolve = findViewById(R.id.btnSolve)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        btnImageInput = findViewById(R.id.btnImageInput)
        tvResult = findViewById(R.id.tvResult)
        progressBar = findViewById(R.id.progressBar)
        scrollViewResult = findViewById(R.id.scrollViewResult)
    }

    private fun setupListeners() {
        btnSolve?.setOnClickListener {
            solveMath()
        }

        btnVoiceInput?.setOnClickListener {
            speechHelper.startListening()
        }

        btnImageInput?.setOnClickListener {
            imagePickerHelper.pickImage()
        }
    }

    private fun configureInputType() {
        when (inputType) {
            "text" -> {
                btnVoiceInput?.visibility = View.GONE
                btnImageInput?.visibility = View.GONE
            }
            "voice" -> {
                btnImageInput?.visibility = View.GONE
                speechHelper.startListening()
            }
            "image" -> {
                btnVoiceInput?.visibility = View.GONE
                imagePickerHelper.pickImage()
            }
        }
    }

    private fun solveMath() {
        val mathText = etMathInput?.text.toString().trim()

        Log.d(TAG, "=== BẮT ĐẦU GIẢI TOÁN ===")
        Log.d(TAG, "Math text: $mathText")

        if (mathText.isEmpty() && selectedImageBitmap == null) {
            Toast.makeText(this, "Vui lòng nhập bài toán hoặc chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val imageBase64 = selectedImageBitmap?.let { bitmapToBase64(it) }
        Log.d(TAG, "Image base64 length: ${imageBase64?.length ?: 0}")

        val request = MathRequest(
            text = mathText,
            image = imageBase64
        )

        Log.d(TAG, "Gửi request đến: ${RetrofitClient.apiService}")

        RetrofitClient.apiService.solveMath(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                showLoading(false)
                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d(TAG, "Response body: $result")

                    val solution = result?.get("solution") as? String ?: "Không có kết quả"
                    Log.d(TAG, "Solution: $solution")
                    displayResult(solution)
                } else {
                    // Đọc error body
                    val errorBody = response.errorBody()?.string() ?: "Không có thông tin lỗi"
                    val errorMsg = "Lỗi ${response.code()}: $errorBody"
                    Log.e(TAG, errorMsg)
                    Toast.makeText(this@MathSolverActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                showLoading(false)
                val errorMsg = "Lỗi kết nối: ${t.message}"
                Log.e(TAG, errorMsg, t)
                Toast.makeText(this@MathSolverActivity, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayResult(solution: String) {
        tvResult?.text = solution
        scrollViewResult?.visibility = View.VISIBLE
        scrollViewResult?.post {
            scrollViewResult?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
        btnSolve?.isEnabled = !show
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Giảm chất lượng để giảm kích thước
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()

        // SỬA: Dùng NO_WRAP để không thêm ký tự xuống dòng
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        Log.d(TAG, "Base64 encoded, length: ${base64.length}")
        return base64
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SpeechRecognitionHelper.SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            speechHelper.handleResult(data)
        }

        if (requestCode == ImagePickerHelper.IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageBitmap = imagePickerHelper.handleResult(data)
            selectedImageBitmap?.let {
                Toast.makeText(this, "Đã chọn ảnh. Nhấn 'Giải' để phân tích", Toast.LENGTH_SHORT).show()
            }
        }
    }
}