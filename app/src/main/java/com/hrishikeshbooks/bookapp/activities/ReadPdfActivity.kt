package com.hrishikeshbooks.bookapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.hrishikeshbooks.bookapp.databinding.ActivityReadPdfBinding
import java.io.File

class ReadPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("filePath")

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        if (filePath.isNullOrEmpty()) {
            Toast.makeText(this, "File path is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPdfSafely(file)
    }

    private fun loadPdfSafely(file: File) {
        try {
            binding.pdfView.fromFile(file)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .spacing(10)
                .onError { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                .onPageError { page, t ->
                    Toast.makeText(this, "Page $page error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
                .load()
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "PDF already loaded, can't load again", Toast.LENGTH_SHORT).show()
            Log.e("ReadPdfActivity", "Load called without recycling", e)
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ReadPdfActivity", "Unexpected error", e)
        }
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            binding.pdfView.recycle()
        }
        super.onDestroy()
    }
}
