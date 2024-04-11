package dev.ogabek.mydicom.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dev.ogabek.mydicom.model.AllData
import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag
import org.dcm4che3.data.UID
import org.dcm4che3.data.VR
import org.dcm4che3.io.DicomInputStream
import org.dcm4che3.io.DicomOutputStream
import org.dcm4che3.util.UIDUtils
import java.io.File
import java.util.Date

class DicomController {

    private val attributes = Attributes()

    fun convertImageToDicom(data: AllData, images: List<File>, dicom: File, onResult: (Boolean) -> Unit) {

        try {

            val bitmapImage = BitmapFactory.decodeFile(images[0].absolutePath) ?: return

            addInformation(data, bitmapImage, images.size)

            val newAttribute = Attributes()

            newAttribute.setString(Tag.ImplementationVersionName, VR.SH, "1")
            newAttribute.setString(Tag.ImplementationClassUID, VR.UI, UIDUtils.createUID())
            newAttribute.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian)
            newAttribute.setString(Tag.MediaStorageSOPClassUID, VR.UI, UIDUtils.createUID())
            newAttribute.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, UIDUtils.createUID())
            newAttribute.setInt(Tag.FileMetaInformationVersion, VR.OB, 1)
            newAttribute.setInt(
                Tag.FileMetaInformationGroupLength, VR.UL, attributes.size() + newAttribute.size()
            )

            var pixelData: ByteArray? = null

            for (i in images) {
                val bitmap = BitmapFactory.decodeFile(i.absolutePath)

                val tempBytes = bitmapToRGB(bitmap)

                pixelData = if (pixelData == null) {
                    tempBytes
                } else {
                    val newByteArray = ByteArray(pixelData.size + tempBytes.size)
                    System.arraycopy(pixelData, 0, newByteArray, 0, pixelData.size)
                    System.arraycopy(tempBytes, 0, newByteArray, pixelData.size, tempBytes.size)
                    newByteArray
                }

            }

            attributes.setBytes(Tag.PixelData, VR.OB, pixelData)


            val dicomOutput = DicomOutputStream(dicom)

            dicomOutput.writeDataset(newAttribute, attributes)

            dicomOutput.close()

            onResult.invoke(true)

        } catch (e: Exception) {
            Log.e("DicomController", "Error occurred ${e.message}")
            e.printStackTrace()
            onResult.invoke(false)
        }

    }

    private fun addInformation(data: AllData, bitmapImage: Bitmap, frames: Int) {

        val colorComponent = if (bitmapImage.config == Bitmap.Config.ARGB_8888) 3 else 1
        val bitsPerPixel = 8// * colorComponent

        attributes.setString(Tag.PatientID, VR.LO, data.patientID)
        attributes.setString(Tag.StudyID, VR.SH, data.studyID)
        attributes.setString(Tag.SeriesNumber, VR.IS, data.seriesID)
        attributes.setString(Tag.InstanceNumber, VR.IS, data.instanceID)

        attributes.setString(Tag.PatientName, VR.PN, data.patientName)
        attributes.setDate(Tag.PatientBirthDate, VR.DA, data.patientBirthDate)
        attributes.setString(Tag.PatientSex, VR.CS, data.patientSex)
        attributes.setString(Tag.PatientAge, VR.AS, data.patientAge.toString())

        attributes.setString(Tag.PerformingPhysicianName, VR.PN, data.performingPhysicianName)
        attributes.setString(Tag.ReferringPhysicianName, VR.PN, data.performingPhysicianName)
        attributes.setString(Tag.InstitutionName, VR.LO, data.institutionName)
        attributes.setString(Tag.InstitutionAddress, VR.ST, data.institutionAddress)
        attributes.setString(Tag.Manufacturer, VR.LO, data.manufacturer)
        attributes.setString(Tag.ManufacturerModelName, VR.LO, data.manufacturer)

        attributes.setString(Tag.StudyDescription, VR.LO, data.description)
        attributes.setString(Tag.ContentDescription, VR.LO, data.description)
        attributes.setString(Tag.SeriesDescription, VR.LO, data.description)
        attributes.setString(Tag.ImageComments, VR.LO, data.description)

        attributes.setDate(Tag.StudyDate, VR.DA, Date())
        attributes.setDate(Tag.ContentDate, VR.DA, Date())
        attributes.setDate(Tag.SeriesDate, VR.DA, Date())

        attributes.setDate(Tag.StudyTime, VR.TM, Date())
        attributes.setDate(Tag.ContentTime, VR.TM, Date())
        attributes.setDate(Tag.SeriesTime, VR.TM, Date())

        attributes.setString(Tag.AccessionNumber, VR.SH, UIDUtils.createUID())
        attributes.setString(Tag.Modality, VR.CS, "SC")
        attributes.setString(Tag.PhotometricInterpretation, VR.CS, "RGB")
        attributes.setInt(Tag.SamplesPerPixel, VR.US, 3)
        attributes.setInt(Tag.Rows, VR.US, bitmapImage.height)
        attributes.setInt(Tag.Columns, VR.US, bitmapImage.width)
        attributes.setInt(Tag.BitsAllocated, VR.US, bitsPerPixel)
        attributes.setInt(Tag.BitsStored, VR.US, bitsPerPixel)
        attributes.setInt(Tag.HighBit, VR.US, 7) // bitsPerPixel - 1
        attributes.setInt(Tag.PixelRepresentation, VR.US, 0)
        attributes.setInt(Tag.PlanarConfiguration, VR.US, 0)
        attributes.setInt(Tag.NumberOfFrames, VR.IS, frames)
        attributes.setDate(Tag.InstanceCreationDate, VR.DA, Date())
        attributes.setDate(Tag.InstanceCreationTime, VR.TM, Date())

        attributes.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage)
        attributes.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID())
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID())
        attributes.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID())

    }

    private fun bitmapToRGB(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val data = ByteArray(width * height * 3)
        for (i in pixels.indices) {
            data[i * 3] = (pixels[i] shr 16 and 255).toByte()
            data[i * 3 + 1] = (pixels[i] shr 8 and 255).toByte()
            data[i * 3 + 2] = (pixels[i] and 255).toByte()
        }
        return data
    }

}

