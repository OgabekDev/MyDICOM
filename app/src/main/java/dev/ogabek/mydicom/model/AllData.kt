package dev.ogabek.mydicom.model

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

data class AllData(
    // IDs
    val patientID: String,
    val studyID: String,
    val seriesID: String,
    val instanceID: String,

    // Patient
    val patientName: String,
    val patientBirthDate: String,
    val patientSex: String,
    val patientAge: Int,

    // Doctor
    val performingPhysicianName: String,
    val referringPhysicianName: String,
    val institutionName: String,
    val institutionAddress: String,
    val manufacturer: String,

    // Picture
    val studyDescription: String,
    val seriesDescription: String,
    val contentDescription: String,
    val studyDate: String,
    val seriesDate: String,
    val contentDate: String,
    val studyTime: String,
    val seriesTime: String,
    val contentTime: String

)

@SuppressLint("SimpleDateFormat")
fun getData(): AllData {
    return AllData(
        UUID.randomUUID().toString().take(5),
        UUID.randomUUID().toString().take(5),
        UUID.randomUUID().toString().take(5),
        UUID.randomUUID().toString().take(5),
        "Ben Wilson",
        "20040530",
        "M",
        20,
        "Steve Cons",
        "Steve Cons",
        "Geo2 IUT Global",
        "Ziyolilar 13",
        "Tech A Pro",
        "Patinent is in a good condition",
        "Patinent is in a good condition",
        "Patinent is in a good condition",
        SimpleDateFormat("yyyyMMdd").format(Date()),
        SimpleDateFormat("yyyyMMdd").format(Date()),
        SimpleDateFormat("yyyyMMdd").format(Date()),
        SimpleDateFormat("HHmmss.SSS").format(Date()),
        SimpleDateFormat("HHmmss.SSS").format(Date()),
        SimpleDateFormat("HHmmss.SSS").format(Date())
    )
}