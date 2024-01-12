package dev.ogabek.mydicom

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import dev.ogabek.mydicom.controller.convertByteArrayToBitmap
import dev.ogabek.mydicom.controller.getAllDataFromDicom
import dev.ogabek.mydicom.controller.getImageBytesFromPixelData
import dev.ogabek.mydicom.databinding.ActivityDicomViewerBinding
import dev.ogabek.mydicom.model.AllData
import dev.ogabek.mydicom.utils.CustomSheet

class DicomViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDicomViewerBinding

    private val images = ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDicomViewerBinding.inflate(layoutInflater)

        initViews()

        setContentView(binding.root)
    }

    private fun initViews() {

        val dicomPath = intent.getStringExtra("dicomPath") ?: ""

        val data = getAllDataFromDicom(dicomPath) ?: return

        binding.tvPatientName.text = data.patientName

        binding.btnInfo.setOnClickListener {

            val sheet = CustomSheet(data)
            sheet.show(this)
        }

        if (data.pixelData != null) Runnable {
            images.addAll(getImagesFromByteArray(data))
        }.run()

        if (images.isNotEmpty()) binding.imgDicom.setImageBitmap(images.first())

        binding.sbImageChanger.max = data.numberOfFrames - 1
        binding.sbImageChanger.progress = 0

        binding.sbImageChanger.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.imgDicom.setImageBitmap(images[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    private fun getImagesFromByteArray(data: AllData): ArrayList<Bitmap> {
        val imagesBytes = getImageBytesFromPixelData(
            data.pixelData!!,
            data.numberOfFrames,
            data.row,
            data.column,
            data.samplesPerPixel
        )

        return ArrayList<Bitmap>().apply {
            repeat(data.numberOfFrames) {
                add(convertByteArrayToBitmap(imagesBytes[it], data.column, data.row))
            }
        }
    }

}