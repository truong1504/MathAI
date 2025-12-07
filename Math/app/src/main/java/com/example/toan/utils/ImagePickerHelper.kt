// File: C:\Users\MSi\Desktop\Math\app\src\main\java\com\example\toan\utils\ImagePickerHelper.kt

package com.example.toan.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore

class ImagePickerHelper(private val activity: Activity) {
    
    companion object {
        const val IMAGE_PICK_CODE = 102
    }
    
    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    
    fun handleResult(data: Intent?): Bitmap? {
        return try {
            val imageUri = data?.data
            MediaStore.Images.Media.getBitmap(activity.contentResolver, imageUri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}