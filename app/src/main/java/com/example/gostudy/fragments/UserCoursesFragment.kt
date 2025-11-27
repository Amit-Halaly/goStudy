package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.R
import com.example.gostudy.adapters.CoursesAdapter
import com.example.gostudy.models.Course
import com.example.gostudy.models.Task
import com.google.firebase.firestore.FirebaseFirestore

class UserCoursesFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var rvCourses: RecyclerView

    private val courseList: MutableList<Course> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private var targetUid: String = ""
    private var targetName: String = ""
    private var targetPhotoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetUid = it.getString(ARG_TARGET_UID, "")
            targetName = it.getString(ARG_TARGET_NAME, "")
            targetPhotoUrl = it.getString(ARG_TARGET_PHOTO, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_courses, container, false)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvUserCoursesTitle)
        rvCourses = view.findViewById(R.id.rvUserCourses)

        tvTitle.text = "${targetName}'s courses"

        rvCourses.layoutManager = LinearLayoutManager(requireContext())
        rvCourses.adapter = CoursesAdapter(courseList) { selectedCourse ->
            openUserCourseDetails(selectedCourse)
        }

        loadCoursesForUser()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCoursesForUser() {
        courseList.clear()

        db.collection("users")
            .document(targetUid)
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
                    courseList.add(course)
                }

                if (courseList.isEmpty()) {
                    rvCourses.adapter?.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                var remaining = courseList.size
                for (course in courseList) {
                    loadTasksForCourse(course) {
                        remaining--
                        if (remaining == 0) {
                            rvCourses.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun loadTasksForCourse(course: Course, onFinished: () -> Unit) {
        course.tasks.clear()

        db.collection("users")
            .document(targetUid)
            .collection("courses")
            .document(course.id)
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val isDone = doc.getBoolean("isDone") ?: false
                    course.tasks.add(Task(name, isDone))
                }
                onFinished()
            }
            .addOnFailureListener {
                onFinished()
            }
    }

    private fun openUserCourseDetails(course: Course) {
        val index = courseList.indexOf(course)
        if (index == -1) return

        val fragment = UserCourseDetailsFragment.newInstance(
            targetUid,
            index,
            targetName
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.userProfileContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val ARG_TARGET_UID = "arg_target_uid"
        private const val ARG_TARGET_NAME = "arg_target_name"
        private const val ARG_TARGET_PHOTO = "arg_target_photo"

        fun newInstance(uid: String, name: String, photoUrl: String) =
            UserCoursesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TARGET_UID, uid)
                    putString(ARG_TARGET_NAME, name)
                    putString(ARG_TARGET_PHOTO, photoUrl)
                }
            }
    }
}
