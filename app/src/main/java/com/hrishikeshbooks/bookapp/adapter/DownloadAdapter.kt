package com.hrishikeshbooks.bookapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.DateFormat
import java.util.*

class DownloadAdapter(
    private val files: List<File>,
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (View, File) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.FileViewHolder>() {

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileNameTv)
        val fileInfo: TextView = view.findViewById(R.id.fileDetailsTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = file.name
        val date = DateFormat.getDateTimeInstance().format(Date(file.lastModified()))
        val size = "%.2f MB".format(file.length() / (1024.0 * 1024.0))
        holder.fileInfo.text = "$size • $date"

        holder.itemView.setOnClickListener { onItemClick(file) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(it, file)
            true
        }
    }

    override fun getItemCount(): Int = files.size
}
