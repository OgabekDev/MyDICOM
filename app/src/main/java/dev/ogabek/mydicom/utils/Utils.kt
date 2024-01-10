package dev.ogabek.mydicom.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
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