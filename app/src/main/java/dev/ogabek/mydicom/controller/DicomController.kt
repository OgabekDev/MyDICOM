package dev.ogabek.mydicom.controller

import android.graphics.BitmapFactory
import android.widget.Toast
import com.imebra.CodecFactory
import com.imebra.DataSet
import com.imebra.Image
import com.imebra.TagId
import com.imebra.bitDepth_t
import com.imebra.bitDepth_t.depthU16
import com.imebra.codecType_t
import com.imebra.imageQuality_t
import dev.ogabek.mydicom.model.AllData

class DicomController {

    fun convertImageToDicom(image: String, dicom: String) {

        val bitmap = BitmapFactory.decodeFile(image)

        val dataSet = DataSet()

        dataSet.setString(TagId(0x0010, 0x0010), "Ogabek Matyakubov")
        dataSet.setString(TagId(0x0010, 0x0020), "12345")

        val dicomImage = Image(300, 200, depthU16, "RGB", 15)

        val dataHandler = dicomImage.writingDataHandler

        for (i in 0..200) {
            for (j in 0..300) {
                dataHandler.setUnsignedLong((i * bitmap.width.toLong() + j) * 3, 65536)
                dataHandler.setUnsignedLong((i * bitmap.width.toLong() + j) * 3 + 1, 65536)
                dataHandler.setUnsignedLong((i * bitmap.width.toLong() + j) * 3 + 2, 65536)
            }
        }

        dataHandler.delete()

        dataSet.setImage(0, dicomImage, imageQuality_t.medium)
        CodecFactory.save(dataSet, dicom, codecType_t.dicom)

    }

    fun convertImageToDicom(data: AllData, image: String, dicom: String) {

        val bitmap = BitmapFactory.decodeFile(image)

        val dataSet = DataSet()

        dataSet.setString(TagId(0x0010, 0x0010), "Ogabek Matyakubov")
        dataSet.setString(TagId(0x0010, 0x0020), "12345")

        val dicomImage = Image(bitmap.width.toLong(), bitmap.height.toLong(), depthU16, "RGB", 15)

        dataSet.setImage(0, dicomImage, imageQuality_t.medium)

        CodecFactory.save(dataSet, dicom, codecType_t.dicom)

    }

}