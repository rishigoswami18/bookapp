package com.hrishikeshbooks.bookapp.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.hrishikeshbooks.bookapp.Constants
import com.hrishikeshbooks.bookapp.MyApplication
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.adapter.AdapterComment
import com.hrishikeshbooks.bookapp.databinding.ActivityPdfDetailBinding
import com.hrishikeshbooks.bookapp.databinding.DialogCommentAddBinding
import com.hrishikeshbooks.bookapp.models.ModelComment
import java.io.File
import java.io.OutputStream

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG = "PDF_DETAIL_TAG"
    }

    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""

    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentArrayList: ArrayList<ModelComment>

    private lateinit var adapterComment: AdapterComment

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId") ?: ""

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!=null){
         checkIsFavorite()
        }


//        progressDialog = ProgressDialog(this).apply {
//            setTitle("Please wait")
//            setCanceledOnTouchOutside(false)
//        }

        MyApplication.Companion.incrementBookViewCount(bookId)
        loadBookDetails()
        showComments()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }
        binding.downloadBookBtn.setOnClickListener {
            downloadBook()
        }


        binding.favoriteBtn.setOnClickListener {
            if(firebaseAuth.currentUser==null){
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
            }
            else{
                if(isInMyFavorite){

                    MyApplication.removeFromFavorite(this,bookId)
                }
                else{
                    addToFavorite()
                }

            }

        }

        binding.addCommentBtn.setOnClickListener {
            if(firebaseAuth.currentUser==null){
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
            }
            else{
                addCommentDialog()
            }
        }
    }

    private fun showComments() {

        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(model!!)

                    }
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }

    private var comment = ""

    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this,R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        commentAddBinding.submitBtn.setOnClickListener {
            comment  = commentAddBinding.commentEt.text.toString().trim()
            if(comment.isEmpty()){
                Toast.makeText(this, "Please enter comment", Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                addComment()
            }

        }


    }
    private fun addComment() {
        progressDialog.setMessage("Adding comment...")
        progressDialog.show()

        val timestamp ="${System.currentTimeMillis()}"
        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Comment added...", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Storage permission granted.")
            downloadBook()
        } else {
            Log.d(TAG, "Storage permission denied.")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun downloadBook() {
        Log.d(TAG, "Downloading book...")

        progressDialog.setMessage("Downloading book...")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "Book downloaded successfully.")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to download book: ${e.message}")
                Toast.makeText(this, "Failed to download book: ${e.message}", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToDownloadsFolder(bytes: ByteArray) {
        Log.d(TAG, "Saving book to app Downloads folder...")

        val fileName = "$bookTitle.pdf"
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)


        try {
            if (downloadsDir != null && downloadsDir.exists() || downloadsDir?.mkdirs() == true) {
                val file = File(downloadsDir, fileName)
                val outputStream: OutputStream = file.outputStream()
                outputStream.write(bytes)
                outputStream.close()

                Toast.makeText(this, "Saved to app's Downloads folder", Toast.LENGTH_SHORT).show()
                incrementDownloadCount()
            } else {
                Toast.makeText(this, "Failed to access app's Downloads folder", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save due to: ${e.message}")
            Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            progressDialog.dismiss()
        }
    }




    private fun incrementDownloadCount() {
        Log.d(TAG, "Incrementing download count...")
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var downloadsCount = snapshot.child("downloadsCount").value.toString()
                if (downloadsCount.isEmpty() || downloadsCount == "null") {
                    downloadsCount = "0"
                }

                val newDownloadCount = downloadsCount.toLong() + 1
                ref.child("downloadsCount").setValue(newDownloadCount)
                    .addOnSuccessListener {
                        Log.d(TAG, "Download count updated successfully.")
                        Toast.makeText(this@PdfDetailActivity, "Download count updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update download count: ${e.message}")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = snapshot.child("categoryId").value.toString()
                val description = snapshot.child("description").value.toString()
                val downloadsCount = snapshot.child("downloadsCount").value.toString()
                val timestamp = snapshot.child("timestamp").value.toString()
                val uid = snapshot.child("uid").value.toString()
                bookUrl = snapshot.child("url").value.toString()
                bookTitle = snapshot.child("title").value.toString()
                val viewsCount = snapshot.child("viewsCount").value.toString()

                val date = MyApplication.Companion.formatTimeStamp(timestamp.toLong())
                MyApplication.Companion.loadCategory(categoryId, binding.categoryTv)
                MyApplication.Companion.loadPdfFromUrlSinglePage(bookUrl, bookTitle, binding.pdfView, binding.progressBar, binding.pagesTv)
                MyApplication.Companion.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                binding.titleTv.text = bookTitle
                binding.descriptionTv.text = description
                binding.viewTv.text = viewsCount
                binding.downloadTv.text = downloadsCount
                binding.dateTv.text = date
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load book details: ${error.message}")
            }
        })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite){

                        Log.d(TAG, "onDataChange: Book is in fav")

                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.favorite_filled_white,0,0)
                        binding.favoriteBtn.text = "Remove fav"

                    }else{

                        Log.d(TAG, "onDataChange: Book is not in fav")

                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.favorite_border_white,0,0)
                        binding.favoriteBtn.text = "Add favorite"

                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })


    }

    private fun addToFavorite(){
        Log.d(TAG, "addToFavorite: Adding to favorite...")
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String,Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Added to favorite successfully")
            }.addOnFailureListener {e->
                Log.d(TAG, "addToFavorite: Failed to add to due to ${e.message} ")
                Toast.makeText(this, "Failed to add to favorite due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

}