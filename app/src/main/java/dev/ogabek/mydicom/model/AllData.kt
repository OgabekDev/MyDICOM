package dev.ogabek.mydicom.model

import android.annotation.SuppressLint
import java.util.Date

data class AllData(
    // IDs
    val patientID: String,
    val studyID: String,
    val seriesID: String,
    val instanceID: String,

    // Patient
    val patientName: String,
    val patientBirthDate: Date,
    val patientSex: String,
    val patientAge: Int,

    // Doctor
    val performingPhysicianName: String,
    val institutionName: String,
    val institutionAddress: String,
    val manufacturer: String,

    // Picture
    val description: String,

    // Dicom Information
    var row: Int = 0,
    var column: Int = 0,
    var samplesPerPixel: Int = 0,
    var numberOfFrames: Int = 0,
    var pixelData: ByteArray? = null,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllData

        if (patientID != other.patientID) return false
        if (studyID != other.studyID) return false
        if (seriesID != other.seriesID) return false
        if (instanceID != other.instanceID) return false
        if (patientName != other.patientName) return false
        if (patientBirthDate != other.patientBirthDate) return false
        if (patientSex != other.patientSex) return false
        if (patientAge != other.patientAge) return false
        if (performingPhysicianName != other.performingPhysicianName) return false
        if (institutionName != other.institutionName) return false
        if (institutionAddress != other.institutionAddress) return false
        if (manufacturer != other.manufacturer) return false
        if (description != other.description) return false
        if (pixelData != null) {
            if (other.pixelData == null) return false
            if (!pixelData.contentEquals(other.pixelData)) return false
        } else if (other.pixelData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = patientID.hashCode()
        result = 31 * result + studyID.hashCode()
        result = 31 * result + seriesID.hashCode()
        result = 31 * result + instanceID.hashCode()
        result = 31 * result + patientName.hashCode()
        result = 31 * result + patientBirthDate.hashCode()
        result = 31 * result + patientSex.hashCode()
        result = 31 * result + patientAge
        result = 31 * result + performingPhysicianName.hashCode()
        result = 31 * result + institutionName.hashCode()
        result = 31 * result + institutionAddress.hashCode()
        result = 31 * result + manufacturer.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (pixelData?.contentHashCode() ?: 0)
        return result
    }
}

@SuppressLint("SimpleDateFormat")
fun getData(): AllData {
    return AllData(
        "2040530",//abs(Random.nextInt()).toString(),
        "2040530",//abs(Random.nextInt()).toString(),
        "2040530",//abs(Random.nextInt()).toString(),
        "2040530",//abs(Random.nextInt()).toString(),
        "Ben Wilson",
        Date(20040530),
        "M",
        20,
        "Steve Cons",
        "Geo2 IUT Global",
        "Ziyolilar 13",
        "Tech A Pro",
        "Patinent is in a good condition"
    )
}