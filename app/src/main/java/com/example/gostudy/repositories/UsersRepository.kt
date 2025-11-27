package com.example.gostudy.repositories

import android.annotation.SuppressLint
import com.example.gostudy.models.AppUser
import com.google.firebase.firestore.FirebaseFirestore

object UsersRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    val users: MutableList<AppUser> = mutableListOf()

    fun loadAllUsers(currentUid: String?, onFinished: () -> Unit) {
        users.clear()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val uid = doc.id
                    if (uid == currentUid) continue

                    val name = doc.getString("displayName") ?: "(no name)"
                    val photoUrl = doc.getString("photoUrl") ?: ""

                    users.add(
                        AppUser(
                            uid = uid,
                            displayName = name,
                            photoUrl = photoUrl
                        )
                    )
                }
                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }
}