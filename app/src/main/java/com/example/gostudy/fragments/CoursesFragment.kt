package com.example.gostudy.fragments

import com.example.gostudy.Course
import com.example.gostudy.CoursesAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        rvCourses.layoutManager = LinearLayoutManager(requireContext())

        courseList.add(Course("Operating Systems", 2, 70))
        courseList.add(Course("Mobile Development", 3, 45))
        courseList.add(Course("Linear Algebra", 1, 90))

        rvCourses.adapter = CoursesAdapter(courseList)

    }
}
