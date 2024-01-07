package dev.ogabek.mydicom

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dev.ogabek.mydicom.controller.DicomController
import dev.ogabek.mydicom.controller.Jpg2Dcm
import dev.ogabek.mydicom.databinding.ActivityMainBinding
import dev.ogabek.mydicom.model.getData
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var cameraController: LifecycleCameraController
    private var cameraCapture: ImageCapture? = null

    private lateinit var dicomController: DicomController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        viewBinding.btnTakePhoto.setOnClickListener { takePhotos() }

        setContentView(viewBinding.root)

        if (!hasPermissions(baseContext)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            lifecycleScope.launch {
                startCameraWithCameraCapture()
            }
        }

        dicomController = DicomController()

    }

    private suspend fun startCameraWithCameraCapture() {
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(viewBinding.myCameraPreview.surfaceProvider)

        cameraCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                cameraCapture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
        }

    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                startCameraWithCameraCapture()
            }
        }
    }

    private fun startCameraWithCameraController() {
        val previewView = viewBinding.myCameraPreview
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)

        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        previewView.controller = cameraController
    }

    private fun takePhotos() {
        var name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        name = "captured"

        val contextValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
            }
        }

        val outputOption = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contextValues
            ).build()

        cameraCapture!!.takePicture(
            outputOption,
            Executors.newSingleThreadExecutor(),
            object: ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d(TAG, outputFileResults.savedUri.toString())

                    val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/CameraX-Images/captured.jpg"

                    DicomController().convertImageToDicom(getData(),  File(file), File(externalCacheDir!!.absolutePath + "/" + "captured.dcm"))

                    Jpg2Dcm(File(file), File(externalCacheDir!!.absolutePath + "/" + "captured2.dcm"))

                }

            }
        )

        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/captured.jpg"

        Jpg2Dcm(File(file), File(externalCacheDir!!.absolutePath + "/" + "captured.dcm"))

//        DicomController2().convert(file, externalCacheDir!!.absolutePath + "/" + "captured.dcm")

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

}