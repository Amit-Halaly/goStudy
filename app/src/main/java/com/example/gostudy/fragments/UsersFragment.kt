package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.R
import com.example.gostudy.repositories.UsersRepository
import com.example.gostudy.adapters.UsersAdapter
import com.example.gostudy.models.AppUser
import com.google.firebase.auth.FirebaseAuth

class UsersFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView

    private val usersList: MutableList<AppUser>
        get() = UsersRepository.users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsers = view.findViewById(R.id.rvUsers)

        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = UsersAdapter(usersList) { user ->
            openUserCourses(user)
        }

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUid != null && usersList.isEmpty()) {
            UsersRepository.loadAllUsers(currentUid) {
                rvUsers.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun openUserCourses(user: AppUser) {
        val fragment = UserCoursesFragment.newInstance(
            user.uid,
            user.displayName,
            user.photoUrl
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
