package dev.ogabek.mydicom

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import dev.ogabek.mydicom.adapter.MyAdapter
import dev.ogabek.mydicom.controller.DicomController
import dev.ogabek.mydicom.controller.PermissionController
import dev.ogabek.mydicom.controller.SharedPref
import dev.ogabek.mydicom.databinding.ActivityMainBinding
import dev.ogabek.mydicom.model.Dicom
import dev.ogabek.mydicom.model.getData
import dev.ogabek.mydicom.utils.getFileFromUri
import dev.ogabek.mydicom.utils.getFileSize
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = "My DICOM Application TAG"

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: SharedPref

    private lateinit var permissionController: PermissionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        initViews()

        setContentView(binding.root)

    }

    private val onClick = { it: String ->

        val file = File(it)

        if (!file.exists()) {
            Toast.makeText(this, "No File exist, removed from list", Toast.LENGTH_SHORT).show()
            pref.deletePath(it)
            setAdapter()
        }

        if (file.extension != "dcm") {
            Toast.makeText(this, "This is not dicom file, removed from list", Toast.LENGTH_SHORT)
                .show()
            pref.deletePath(it)
            setAdapter()
        }
//
        val intent = Intent(this, DicomViewerActivity::class.java)
        intent.putExtra("dicomPath", it)
        startActivity(intent)

    }

    private val onDelete = { it: String ->
        pref.deletePath(it)
        setAdapter()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"  // Allow any file type

        startActivityForResult(intent, 1)
    }

    private fun initViews() {

        pref = SharedPref(this)

        permissionController = PermissionController(this)

        if (!permissionController.checkPermission()) {
            permissionController.askPermission()
        }

        binding.rvDicomFiles.layoutManager = LinearLayoutManager(this, VERTICAL, false)
        setAdapter()

        binding.btnCreate.setOnClickListener {

            try {
                val images = ArrayList<File>().apply {
                    repeat(5) {
                        add(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/down.jpg"))
                    }
                }

                val data = getData()
                var dicom =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/DicomFiles/${data.patientID}.dcm"
                dicom = externalCacheDir!!.absolutePath + "/${data.patientID}.dcm"

                DicomController().convertImageToDicom(data, images, File(dicom))

                pref.addPath(dicom)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                setAdapter()
            }

//            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.btnOpen.setOnClickListener {
            openFilePicker()
        }

    }

    private fun setAdapter() {
        binding.rvDicomFiles.adapter = MyAdapter(this, getDicomFiles(), onClick, onDelete)
    }

    private fun getDicomFiles(): List<Dicom> {
        val dcmFiles = ArrayList<Dicom>()

        val files = pref.getPaths()

        for (i in files) {
            val file = File(i)
            dcmFiles.add(
                Dicom(
                    File(i).absolutePath,
                    file.name,
                    file.extension,
                    getFileSize(file)
                )
            )
        }

        return dcmFiles
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let {
                val file = getFileFromUri(this, it)!!
                if (file.extension != "dcm") {
                    Toast.makeText(this, "This is not dicom file", Toast.LENGTH_SHORT).show()
                    return
                }
                pref.addPath(getFileFromUri(this, it)!!.absolutePath)
                setAdapter()
                Log.d(TAG, "onActivityResult: ")
            }
        }
    }

}