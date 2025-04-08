package com.hrishikeshbooks.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.AlertDialogDefaults

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.hrishikeshbooks.bookapp.Constants.MAX_BYTES_PDF
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        val backgroundScope = CoroutineScope(Dispatchers.IO)


    }



    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }


                }.addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: failed to get metadata due to ${e.message}")

                }
        }



        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_LOAD_TAG"
            try {
                pdfView.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "loadPdfFromUrlSinglePage: ${e.message}")
            }

            val cacheDir = pdfView.context.cacheDir
            val fileName = "${pdfTitle.hashCode()}.pdf"
            val file = File(cacheDir, fileName)

            // If PDF already cached, load it from cache
            if (file.exists()) {
                Log.d(TAG, "loadPdfFromUrlSinglePage: Loading from cache")
                val bytes = file.readBytes()
                displaySinglePagePdf(bytes, pdfView, progressBar)
                return
            }

            // Not cached, download from Firebase
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    try {
                        // Cache the PDF file
                        file.writeBytes(bytes)
                        Log.d(TAG, "loadPdfFromUrlSinglePage: PDF downloaded and cached")
                        displaySinglePagePdf(bytes, pdfView, progressBar)
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        Log.e(TAG, "loadPdfFromUrlSinglePage: Failed to cache PDF: ${e.message}", e)
                        Toast.makeText(pdfView.context, "Failed to cache PDF", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "loadPdfFromUrlSinglePage: Failed to load PDF: ${exception.message}", exception)
                    Toast.makeText(pdfView.context, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                }
        }

        private fun displaySinglePagePdf(
            bytes: ByteArray,
            pdfView: PDFView,
            progressBar: ProgressBar
        ) {
            pdfView.fromBytes(bytes)
                .pages(0) // First page only
                .enableSwipe(false)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onLoad {
                    progressBar.visibility = View.GONE
                }
                .onError { error ->
                    progressBar.visibility = View.GONE
                    Log.e("PDF_VIEW_ERROR", "Error loading PDF: ${error.message}", error)
                    Toast.makeText(pdfView.context, "Failed to render PDF", Toast.LENGTH_SHORT).show()
                }
                .onPageError { page, error ->
                    progressBar.visibility = View.GONE
                    Log.e("PDF_PAGE_ERROR", "Error on page $page: ${error.message}", error)
                    Toast.makeText(pdfView.context, "Error rendering page $page", Toast.LENGTH_SHORT).show()
                }
                .load()
        }





        fun loadCategory(categoryId: String, categoryTv: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category: String = "${snapshot.child("category").value}"
                        categoryTv.text = category

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }


        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook: deleting")
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from from db now...")
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_LONG)
                                .show()
                            Log.d(TAG, "deleteBook: Deleted from db too...")

                        }.addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(
                                context,
                                "Failed to delete from db due to ${e.message}", Toast.LENGTH_SHORT
                            ).show()

                        }

                }.addOnFailureListener { e ->
                    Log.d(TAG, "deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete from storage due to ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }

        fun incrementBookViewCount(bookId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        var viewsCount = "${snapshot.child("viewsCount").value}"
                        if(viewsCount =="" || viewsCount=="null"){
                            viewsCount = "0"
                        }
                        val newViewsCount = viewsCount.toLong() + 1
                        val hashmap = HashMap<String, Any>() // ✅ Create an instance
                        hashmap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashmap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        public fun removeFromFavorite(context: Context,bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFromFavorite: Removing from fav")
            val firebaseAuth = FirebaseAuth.getInstance()
            val uid = firebaseAuth.uid
            if(uid == null){
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                return
            }

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: Removed from fav")
                    Toast.makeText(context, "Removed from fav", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener { e ->
                    Log.d(TAG, "removeFromFavorite: Failed to remove from favorite due to ${e.message}")
                    Toast.makeText(context, "Failed : ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }

    }
}