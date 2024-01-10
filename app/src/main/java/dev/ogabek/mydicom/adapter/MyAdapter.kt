package dev.ogabek.mydicom.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.ogabek.mydicom.R
import dev.ogabek.mydicom.model.Dicom
import java.io.File

class MyAdapter(private val context: Context, private val list: List<Dicom>, private val onClick: (String) -> Unit, private val onLongClick: (String) -> Unit): RecyclerView.Adapter<MyAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName = itemView.findViewById<TextView>(R.id.tvFileName)!!
        val tvExtension = itemView.findViewById<TextView>(R.id.tvModality)!!
        val tvSize = itemView.findViewById<TextView>(R.id.tvFileSize)!!
        val item = itemView.findViewById<LinearLayout>(R.id.item)!!

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dicom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = list[position].name
        holder.tvExtension.text = list[position].modality
        holder.tvSize.text = list[position].size

        holder.item.setOnClickListener {
            onClick.invoke(list[position].path)
        }

        holder.item.setOnLongClickListener {
            onLongClick.invoke(list[position].path)
            true
        }
    }

    override fun getItemCount() = list.size

}