package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gostudy.R
import com.example.gostudy.models.Course
import com.example.gostudy.models.Task
import com.google.firebase.firestore.FirebaseFirestore

class UserHomeFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvGpa: TextView
    private lateinit var tvNextTask: TextView
    private lateinit var tvStats: TextView

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var friendUid: String = ""
    private var friendName: String = "User"
    private var friendPhotoUrl: String = ""


    private val coursesList: MutableList<Course> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            friendUid = it.getString(ARG_FRIEND_UID, "")
            friendName = it.getString(ARG_FRIEND_NAME, "User")
            friendPhotoUrl = it.getString(ARG_FRIEND_PHOTO, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_home, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvUserHomeTitle)
        tvSubtitle = view.findViewById(R.id.tvUserHomeSubtitle)
        tvGpa = view.findViewById(R.id.tvUserHomeGpa)
        tvNextTask = view.findViewById(R.id.tvUserHomeNextTask)
        tvStats = view.findViewById(R.id.tvUserHomeStats)

        tvTitle.text = "$friendName's overview"

        loadGpa()
        loadCoursesAndTasks()
    }

    @SuppressLint("DefaultLocale")
    private fun loadGpa() {
        if (friendUid.isEmpty()) return

        db.collection("users")
            .document(friendUid)
            .get()
            .addOnSuccessListener { doc ->
                val gpa = doc.getDouble("gpa")
                tvGpa.text = gpa?.let { String.format("%.2f", it) } ?: "--"
            }
            .addOnFailureListener {
                tvGpa.text = "--"
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadCoursesAndTasks() {
        if (friendUid.isEmpty()) return

        db.collection("users")
            .document(friendUid)
            .collection("courses")
            .get()
            .addOnSuccessListener { result ->
                coursesList.clear()

                val tasksLoaders = mutableListOf<() -> Unit>()
                var pending = 0

                for (doc in result) {
                    val courseId = doc.id
                    val name = doc.getString("name") ?: "Course"

                    val course = Course(
                        name = name,
                        tasks = mutableListOf()
                    )
                    coursesList.add(course)

                    pending++
                    val loader: () -> Unit = {
                        db.collection("users")
                            .document(friendUid)
                            .collection("courses")
                            .document(courseId)
                            .collection("tasks")
                            .get()
                            .addOnSuccessListener { tasksSnapshot ->
                                val tasks = mutableListOf<Task>()
                                for (taskDoc in tasksSnapshot) {
                                    val tName = taskDoc.getString("name") ?: "Task"
                                    val isDone = taskDoc.getBoolean("isDone") == true
                                    tasks.add(Task(name = tName, isDone = isDone))
                                }
                                course.tasks.clear()
                                course.tasks.addAll(tasks)

                                pending--
                                if (pending == 0) {
                                    updateSummary()
                                }
                            }
                            .addOnFailureListener {
                                pending--
                                if (pending == 0) {
                                    updateSummary()
                                }
                            }
                    }
                    tasksLoaders.add(loader)
                }

                if (pending == 0) {
                    updateSummary()
                } else {
                    tasksLoaders.forEach { it() }
                }
            }
            .addOnFailureListener {
                tvNextTask.text = "No tasks yet"
                tvStats.text = "No study stats yet"
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSummary() {
        if (coursesList.isEmpty()) {
            tvNextTask.text = "No tasks yet"
            tvStats.text = "No study stats yet"
            return
        }

        var totalCourses = coursesList.size
        var totalTasks = 0
        var completedTasks = 0
        var nextTaskText: String? = null

        for (course in coursesList) {
            totalTasks += course.totalTasks
            completedTasks += course.completedTasks

            if (nextTaskText == null) {
                val nextTask = course.tasks.firstOrNull { !it.isDone }
                if (nextTask != null) {
                    nextTaskText = "${course.name}: ${nextTask.name}"
                }
            }
        }

        if (nextTaskText == null) {
            tvNextTask.text = "All tasks completed 🎉"
        } else {
            tvNextTask.text = nextTaskText
        }

        tvStats.text = "Courses: $totalCourses • Tasks: $totalTasks • Done: $completedTasks"
    }

    companion object {
        private const val ARG_FRIEND_UID = "arg_friend_uid"
        private const val ARG_FRIEND_NAME = "arg_friend_name"
        private const val ARG_FRIEND_PHOTO = "arg_friend_photo"

        fun newInstance(friendUid: String, friendName: String, photoUrl: String = "") =
            UserHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FRIEND_UID, friendUid)
                    putString(ARG_FRIEND_NAME, friendName)
                    putString(ARG_FRIEND_PHOTO, photoUrl)
                }
            }
    }

}
