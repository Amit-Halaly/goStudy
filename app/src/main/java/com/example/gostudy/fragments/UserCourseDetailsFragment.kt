package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.R
import com.example.gostudy.adapters.TasksAdapter
import com.example.gostudy.models.Course
import com.example.gostudy.models.Task
import com.google.firebase.firestore.FirebaseFirestore

class UserCourseDetailsFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvTasks: RecyclerView

    private val db = FirebaseFirestore.getInstance()

    private var targetUid: String = ""
    private var courseIndex: Int = -1
    private var userName: String = ""

    private val courseList: MutableList<Course> = mutableListOf()
    private val tasksList: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetUid = it.getString(ARG_TARGET_UID, "")
            courseIndex = it.getInt(ARG_COURSE_INDEX, -1)
            userName = it.getString(ARG_USER_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_course_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvUserCourseTitle)
        tvSummary = view.findViewById(R.id.tvUserCourseSummary)
        progressBar = view.findViewById(R.id.progressUserCourse)
        rvTasks = view.findViewById(R.id.rvUserTasks)

        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = TasksAdapter(
            tasksList,
            onTaskToggled = { _, _ -> },
            isReadOnly = true
        )

        loadCoursesAndTasks()
    }

    @SuppressLint("SetTextI18n")
    private fun loadCoursesAndTasks() {
        if (courseIndex == -1) return

        db.collection("users")
            .document(targetUid)
            .collection("courses")
            .get()
            .addOnSuccessListener { result ->
                courseList.clear()

                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val id = doc.id
                    courseList.add(Course(name, mutableListOf(), id))
                }

                if (courseIndex < 0 || courseIndex >= courseList.size) return@addOnSuccessListener

                val course = courseList[courseIndex]
                tvTitle.text = "${userName} - ${course.name}"

                loadTasksForCourse(course)
            }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun loadTasksForCourse(course: Course) {
        tasksList.clear()

        db.collection("users")
            .document(targetUid)
            .collection("courses")
            .document(course.id)
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                var completed = 0

                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val isDone = doc.getBoolean("isDone") ?: false

                    tasksList.add(Task(name, isDone))
                    if (isDone) completed++
                }

                val total = tasksList.size
                val percent = if (total == 0) 0 else (completed * 100 / total)

                tvSummary.text = "$completed of $total tasks done ($percent%)"
                progressBar.max = 100
                progressBar.progress = percent

                rvTasks.adapter?.notifyDataSetChanged()
            }
    }

    companion object {
        private const val ARG_TARGET_UID = "arg_target_uid"
        private const val ARG_COURSE_INDEX = "arg_course_index"
        private const val ARG_USER_NAME = "arg_user_name"

        fun newInstance(uid: String, courseIndex: Int, userName: String) =
            UserCourseDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TARGET_UID, uid)
                    putInt(ARG_COURSE_INDEX, courseIndex)
                    putString(ARG_USER_NAME, userName)
                }
            }
    }
}
