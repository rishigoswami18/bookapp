package com.hrishikeshbooks.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hrishikeshbooks.bookapp.MyApplication
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.adapter.AdapterPdfFavorite
import com.hrishikeshbooks.bookapp.databinding.ActivityProfileBinding
import com.hrishikeshbooks.bookapp.models.ModelPdf

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var bookArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBookCountTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        loadUserInfo()
        loadFavoriteBooks()



        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))

        }

        binding.accountStatusTv.setOnClickListener {
            if(firebaseUser.isEmailVerified){
                Toast.makeText(this,"Already verified...", Toast.LENGTH_SHORT).show()

            }else{
                emailVerificationDialog()
            }
        }


    }
    private fun emailVerificationDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("send email verification instructions ${firebaseUser.email}" )
            .setPositiveButton("SEND"){d,e->
                sendEmailVerification()

            }.setNegativeButton("CANCEL"){d,e->
                d.dismiss()

            }.show()
    }
    private fun sendEmailVerification(){
        progressDialog.setMessage("Sending email verification to email ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Instructions sent! Check your email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun loadUserInfo() {
        if(firebaseUser.isEmailVerified){
            binding.accountStatusTv.text = "Verified"
        }else{
            binding.accountStatusTv.text = "Not Verified"
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("ProfileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    try{
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.person_gray)
                            .into(binding.profileIv)

                    }catch (e: Exception){

                    }



                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    private fun loadFavoriteBooks(){

        bookArrayList = ArrayList();

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookArrayList.clear()
                    for(ds in snapshot.children){
                        val bookId = "${ds.child("bookId").value}"
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId
                        bookArrayList.add(modelPdf)

                    }

                    binding.favoriteBookCountTv.text = "${bookArrayList.size}"
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity,bookArrayList)
                    binding.favoriteRv.adapter = adapterPdfFavorite




                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}