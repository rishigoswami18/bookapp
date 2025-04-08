package com.hrishikeshbooks.bookapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hrishikeshbooks.bookapp.databinding.FragmentHomeBinding
import com.hrishikeshbooks.bookapp.models.ModelCategory
import com.hrishikeshbooks.bookapp.adapters.ViewPagerAdapter


class HomeFragment : Fragment() {



    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()



        return binding.root
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            childFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
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

                viewPager.adapter = viewPagerAdapter

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
