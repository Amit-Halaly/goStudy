package com.example.gostudy

import android.annotation.SuppressLint
import com.example.gostudy.models.Course
import com.google.firebase.firestore.FirebaseFirestore


object CoursesRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    val courses: MutableList<Course> = mutableListOf()

    fun loadCoursesForUser(uid: String, onFinished: () -> Unit) {
        courses.clear()

        db.collection("users")
            .document(uid)
            .collection("courses")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val id = doc.id

                    val course = Course(
                        name = name,
                        tasks = mutableListOf(),
                        id = id
                    )
                    courses.add(course)
                }

                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }

    fun addCourse(uid: String, name: String, onFinished: () -> Unit) {
        val data = hashMapOf(
            "name" to name
        )

        db.collection("users")
            .document(uid)
            .collection("courses")
            .add(data)
            .addOnSuccessListener { documentRef ->
                val newCourse = Course(
                    name = name,
                    tasks = mutableListOf(),
                    id = documentRef.id
                )
                courses.add(newCourse)
                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }
}