package dev.ogabek.mydicom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.ogabek.mydicom.databinding.ActivityDicomViewerBinding
import dev.ogabek.mydicom.model.AllData
import org.dcm4che3.data.Tag
import org.dcm4che3.io.DicomInputStream
import java.io.File
import java.util.Date


class DicomViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDicomViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDicomViewerBinding.inflate(layoutInflater)

        initViews()

        setContentView(binding.root)
    }

    private fun initViews() {

        val dicomPath = intent.getStringExtra("dicomPath") ?: ""

        getAllDataFromDicom(dicomPath)

    }

    private fun getAllDataFromDicom(dicomPath: String): AllData? {

        var returnData: AllData? = null

        try {
            val dicomFile = File(dicomPath)

            val dicomReader = DicomInputStream(dicomFile)
            val attributes = dicomReader.readDataset()

            val rows = attributes.getInt(Tag.Rows, 1)
            val column = attributes.getInt(Tag.Columns, 1)

            val pixelData = attributes.getBytes(Tag.PixelData)

            val patientID = attributes.getString(Tag.PatientID)
            val studyID = attributes.getString(Tag.StudyID)
            val seriesID = attributes.getString(Tag.SeriesNumber)
            val instanceID = attributes.getString(Tag.InstanceNumber)

            val patientName = attributes.getString(Tag.PatientName)
            val patientBirthDate = attributes.getDate(Tag.PatientBirthDate)
            val patientSex = attributes.getString(Tag.PatientSex)
            val patientAge = attributes.getString(Tag.PatientAge).toInt()

            val doctorName = attributes.getString(Tag.PerformingPhysicianName)
            val institutionName = attributes.getString(Tag.InstitutionName)
            val institutionAddress = attributes.getString(Tag.InstitutionAddress)
            val manufacturer = attributes.getString(Tag.Manufacturer)

            val description = attributes.getString(Tag.StudyDescription)

            returnData = AllData(
                patientID ?: "",
                studyID ?: "",
                seriesID ?: "",
                instanceID ?: "",
                patientName ?: "",
                patientBirthDate ?: Date(),
                patientSex ?: "",
                patientAge,
                doctorName ?: "",
                institutionName ?: "",
                institutionAddress ?: "",
                manufacturer ?: "",
                description ?: ""
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return returnData
    }
}