package com.hrishikeshbooks.bookapp.activities

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hrishikeshbooks.bookapp.adapter.AdapterPdfAdmin
import com.hrishikeshbooks.bookapp.databinding.ActivityPdfListAdminBinding
import com.hrishikeshbooks.bookapp.models.ModelPdf

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subTitleTv.text = category

        loadPdfList()


        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                }
                catch (e:Exception){
                    Log.d(TAG, "onTextChanged: ${e.message}")

                }
            }
            override fun afterTextChanged(p0: Editable?) {
                try {
                    adapterPdfAdmin.filter!!.filter(p0)
                } catch (e: Exception) {
                    Log.d(ContentValues.TAG, "onTextChanged: ${e.message}")
                }
            }
        })
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }




    }

    private fun loadPdfList() {

        pdfArrayList = ArrayList()
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)
                        if (model != null) {
                            pdfArrayList.add(model)

                            Log.d(TAG, "onDataChange: ${model} ${model.categoryId}")
                        }
                    }
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}