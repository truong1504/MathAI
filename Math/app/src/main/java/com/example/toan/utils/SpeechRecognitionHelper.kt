// File: C:\Users\MSi\Desktop\Math\app\src\main\java\com\example\toan\utils\SpeechRecognitionHelper.kt

package com.example.toan.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast

class SpeechRecognitionHelper(
    private val activity: Activity,
    private val onResult: (String) -> Unit
) {
    
    companion object {
        const val SPEECH_REQUEST_CODE = 101
    }
    
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói bài toán của bạn...")
        }
        
        try {
            activity.startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(activity, "Thiết bị không hỗ trợ nhận diện giọng nói", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun handleResult(data: Intent?) {
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!results.isNullOrEmpty()) {
            val recognizedText = results[0]
            onResult(recognizedText)
        }
    }
}