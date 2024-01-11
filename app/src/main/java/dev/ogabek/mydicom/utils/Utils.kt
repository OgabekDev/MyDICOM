package dev.ogabek.mydicom.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


fun getFileFromUri(context: Context, uri: Uri): File? {
    val contentResolver: ContentResolver = context.contentResolver
    val inputStream: InputStream?
    try {
        inputStream = contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val file = createFile(context, getFileName(context, uri))
            copyInputStreamToFile(inputStream, file)
            return file
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme.equals("content")) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result ?: "file"
}

private fun createFile(context: Context, fileName: String): File {
    val storageDir = context.getExternalFilesDir(null)
    return File(storageDir, fileName)
}

private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
    var outputStream: FileOutputStream? = null
    try {
        outputStream = FileOutputStream(file)
        val buffer = ByteArray(4 * 1024)
        var read: Int
        while ((inputStream.read(buffer).also { read = it }) != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            outputStream?.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun getFileSize(file: File): String {
    val fileSizeInBytes = file.length()

    val fileSizeInKB = fileSizeInBytes / 1024.0
    val fileSizeInMB = fileSizeInKB / 1024.0

    return when {
        fileSizeInMB >= 1.0 -> String.format("%.2f MB", fileSizeInMB)
        fileSizeInKB >= 1.0 -> String.format("%.2f KB", fileSizeInKB)
        else -> "$fileSizeInBytes Bytes"
    }
}

private fun yuvToJPEG(image: ImageProxy): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Copy Y
    yBuffer.get(nv21, 0, ySize)
    // Copy UV
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val outStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, outStream)

    return outStream.toByteArray()
}