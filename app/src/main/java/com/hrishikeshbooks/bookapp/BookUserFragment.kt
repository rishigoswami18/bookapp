package com.hrishikeshbooks.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hrishikeshbooks.bookapp.adapter.AdapterPdfUser
import com.hrishikeshbooks.bookapp.databinding.FragmentBookUserBinding
import com.hrishikeshbooks.bookapp.models.ModelPdf


class BookUserFragment : Fragment {
    private lateinit var binding: FragmentBookUserBinding

    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId:String,category: String,uid:String):BookUserFragment{
            val fragment = BookUserFragment()
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid =""
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        val args =arguments
        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(context),container,false)
        Log.d(TAG, "onCreateView: Category $category")
        if(category == "All"){
            loadAllBooks()

        }else if(category == "Most Viewed"){
            loadMostViewedDownloadedBooks("viewsCount")

        }else if(category == "Most Downloaded"){
            loadMostViewedDownloadedBooks("downloadsCount")

        }else{
            loadCategorizedBooks()
        }
        binding.searchEt.addTextChangedListener (object :TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }catch (e: Exception){
                    Log.d(TAG,"onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        return binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    private fun loadCategorizedBooks() {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }

}