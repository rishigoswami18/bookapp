package com.hrishikeshbooks.bookapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.hrishikeshbooks.bookapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)

        progressDialog.setTitle("Please wait")

        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var email = ""
    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        if(email.isEmpty()){
            binding.emailTil.error = "Enter email..."
            binding.emailTil.requestFocus()
        }else{
            binding.emailTil.error = null
            resetPassword()
        }

    }

    private fun resetPassword() {
        progressDialog.setMessage("Sending password reset instruction to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Password reset instructions sent to $email", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }
}


