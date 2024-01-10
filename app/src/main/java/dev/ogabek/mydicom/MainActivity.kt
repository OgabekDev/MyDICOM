package dev.ogabek.mydicom

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import dev.ogabek.mydicom.controller.DicomController
import dev.ogabek.mydicom.controller.PermissionController
import dev.ogabek.mydicom.databinding.ActivityMainBinding
import dev.ogabek.mydicom.model.getData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var cameraCapture: ImageCapture? = null

    private lateinit var dicomController: DicomController
    private lateinit var permissionController: PermissionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        permissionController = PermissionController(this, 777)

        if (!permissionController.checkPermission()) {
            permissionController.askPermission {
//                if (!it) permissionController.openSettingsForPermission()
            }
        }

        binding.btnCreate.setOnClickListener {

        }

    }

    private fun takePhotos() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contextValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Dicom-Images")
            }
        }

        val outputOption = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contextValues
            ).build()

        for (i in 1..5) cameraCapture!!.takePicture(
            outputOption,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d(TAG, outputFileResults.savedUri.toString())
                    Log.i("Take Picture", "onImageSaved: $i")

                    val file =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Dicom-Images/$name.jpg"

                    DicomController().convertImageToDicom(
                        getData(),
                        File(file),
                        File(externalCacheDir!!.absolutePath + "/" + "$i.dcm")
                    )

                }

            }
        )

    }

    companion object {

        private const val TAG = "My DICOM Application TAG"

        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }

    fun getDicomFiles(context: Context): List<File> {
        val dcmFiles = ArrayList<File>()

        return dcmFiles
    }

}