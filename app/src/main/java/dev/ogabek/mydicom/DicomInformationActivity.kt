package dev.ogabek.mydicom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import dev.ogabek.mydicom.controller.DicomController
import dev.ogabek.mydicom.controller.SharedPref
import dev.ogabek.mydicom.databinding.ActivityDicomInformationBinding
import dev.ogabek.mydicom.model.AllData
import java.io.File
import java.util.Date
import java.util.UUID

class DicomInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDicomInformationBinding

    private lateinit var pref: SharedPref
    private var patientID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDicomInformationBinding.inflate(layoutInflater)
        pref = SharedPref(this)

        initViews(intent.getStringExtra("patientID")!!)

        setContentView(binding.root)
    }

    private fun initViews(patientID: String) {

        binding.btnSave.setOnClickListener {
            val data = AllData(
                patientID,
                UUID.randomUUID().toString().take(8),
                UUID.randomUUID().toString().take(8),
                UUID.randomUUID().toString().take(8),
                binding.inputPatientName.text.toString(),
                Date(binding.inputPatientDate.text.toString()),
                binding.inputPatientSex.text.toString(),
                binding.inputPatientAge.text.toString().toInt(),
                binding.inputDoctorName.text.toString(),
                binding.inputHospital.text.toString(),
                binding.inputHospitalAddress.text.toString(),
                binding.inputManufacturer.text.toString(),
                binding.inputDescription.text.toString()
            )

            val files = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/MyDICOM Files/$patientID")
            val dicom = File(files.absolutePath + "/$patientID.dcm")

            DicomController().convertImageToDicom(data, files.listFiles()?.toList() ?: listOf(), dicom) {
                if (it) {
                    Toast.makeText(this, "Conversion Done", Toast.LENGTH_SHORT).show()
                    pref.addPath(dicom.absolutePath)
                    val intent = Intent(this, DicomViewerActivity::class.java)
                    intent.putExtra("dicomPath", dicom.absolutePath)
                    startActivity(intent)
                } else
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()

                finish()
            }

        }

    }
}