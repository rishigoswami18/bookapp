package com.hrishikeshbooks.bookapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {


    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()

        }

        binding.updateBtn.setOnClickListener {
            validateData()

        }

    }

    private var name =""


    private fun validateData(){
        name =binding.nameEt.text.toString().trim()
        if(name.isEmpty()){
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
        }else{
            if(imageUri==null){
                updateProfile("")
            }else{
                uploadImage()
            }

        }

    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        val filePathName = "ProfileImages/"+firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask:Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateProfile(uploadedImageUrl: String){
        progressDialog.setMessage("Updating profile...")

        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["name"] = "$name"
        if(imageUri!=null){
            hashMap["ProfileImage"] = uploadedImageUrl

        }
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {


                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("ProfileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"




                    binding.nameEt.setText(name)

                    try{
                        Glide.with(this@ProfileEditActivity)
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

    private fun showImageAttachMenu(){
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            val id = item.itemId
            if(id == 0){
                //camera clicked
                pickImageCamera()
            }
            else if(id == 1){
                pickImageGallery()
            }
            true

        }

    }
    private fun pickImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type ="image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->

            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                //imageUri = data!!.data

                binding.profileIv.setImageURI(imageUri)
            }else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                binding.profileIv.setImageURI(imageUri)
            }else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )

}

