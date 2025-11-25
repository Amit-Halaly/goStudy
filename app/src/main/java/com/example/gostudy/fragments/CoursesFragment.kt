package com.example.gostudy.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import com.example.gostudy.models.Course
import com.example.gostudy.adapters.CoursesAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.CoursesRepository
import com.example.gostudy.R
import com.google.firebase.auth.FirebaseAuth

class CoursesFragment : Fragment() {

    private lateinit var rvCourses: RecyclerView
    private lateinit var btnAdd: Button

    private val courseList: MutableList<Course>
        get() = CoursesRepository.courses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCourses = view.findViewById(R.id.rvCourses)
        btnAdd = view.findViewById(R.id.btnAddCourse)

        rvCourses.layoutManager = LinearLayoutManager(requireContext())
        rvCourses.adapter = CoursesAdapter(courseList) { selectedCourse ->
            openCourseDetails(selectedCourse)
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && courseList.isEmpty()) {
            CoursesRepository.loadCoursesForUser(uid) {
                rvCourses.adapter?.notifyDataSetChanged()
            }
        }


        btnAdd.setOnClickListener {
            showAddCourseDialog()
        }
    }

    private fun openCourseDetails(course: Course) {
        val index = courseList.indexOf(course)
        if (index == -1) return

        val fragment = CourseDetailsFragment.newInstance(index)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_course, null)

        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Course")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->

                val name = etName.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Course name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                CoursesRepository.addCourse(uid, name) {
                    rvCourses.adapter?.notifyItemInserted(courseList.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        rvCourses.adapter?.notifyDataSetChanged()
    }
}