fun getAllDataFromDicom(dicomPath: String): AllData? {

    var returnData: AllData? = null

    try {
        val dicomFile = File(dicomPath)

        if (!dicomFile.exists()) {
            return null
        }

        val dicomReader = DicomInputStream(dicomFile)
        val attributes = dicomReader.readDataset()

        val rows = attributes.getInt(Tag.Rows, 1)
        val column = attributes.getInt(Tag.Columns, 1)
        val samplesPerPixel = attributes.getInt(Tag.SamplesPerPixel, 1)
        val numberOfFrames = attributes.getInt(Tag.NumberOfFrames, 1)

        val pixelData = attributes.getSafeBytes(Tag.PixelData)

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

        val description = attributes.getString(Tag.ImageComments)

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
            description ?: "",
            rows,
            column,
            samplesPerPixel,
            numberOfFrames,
            pixelData
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return returnData
}

fun getImageBytesFromPixelData(bytes: ByteArray, numberOfFrames: Int, rows: Int, columns: Int, samplesPerPixel: Int): ArrayList<ByteArray> {

    val images = ArrayList<ByteArray>()

    repeat(numberOfFrames) {
        try {

            images.add(getSingleFrame(bytes, it, rows, columns, samplesPerPixel))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return images

}

private fun getSingleFrame(bytes: ByteArray, frameNumber: Int, rows: Int, columns: Int, samplesPerPixel: Int): ByteArray {

    val frameSize = rows * columns * samplesPerPixel
    val startPixel = frameNumber * frameSize
    if (startPixel + frameSize <= bytes.size) {
        val frameBytes = ByteArray(frameSize)
        System.arraycopy(bytes, startPixel, frameBytes, 0, frameSize)
        return frameBytes
    }

    return ByteArray(0)
}

fun convertByteArrayToBitmap(
    bytes: ByteArray,
    width: Int,
    height: Int
): Bitmap {
    val argbPixels = IntArray(width * height)
    for (i in argbPixels.indices) {
        argbPixels[i] =
            bytes[i * 3].toInt() and 255 shl 16 or -16777216 or (bytes[i * 3 + 1].toInt() and 255 shl 8) or (bytes[i * 3 + 2].toInt() and 255)
    }
    return Bitmap.createBitmap(argbPixels, width, height, Bitmap.Config.ARGB_8888)
}
