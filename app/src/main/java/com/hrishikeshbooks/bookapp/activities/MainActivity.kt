package com.hrishikeshbooks.bookapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hrishikeshbooks.bookapp.databinding.ActivityMainBinding
import android.content.SharedPreferences
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        sharedPrefs = getSharedPreferences("PurchasePrefs", MODE_PRIVATE)

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.SkipBtn.setOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }
    }
}
