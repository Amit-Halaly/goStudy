package com.example.gostudy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gostudy.fragments.UserHomeFragment
import com.example.gostudy.fragments.UserCoursesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var friendUid: String
    private lateinit var friendName: String
    private lateinit var friendPhotoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        friendUid = intent.getStringExtra("friendUid") ?: ""
        friendName = intent.getStringExtra("friendName") ?: "User"
        friendPhotoUrl = intent.getStringExtra("friendPhotoUrl") ?: ""

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavUser)

        openFragment(UserHomeFragment.newInstance(friendUid, friendName, friendPhotoUrl))

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_user_home ->
                    openFragment(UserHomeFragment.newInstance(friendUid, friendName, friendPhotoUrl))

                R.id.nav_user_courses ->
                    openFragment(UserCoursesFragment.newInstance(friendUid, friendName, friendPhotoUrl))
            }
            true
        }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.userProfileContainer, fragment)
            .commit()
    }
}
