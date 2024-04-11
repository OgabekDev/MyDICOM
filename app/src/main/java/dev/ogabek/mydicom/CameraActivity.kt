package dev.ogabek.mydicom

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import dev.ogabek.mydicom.controller.PermissionController
import dev.ogabek.mydicom.databinding.ActivityCameraBinding
import dev.ogabek.mydicom.model.Frames
import dev.ogabek.mydicom.utils.saveFramesAsImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var permissionController: PermissionController

    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraID: String
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    private var isOpenTakePicture = false
    private var isPicturesDone: Boolean? = null
    private var patientID: String = ""

    private val frameBytes = ArrayList<Frames>()

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            convertSurfaceToImage()
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun convertSurfaceToImage() {

        if (isOpenTakePicture) {

            if (frameBytes.size >= IMAGE_COUNT + 1) {
                isOpenTakePicture = false
                val frames = frameBytes
                frames.removeAt(0)
                GlobalScope.launch {
                    saveFramesAsImage(patientID, frames) {
                        isPicturesDone = it
                    }
                }
                return
            }

            val frame = getFrame() ?: return

            val currentTime = System.currentTimeMillis()

            if (frameBytes.isEmpty()) {
                frameBytes.add(Frames(frame))
                Log.d(TAG, "Image Taken in $currentTime")
            } else if (currentTime - frameBytes.last().takenTime >= INTERVAL) {
                frameBytes.add(Frames(frame))
                Log.d(TAG, "Image Taken in $currentTime")
            } else return

        }

        if (isPicturesDone == true) {
            Toast.makeText(this, "Images Done", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "convertSurfaceToImage: Saved")
            val intent = Intent(this, DicomInformationActivity::class.java)
            intent.putExtra("patientID", patientID)
            startActivity(intent)
            finish()
        } else if (isPicturesDone == false) {
            Toast.makeText(this, "Something went wrong when saving images", Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "convertSurfaceToImage: Not Saved")
            finish()
        }
        isPicturesDone = null

    }

    private fun getFrame(): Bitmap? {
        val height = binding.myCameraPreview.height
        val width = binding.myCameraPreview.width

        return try {
            val frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            binding.myCameraPreview.getBitmap(frame)
            frame
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val stateCallback: StateCallback = object : StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        try {
            val texture: SurfaceTexture = binding.myCameraPreview.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (null == cameraDevice) {
                            return
                        }
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(
                            this@CameraActivity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraID = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraID)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    777
                )
                return
            }
            manager.openCamera(cameraID, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.e(TAG, "openCamera X")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        permissionController = PermissionController(this)

        initViews()

        setContentView(binding.root)
    }

    private fun initViews() {

        if (!permissionController.checkPermission()) {
            permissionController.askPermissionCamera()
        }

        binding.btnTakePhoto.setOnClickListener {
            patientID = UUID.randomUUID().toString().take(8)
            isOpenTakePicture = true
        }

    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (binding.myCameraPreview.isAvailable) {
            openCamera()
        } else {
            binding.myCameraPreview.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {

        private const val TAG = "My DICOM Application TAG"

        private const val INTERVAL = 75L
        private const val IMAGE_COUNT = 5
    }

}