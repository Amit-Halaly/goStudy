package com.example.gostudy

import android.annotation.SuppressLint
import com.example.gostudy.models.Course
import com.google.firebase.firestore.FirebaseFirestore
import com.example.gostudy.models.Task


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

                if (courses.isEmpty()) {
                    onFinished()
                    return@addOnSuccessListener
                }

                var remaining = courses.size

                for (course in courses) {
                    loadTasksForCourse(uid, course) {
                        remaining--
                        if (remaining == 0) {
                            onFinished()
                        }
                    }
                }
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

    fun loadTasksForCourse(uid: String, course: Course, onFinished: () -> Unit) {
        course.tasks.clear()

        db.collection("users")
            .document(uid)
            .collection("courses")
            .document(course.id)
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val isDone = doc.getBoolean("isDone") == true

                    val task = Task(name = name, isDone = isDone)
                    course.tasks.add(task)
                }
                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }

    fun addTaskToCourse(uid: String, course: Course, taskName: String, onFinished: () -> Unit) {
        val data = hashMapOf(
            "name" to taskName,
            "isDone" to false
        )

        db.collection("users")
            .document(uid)
            .collection("courses")
            .document(course.id)
            .collection("tasks")
            .add(data)
            .addOnSuccessListener {
                course.tasks.add(Task(taskName, false))
                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }

    fun updateTaskStatus(uid: String, course: Course, taskIndex: Int, isDone: Boolean) {
        if (taskIndex < 0 || taskIndex >= course.tasks.size) return
        val task = course.tasks[taskIndex]

        task.isDone = isDone

        db.collection("users")
            .document(uid)
            .collection("courses")
            .document(course.id)
            .collection("tasks")
            .whereEqualTo("name", task.name)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    doc.reference.update("isDone", isDone)
                }
            }
    }
}