package dev.ogabek.mydicom.controller

import android.graphics.BitmapFactory
import android.util.Log
import com.imebra.CodecFactory
import com.imebra.DataSet
import com.imebra.Image
import com.imebra.TagId
import com.imebra.bitDepth_t
import com.imebra.codecType_t
import com.imebra.imageQuality_t
import dev.ogabek.mydicom.model.AllData
import org.dcm4che3.data.Attributes
import org.dcm4che3.data.ElementDictionary
import org.dcm4che3.data.Tag
import org.dcm4che3.data.VR
import org.dcm4che3.imageio.codec.jpeg.JPEGParser
import org.dcm4che3.io.DicomOutputStream
import org.dcm4che3.util.UIDUtils
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

//import android.graphics.BitmapFactory
//import com.imebra.CodecFactory
//import com.imebra.DataSet
//import com.imebra.Image
//import com.imebra.TagId
//import com.imebra.bitDepth_t.*
//import com.imebra.codecType_t
//import com.imebra.imageQuality_t
//import dev.ogabek.mydicom.model.AllData

class DicomController2 {

    fun convertImageToDicom(image: String, dicom: String) {

        val bitmap = BitmapFactory.decodeFile(image)

        val dataSet = DataSet()

        dataSet.setString(TagId(0x0010, 0x0010), "Ogabek Matyakubov")
        dataSet.setString(TagId(0x0010, 0x0020), "12345")

        val dicomImage = Image(300, 200, bitDepth_t.depthU16, "RGB", 15)

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

        val dicomImage = Image(bitmap.width.toLong(), bitmap.height.toLong(), bitDepth_t.depthU8, "RGB", 15)

        dataSet.setImage(0, dicomImage, imageQuality_t.medium)

        CodecFactory.save(dataSet, dicom, codecType_t.dicom)

    }

    fun convert(image: String, dicom: String) {
        val dataSet = CodecFactory.load(image)

        dataSet.setString(TagId(0x0010, 0x0010), "Ogabek Matyakubov")
        dataSet.setString(TagId(0x0010, 0x0020), "12345")

        CodecFactory.save(dataSet, dicom, codecType_t.dicom)

    }

}


class DicomController {

    private var attribute = Attributes()
    fun convertImageToDicom(data: AllData, image: String, dicom: String) {

        attribute = Attributes()

        addInfo(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID())
        addInfo(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID())
        addInfo(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID())
        addInfo(Tag.SOPClassUID, VR.UI, UIDUtils.createUID())

        addInfo(Tag.PatientID, data.patientID)
        addInfo(Tag.StudyID, data.studyID)
        addInfo(Tag.SeriesNumber, data.seriesID)
        addInfo(Tag.InstanceNumber, data.instanceID)

        addInfo(Tag.PatientName, data.patientName)
        addInfo(Tag.PatientBirthDate, data.patientBirthDate)
        addInfo(Tag.PatientSex, data.patientSex)
        addInfo(Tag.PatientAge, data.patientAge.toString())

        addInfo(Tag.PerformingPhysicianName, data.performingPhysicianName)
        addInfo(Tag.ReferringPhysicianName, data.referringPhysicianName)
        addInfo(Tag.InstitutionName, data.institutionName)
        addInfo(Tag.InstitutionAddress, data.institutionAddress)
        addInfo(Tag.Manufacturer, data.manufacturer)

        addInfo(Tag.StudyDescription, data.studyDescription)
        addInfo(Tag.SeriesDescription, data.seriesDescription)
        addInfo(Tag.ContentDescription, data.contentDescription)
        addInfo(Tag.StudyDate, data.studyDate)
        addInfo(Tag.SeriesDate, data.seriesDate)
        addInfo(Tag.ContentDate, data.contentDate)
        addInfo(Tag.StudyTime, data.studyTime)
        addInfo(Tag.SeriesTime, data.seriesTime)
        addInfo(Tag.ContentTime, data.contentTime)

        generateDicomFile(Paths.get(image, ""), Paths.get(dicom, ""))

        Log.d("TAG", "convertImageToDicom: Done")

    }

    private fun addInfo(tag: Int, value: String) {
        attribute.setString(tag, ElementDictionary.getElementDictionary("ogabekdev").vrOf(tag), value)
    }

//    private fun addInfo(tag: Int, value: Date) {
//        attribute.setDate(tag, ElementDictionary.vrOf(tag, "ogabekdev"), value)
//    }

//    private fun addInfo(tag: Int, value: Int) {
//        attribute.setInt(tag, ElementDictionary.vrOf(tag, "ogabekdev"), value)
//    }

    private fun addInfo(tag: Int, vr: VR, value: String) {
        attribute.setString(tag, ElementDictionary.getElementDictionary("ogabekdev").vrOf(tag), value)
    }
    private fun generateDicomFile(imagePath: Path, dicomPath: Path) {
        try {

            val metaData = Attributes()
            metaData.addAll(attribute)

            val channel = Files.newByteChannel(imagePath)

            val dicomOutputStream = DicomOutputStream(dicomPath.toFile())

            try {
                val parser = JPEGParser(channel)

                parser.getAttributes(metaData)

                dicomOutputStream.writeDataset(metaData.createFileMetaInformation(parser.transferSyntaxUID), metaData)
                dicomOutputStream.writeHeader(Tag.PixelData, VR.OB, -1)
                dicomOutputStream.writeHeader(Tag.Item, null, 0)

                writePixelToOutputStream(channel, parser.codeStreamPosition, dicomOutputStream)

                dicomOutputStream.writeHeader(Tag.SequenceDelimitationItem, null, 0)

                dicomOutputStream.close()

                if (channel != null) {
                    channel.close()
                    return
                }
                return

            } catch (e: Exception) {
                channel?.close()
                e.printStackTrace()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    private fun writePixelToOutputStream(
        channel: SeekableByteChannel,
        position: Long,
        dicomOutputStream: DicomOutputStream
    ) {
        val size = channel.size() - position
        dicomOutputStream.writeHeader(Tag.Item, null, ((size + 1) and -2).toInt())
        channel.position(position)
        write(channel, dicomOutputStream)
        if ((1 and size.toInt()) != 0) {
            dicomOutputStream.write(0)
        }
    }

    private fun write(enter: ByteChannel, output: OutputStream) {
        val buffer = ByteBuffer.wrap(ByteArray(8162))

        while (true) {
            val read = enter.read(buffer)
            if (read > 0) {
                output.write(ByteArray(8162), 0, read)
                buffer.clear()
            } else {
                return
            }
        }
    }

}
