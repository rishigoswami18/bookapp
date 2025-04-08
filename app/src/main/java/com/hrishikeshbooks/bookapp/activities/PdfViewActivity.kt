package com.hrishikeshbooks.bookapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.hrishikeshbooks.bookapp.Constants
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.databinding.ActivityPdfViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding
    private lateinit var adView: AdView
    private var bookId = ""

    companion object {
        private const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        MobileAds.initialize(this) {
            Log.d(TAG, "MobileAds initialized")
           loadBannerAd()
        }

        bookId = intent.getStringExtra("bookId") ?: run {
            Toast.makeText(this, "Invalid book ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadBookDetails()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadBannerAd() {
        adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Ad loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load: ${adError.message}")
            }
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "Loading book details for ID: $bookId")
        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference("Books")
            .child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value?.toString() ?: run {
                        Toast.makeText(this@PdfViewActivity, "Invalid book URL", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    loadBookFromUrl(pdfUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}")
                    Toast.makeText(this@PdfViewActivity, "Failed to load book", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "Loading PDF from URL: $pdfUrl")

        FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            .getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        binding.toolbarSubtitleTv.text = "Page ${page + 1} of $pageCount"
                    }
                    .onError { error ->
                        Log.e(TAG, "PDF rendering error: ${error?.message}")
                        Toast.makeText(this, "Error displaying PDF", Toast.LENGTH_SHORT).show()
                    }
                    .onPageError { page, error ->
                        Log.e(TAG, "Error on page $page: ${error?.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to download PDF: ${exception.message}")
                Toast.makeText(this, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

//    override fun onResume() {
//        super.onResume()
//        if (::adView.isInitialized) {
//            adView.resume()
//        }
//    }
//
//    override fun onPause() {
//        if (::adView.isInitialized) {
//            adView.pause()
//        }
//        super.onPause()
//    }
//
//    override fun onDestroy() {
//        if (::adView.isInitialized) {
//            adView.destroy()
//        }
//        super.onDestroy()
//    }
}
