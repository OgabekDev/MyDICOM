package dev.ogabek.mydicom.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dev.ogabek.mydicom.model.AllData
import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag
import org.dcm4che3.data.UID
import org.dcm4che3.data.VR
import org.dcm4che3.io.DicomOutputStream
import org.dcm4che3.util.UIDUtils
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.util.Date

class DicomController {

    private val attributes = Attributes()

    fun convertImageToDicom(data: AllData, image: File, dicom: File) {

        try {
            val fileLength = image.length().toInt()

            val bitmapImage = BitmapFactory.decodeFile(image.absolutePath) ?: return

            addInformation(data, bitmapImage)

            val newAttribute = Attributes()

            newAttribute.setString(Tag.ImplementationVersionName, VR.SH, "ImplementationVersionName")
            newAttribute.setString(Tag.ImplementationClassUID, VR.UI, UIDUtils.createUID())
            newAttribute.setString(Tag.TransferSyntaxUID, VR.UI, UID.JPEGLossless)
            newAttribute.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.JPEGLossless)
            newAttribute.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, UIDUtils.createUID())
            newAttribute.setInt(Tag.FileMetaInformationVersion, VR.OB, 1)
            newAttribute.setInt(Tag.FileMetaInformationGroupLength, VR.UL, attributes.size() + newAttribute.size())

            val dicomOutput = DicomOutputStream(dicom)

            dicomOutput.writeDataset(newAttribute, attributes)
            dicomOutput.writeHeader(Tag.PixelData, VR.OB, -1)
            dicomOutput.writeHeader(Tag.Item, null, 0)
            dicomOutput.writeHeader(Tag.Item, null, fileLength + 1 and 1.inv())

            val bufferedInput = BufferedInputStream(FileInputStream(image))
            val dataInput = DataInputStream(bufferedInput)

            val buffer = ByteArray(65536)
            var bytesRead: Int
            while (dataInput.read(buffer).also { bytesRead = it } > 0) {
                dicomOutput.write(buffer, 0, bytesRead)
            }

            if (fileLength and 1 != 0)
                dicomOutput.write(0)

            dicomOutput.writeHeader(Tag.SequenceDelimitationItem, null, 0)

            dicomOutput.close()

            Log.d("DicomController", "Conversion Done")

        } catch (e: Exception) {
            Log.e("DicomController", "Error occurred ${e.message}")
            e.printStackTrace()
        }

    }

    private fun addInformation(data: AllData, bitmapImage: Bitmap) {

        val colorComponent = if (bitmapImage.config == Bitmap.Config.ARGB_8888) 3 else 1
        val bitsPerPixel = 8 * colorComponent

        attributes.setString(Tag.PatientID, VR.LO, data.patientID)
        attributes.setString(Tag.StudyID, VR.SH, data.studyID)
        attributes.setString(Tag.SeriesNumber, VR.IS, data.seriesID)
        attributes.setString(Tag.InstanceNumber, VR.IS, data.instanceID)

        attributes.setString(Tag.PatientName, VR.PN, data.patientName)
        attributes.setDate(Tag.PatientBirthDate, VR.DA, data.patientBirthDate)
        attributes.setString(Tag.PatientSex, VR.CS, data.patientSex)
        attributes.setString(Tag.PatientAge, VR.AS, data.patientAge.toString())

        attributes.setString(Tag.PerformingPhysicianName, VR.PN, data.performingPhysicianName)
        attributes.setString(Tag.ReferringPhysicianName, VR.PN, data.referringPhysicianName)
        attributes.setString(Tag.InstitutionName, VR.LO, data.institutionName)
        attributes.setString(Tag.InstitutionAddress, VR.ST, data.institutionAddress)
        attributes.setString(Tag.Manufacturer, VR.LO, data.manufacturer)
        attributes.setString(Tag.ManufacturerModelName, VR.LO, data.manufacturer)

        attributes.setString(Tag.StudyDescription, VR.LO, data.studyDescription)
        attributes.setString(Tag.ContentDescription, VR.LO, data.contentDescription)
        attributes.setString(Tag.SeriesDescription, VR.LO, data.seriesDescription)

        attributes.setDate(Tag.StudyDate, VR.DA, Date())
        attributes.setDate(Tag.ContentDate, VR.DA, Date())
        attributes.setDate(Tag.SeriesDate, VR.DA, Date())

        attributes.setDate(Tag.StudyTime, VR.TM, Date())
        attributes.setDate(Tag.ContentTime, VR.TM, Date())
        attributes.setDate(Tag.SeriesTime, VR.TM, Date())

        attributes.setString(Tag.AccessionNumber, VR.SH, UIDUtils.createUID())
        attributes.setString(Tag.Modality, VR.CS, "SC")
        attributes.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100")
        attributes.setString(Tag.PhotometricInterpretation, VR.CS, if (colorComponent == 3) "YBR_FULL_422" else "MONOCHROME2")
        attributes.setInt(Tag.SamplesPerPixel, VR.US, bitsPerPixel)
        attributes.setInt(Tag.Rows, VR.US, bitmapImage.height)
        attributes.setInt(Tag.Columns, VR.US, bitmapImage.width)
        attributes.setInt(Tag.BitsAllocated, VR.US, bitsPerPixel)
        attributes.setInt(Tag.BitsStored, VR.US, bitsPerPixel)
        attributes.setInt(Tag.HighBit, VR.US, 11) // bitsPerPixel - 1
        attributes.setInt(Tag.PixelRepresentation, VR.US, 0)
        attributes.setDate(Tag.InstanceCreationDate, VR.DA, Date())
        attributes.setDate(Tag.InstanceCreationTime, VR.TM, Date())

        attributes.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage)
        attributes.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID())
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID())
        attributes.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID())

    }

}
