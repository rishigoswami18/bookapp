//package com.hrishikeshbooks.bookapp
//
//import android.app.ProgressDialog
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.os.Environment
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.hrishikeshbooks.bookapp.databinding.ActivityPdfDetailBinding
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.google.firebase.storage.FirebaseStorage
//import java.io.FileOutputStream
//import kotlin.math.log
//
//class PdfDetailActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityPdfDetailBinding
//
//    private companion object{
//        const val TAG = "PDF_DETAIL_TAG"
//    }
//
//
//    private var bookId = ""
//    private var bookTitle = ""
//
//    private var bookUrl = ""
//    private lateinit var progressDialog: ProgressDialog
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        bookId = intent.getStringExtra("bookId")!!
//
//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please wait")
//        progressDialog.setCanceledOnTouchOutside(false)
//
//
//
//        MyApplication.incrementBookViewCount(bookId)
//
//        loadBookDetails()
//
//        binding.backBtn.setOnClickListener {
//            onBackPressed()
//
//        }
//        binding.readBookBtn.setOnClickListener {
//            val intent = Intent(this, PdfViewActivity::class.java)
//            intent.putExtra("bookId", bookId)
//            startActivity(intent)
//
//        }
//        binding.downloadBookBtn.setOnClickListener {
//           if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//               Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
//               downloadBook()
//           }
//            else{
//               Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted LETS request it")
//               requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//
//           }
//
//        }
//
//    }
//
//    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
//        isGranted: Boolean ->
//        if(isGranted){
//            Log.d(TAG, "onCreate: STORAGE PERMISSION was granted")
//            downloadBook()
//        }
//        else{
//            Log.d(TAG, "onCreate: STORAGE PERMISSION was denied")
//            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
//        }
//
//    }
//
//    private fun downloadBook(){
//        Log.d(TAG, "downloadBook: Downloading Book")
//
//        progressDialog.setMessage("Downloading book")
//
//        progressDialog.show()
//
//        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
//        storageReference.getBytes(Constants.MAX_BYTES_PDF)
//            .addOnSuccessListener {bytes->
//                Log.d(TAG, "downloadBook: Book downloaded...")
//                saveToDownloadsFolder(bytes)
//
//            }.addOnFailureListener {e->
//                Log.d(TAG, "downloadBook: Failed to download due to ${e.message}")
//                Toast.makeText(this,"Failed to download book due to${e.message}",Toast.LENGTH_SHORT).show()
//
//
//            }
//
//    }
//
//    private fun saveToDownloadsFolder(bytes: ByteArray?) {
//        Log.d(TAG, "saveToDownloadsFolder: saving downloaded book")
//        val nameWithExtention ="$bookTitle.pdf"
//        try {
//            val downloadsFolder = Environment.getExternalStorageDirectory(Environment.DIRECTORY_DOWNLOADS)
//            downloadsFolder.mkdirs()
//
//            val filePath = downloadsFolder.path +"/"+nameWithExtention
//            val out = FileOutputStream(filePath)
//            out.write(bytes)
//            out.close()
//            Toast.makeText(this,"save to Downloads Folder",Toast.LENGTH_SHORT).show()
//            incrementDownloadCount()
//
//        }catch (e:Exception){
//            Log.d(TAG, "saveToDownloadsFolder: Failed to save due to ${e.message}")
//            Toast.makeText(this,"Failed to save due to${e.message}",Toast.LENGTH_SHORT).show()
//
//        }
//
//
//
//    }
//
//    private fun incrementDownloadCount() {
//        Log.d(TAG, "incrementDownloadCount: ")
//        val ref = FirebaseDatabase.getInstance().getReference("Books")
//        ref.child(bookId)
//            .addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
//                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")
//                    if(downloadsCount=="" || downloadsCount=="null"){
//                        downloadsCount = "0"
//                    }
//
//                    val newDownloadCount = downloadsCount.toLong()+1
//                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadCount")
//                    val hashMap: HashMap<String, Any> = HashMap()
//                    hashMap["downloadsCount"] = newDownloadCount
//                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
//                    dbRef.child(bookId)
//                        .updateChildren(hashMap)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "onDataChange: Downloads Count incremented")
//                            Toast.makeText(this@PdfDetailActivity, "Downloads Count incremented", Toast.LENGTH_SHORT).show()
//
//                        }.addOnFailureListener {e->
//                            Log.d(TAG, "onDataChange: Failed to increment due to ${e.message}")
//
//                        }
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//
//                }
//            })
//    }
//
//
//    private fun loadBookDetails() {
//        val ref = FirebaseDatabase.getInstance().getReference("Books")
//        ref.child(bookId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val categoryId = "${snapshot.child("categoryId").value}"
//                    val description = "${snapshot.child("description").value}"
//                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
//                    val timestamp = "${snapshot.child("timestamp").value}"
//                    val uid = "${snapshot.child("uid").value}"
//                    bookUrl = "${snapshot.child("url").value}"
//                    bookTitle = "${snapshot.child("title").value}"
//                    val viewsCount = "${snapshot.child("viewsCount").value}"
//
//                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
//                    MyApplication.loadCategory(categoryId,binding.categoryTv)
//                    MyApplication.loadPdfFromUrlSinglePage("$bookUrl", "$bookTitle", binding.pdfView, binding.progressBar, binding.pagesTv)
//                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)
//
//                    binding.titleTv.text = title
//                    binding.descriptionTv.text = description
//                    binding.viewTv.text = viewsCount
//                    binding.downloadTv.text = downloadsCount
//                    binding.dateTv.text = date
//
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//    }
//}
package com.hrishikeshbooks.bookapp.activities

