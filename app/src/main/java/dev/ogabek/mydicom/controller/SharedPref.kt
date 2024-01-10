package dev.ogabek.mydicom.controller

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson

class SharedPref(context: Context) {

    private val pref = context.getSharedPreferences("dicom", MODE_PRIVATE)

    fun addPath(path: String) {

        val data = pref.getString("PATHS", null)

        var dicomList = ArrayList<String>()

        if (data != null) {
            dicomList = Gson().fromJson<ArrayList<String>>(data, ArrayList::class.java)
        }

        dicomList.add(path)

        val json = Gson().toJson(dicomList)

        pref
            .edit()
            .putString("PATHS", json)
            .apply()

    }

    fun deletePath(path: String) {

        val data = pref.getString("PATHS", null)

        var dicomList = ArrayList<String>()

        if (data != null) {
            dicomList = Gson().fromJson<ArrayList<String>>(data, ArrayList::class.java)
        }

        dicomList.remove(path)

        val json = Gson().toJson(dicomList)

        pref
            .edit()
            .putString("PATHS", json)
            .apply()

    }

    fun getPaths(): List<String> {

        val data = pref.getString("PATHS", null) ?: return emptyList()

        return Gson().fromJson<ArrayList<String>>(data, ArrayList::class.java)

    }

}