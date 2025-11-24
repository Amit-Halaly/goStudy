package com.example.gostudy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.Task
import com.example.gostudy.R
import com.example.gostudy.TasksAdapter

class CourseDetailsFragment : Fragment() {

    private var courseName: String? = null
    private var totalTasks: Int = 0
    private var completedTasks: Int = 0

    private lateinit var tvTitle: TextView
    private lateinit var rvTasks: RecyclerView

    private val tasksList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseName = it.getString(ARG_COURSE_NAME)
            totalTasks = it.getInt(ARG_TOTAL_TASKS)
            completedTasks = it.getInt(ARG_COMPLETED_TASKS)
        }

        generateTasks()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvCourseTitle)
        rvTasks = view.findViewById(R.id.rvTasks)

        tvTitle.text = courseName ?: "Course Details"

        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = TasksAdapter(tasksList)
    }

    private fun generateTasks() {
        tasksList.clear()
        for (i in 1..totalTasks) {
            val isDone = i <= completedTasks
            tasksList.add(Task("Task $i", isDone))
        }
    }

    companion object {
        private const val ARG_COURSE_NAME = "arg_course_name"
        private const val ARG_TOTAL_TASKS = "arg_total_tasks"
        private const val ARG_COMPLETED_TASKS = "arg_completed_tasks"

        fun newInstance(name: String, totalTasks: Int, completedTasks: Int) =
            CourseDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE_NAME, name)
                    putInt(ARG_TOTAL_TASKS, totalTasks)
                    putInt(ARG_COMPLETED_TASKS, completedTasks)
                }
            }
    }
}
