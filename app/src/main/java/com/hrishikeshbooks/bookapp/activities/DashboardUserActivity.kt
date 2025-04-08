package com.hrishikeshbooks.bookapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hrishikeshbooks.bookapp.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hrishikeshbooks.bookapp.*
import com.hrishikeshbooks.bookapp.adapters.ViewPagerAdapter
import com.hrishikeshbooks.bookapp.databinding.ActivityDashboardUserBinding
import com.hrishikeshbooks.bookapp.models.ModelCategory

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupBottomNavigation()
        setupViewPager()
        setupButtons()

        // Auto open Downloads if coming back from purchase
        if (intent.getBooleanExtra("openDownloads", false) && hasPurchased()) {
            binding.bottomNavigationView.selectedItemId = R.id.nav_downloads
        }
    }

    private fun hasPurchased(): Boolean {
        val prefs = getSharedPreferences("purchase_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isPurchased", false)
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.subTitleTv.text = "Not Logged In"
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        } else {
            binding.subTitleTv.text = firebaseUser.email
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_downloads -> {
                    if (hasPurchased()) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, DownloadsFragment())
                            .commit()
                    } else {
                        startActivity(Intent(this, PurchaseActivity::class.java))
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnChatBot.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val defaultCategories = listOf(
                    ModelCategory("01", "All", 1, ""),
                    ModelCategory("02", "Most Viewed", 1, ""),
                    ModelCategory("03", "Most Downloaded", 1, "")
                )

                defaultCategories.forEach {
                    categoryArrayList.add(it)
                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(it.id, it.category, it.uid),
                        it.category
                    )
                }

                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    model?.let {
                        categoryArrayList.add(it)
                        viewPagerAdapter.addFragment(
                            BookUserFragment.newInstance(it.id, it.category, it.uid),
                            it.category
                        )
                    }
                }

                viewPagerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}
