package dev.ogabek.mydicom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.ogabek.mydicom.controller.getAllDataFromDicom
import dev.ogabek.mydicom.databinding.ActivityDicomViewerBinding
import dev.ogabek.mydicom.utils.CustomSheet

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

        val data = getAllDataFromDicom(dicomPath)

        binding.btnInfo.setOnClickListener {
            if (data == null)
                return@setOnClickListener

            val sheet = CustomSheet(data)
            sheet.show(this)
        }


    }
}