package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.models.Task
import com.example.gostudy.R
import com.example.gostudy.adapters.TasksAdapter
import androidx.appcompat.app.AlertDialog
import com.example.gostudy.CoursesRepository
import com.example.gostudy.models.Course


class CourseDetailsFragment : Fragment() {

    private var courseIndex: Int = -1
    private lateinit var course: Course

    private lateinit var tvTitle: TextView
    private lateinit var tvTasksSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvTasks: RecyclerView
    private lateinit var btnAddTask: Button

    private val tasksList: MutableList<Task>
        get() = course.tasks

    private var completedCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseIndex = it.getInt(ARG_COURSE_INDEX)
        }

        course = CoursesRepository.courses[courseIndex]
        completedCount = course.completedTasks
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
        tvTasksSummary = view.findViewById(R.id.tvTasksSummary)
        progressBar = view.findViewById(R.id.progressCourseDetail)
        rvTasks = view.findViewById(R.id.rvTasks)
        btnAddTask = view.findViewById(R.id.btnAddTask)

        tvTitle.text = course.name

        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = TasksAdapter(tasksList, ::onTaskToggled)

        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        updateProgressUI()
    }

    private fun onTaskToggled(position: Int, isChecked: Boolean) {
        if (position < 0 || position >= tasksList.size) return

        tasksList[position].isDone = isChecked
        completedCount = course.completedTasks
        updateProgressUI()
        rvTasks.adapter?.notifyItemChanged(position)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressUI() {
        val total = course.totalTasks
        val percent = course.progressPercent
        completedCount = course.completedTasks

        tvTasksSummary.text = "$completedCount of $total tasks done ($percent%)"
        progressBar.progress = percent
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.add_task, null)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->

                val name = etTaskName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Task name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newTask = Task(name = name, isDone = false)
                tasksList.add(newTask)

                rvTasks.adapter?.notifyItemInserted(tasksList.size - 1)
                updateProgressUI()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    companion object {
        private const val ARG_COURSE_INDEX = "arg_course_index"

        fun newInstance(index: Int) =
            CourseDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COURSE_INDEX, index)
                }
            }
    }
}

