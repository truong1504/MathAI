// File: C:\Users\MSi\Desktop\Math\app\src\main\java\com\example\toan\MainActivity.kt

package com.example.toan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Kiểm tra quyền
        checkPermissions()

        // Khởi tạo các nút
        setupButtons()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupButtons() {
        val cardTextInput = findViewById<MaterialCardView>(R.id.cardTextInput)
        val cardVoiceInput = findViewById<MaterialCardView>(R.id.cardVoiceInput)
        val cardImageInput = findViewById<MaterialCardView>(R.id.cardImageInput)

        cardTextInput?.setOnClickListener {
            startMathSolver("text")
        }

        cardVoiceInput?.setOnClickListener {
            startMathSolver("voice")
        }

        cardImageInput?.setOnClickListener {
            startMathSolver("image")
        }
    }

    private fun startMathSolver(inputType: String) {
        val intent = Intent(this, MathSolverActivity::class.java)
        intent.putExtra("INPUT_TYPE", inputType)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Cần cấp quyền để sử dụng đầy đủ tính năng", Toast.LENGTH_LONG).show()
            }
        }
    }
}