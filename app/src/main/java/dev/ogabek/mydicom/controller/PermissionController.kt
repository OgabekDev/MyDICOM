package dev.ogabek.mydicom.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionController(private val context: Context, private val requestCode: Int) {

    private val readStoragePermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private val writeStoragePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val cameraPermission = android.Manifest.permission.CAMERA

    fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            readStoragePermission
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    writeStoragePermission
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    cameraPermission
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun askPermission(result: (Boolean) -> Unit) {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(readStoragePermission, writeStoragePermission),
                requestCode
            )
            return result.invoke(false)
        }
        return result.invoke(true)
    }

    fun openSettingsForPermission() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}