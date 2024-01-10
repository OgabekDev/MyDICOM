package dev.ogabek.mydicom.model

data class Dicom(
    val path: String,
    val name: String,
    val modality: String,
    val size: String
)