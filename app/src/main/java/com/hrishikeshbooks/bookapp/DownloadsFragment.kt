package com.hrishikeshbooks.bookapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hrishikeshbooks.bookapp.activities.ReadPdfActivity
import java.io.File

class DownloadsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DownloadAdapter
    private var pdfFiles: MutableList<File> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_downloads, container, false)

        recyclerView = view.findViewById(R.id.downloadsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())



        // Only allow access if purchased
        if (isPurchased()) {
            loadDownloadedFiles()
        } else {
            Toast.makeText(requireContext(), "Please purchase to access downloads", Toast.LENGTH_LONG).show()
        }

        return view
    }


    private fun isPurchased(): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences("purchase_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("isPurchased", false)
    }

    private fun loadDownloadedFiles() {
        val dir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        pdfFiles = dir?.listFiles { file -> file.extension == "pdf" }?.toMutableList() ?: mutableListOf()

        if (pdfFiles.isEmpty()) {
            Toast.makeText(requireContext(), "No downloaded PDFs found", Toast.LENGTH_SHORT).show()
        }

        adapter = DownloadAdapter(
            pdfFiles,
            onItemClick = { file -> openPdf(file) },
            onItemLongClick = { view, file -> showPopupMenu(view, file) }
        )

        recyclerView.adapter = adapter
    }

    private fun openPdf(file: File) {
        val intent = Intent(requireContext(), ReadPdfActivity::class.java).apply {
            putExtra("filePath", file.absolutePath)
        }
        startActivity(intent)
    }

    private fun showPopupMenu(view: View, file: File) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_download_item, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_open -> {
                    openPdf(file)
                    true
                }
                R.id.action_delete -> {
                    if (file.delete()) {
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                        loadDownloadedFiles() // Refresh list
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
