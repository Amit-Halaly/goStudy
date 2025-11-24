package com.example.gostudy.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import com.example.gostudy.Course
import com.example.gostudy.CoursesAdapter
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
import com.example.gostudy.R

class CoursesFragment : Fragment() {

    private lateinit var rvCourses: RecyclerView
    private lateinit var btnAdd: Button

    private val courseList = mutableListOf<Course>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCourses = view.findViewById(R.id.rvCourses)

        btnAdd = view.findViewById(R.id.btnAddCourse)
        btnAdd.setOnClickListener {
            showAddCourseDialog()
        }


        rvCourses.layoutManager = LinearLayoutManager(requireContext())

        courseList.add(Course("Operating Systems", totalTasks = 10, completedTasks = 7))
        courseList.add(Course("Mobile Development", totalTasks = 8, completedTasks = 3))
        courseList.add(Course("Linear Algebra", totalTasks = 5, completedTasks = 4))


        rvCourses.adapter = CoursesAdapter(courseList)

    }

    @SuppressLint("MissingInflatedId")
    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_course, null)

        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etTotalTasks = dialogView.findViewById<EditText>(R.id.etTotalTasks)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Course")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->

                val name = etName.text.toString().trim()
                val totalTasksStr = etTotalTasks.text.toString().trim()

                if (name.isEmpty() || totalTasksStr.isEmpty()) {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val totalTasks = totalTasksStr.toIntOrNull()
                if (totalTasks == null || totalTasks <= 0) {
                    Toast.makeText(requireContext(), "Total tasks must be a positive number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newCourse = Course(
                    name = name,
                    totalTasks = totalTasks,
                    completedTasks = 0
                )

                courseList.add(newCourse)
                rvCourses.adapter?.notifyItemInserted(courseList.size - 1)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }


}
