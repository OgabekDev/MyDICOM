package dev.ogabek.mydicom.model

import android.graphics.Bitmap

data class Frames(
    val imageFile: Bitmap,
    val takenTime: Long = System.currentTimeMillis()
)
