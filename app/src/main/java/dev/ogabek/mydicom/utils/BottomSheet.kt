package dev.ogabek.mydicom.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.maxkeppeler.sheets.core.Sheet
import dev.ogabek.mydicom.R
import dev.ogabek.mydicom.databinding.LayoutInformationBinding
import dev.ogabek.mydicom.model.AllData

class CustomSheet(private val data: AllData) : Sheet() {

    override val dialogTag = "CustomSheet"

    private lateinit var binding: LayoutInformationBinding
    override fun onCreateLayoutView() =
        LayoutInformationBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvPatientName).text = data.patientName
        view.findViewById<TextView>(R.id.tvPatientAge).text = data.patientAge.toString()
        view.findViewById<TextView>(R.id.tvPatientBirthday).text = data.patientBirthDate.toString()
        view.findViewById<TextView>(R.id.tvPatientSex).text = data.patientSex
        view.findViewById<TextView>(R.id.tvDoctorName).text = data.performingPhysicianName
        view.findViewById<TextView>(R.id.tvHospital).text = data.institutionName
        view.findViewById<TextView>(R.id.tvHospitalAddress).text = data.institutionAddress
        view.findViewById<TextView>(R.id.tvManufacturer).text = data.manufacturer
        view.findViewById<TextView>(R.id.tvPatientDescription).text = data.description

    }

    fun show(ctx: Context): CustomSheet {
        this.windowContext = ctx
        this.show()
        return this
    }
}