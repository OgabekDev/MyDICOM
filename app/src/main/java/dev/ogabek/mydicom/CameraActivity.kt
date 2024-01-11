package dev.ogabek.mydicom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.ogabek.mydicom.controller.PermissionController
import dev.ogabek.mydicom.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var permissionController: PermissionController

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

        startCameraWithCameraController()

        binding.btnTakePhoto.setOnClickListener {

        }

    }


    private fun startCameraWithCameraController() {

    }


    companion object {

        private const val TAG = "My DICOM Application TAG"

        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

}