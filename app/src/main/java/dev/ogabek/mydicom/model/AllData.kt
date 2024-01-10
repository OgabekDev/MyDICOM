package dev.ogabek.mydicom.model

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
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

)

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