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
import com.example.gostudy.Task
import com.example.gostudy.R
import com.example.gostudy.TasksAdapter
import androidx.appcompat.app.AlertDialog

class CourseDetailsFragment : Fragment() {

    private lateinit var btnAddTask: Button
    private lateinit var tvTasksSummary: TextView
    private lateinit var progressBar: ProgressBar

    private val tasksList = mutableListOf<Task>()
    private var completedCount: Int = 0

    private var courseName: String? = null
    private var totalTasks: Int = 0
    private var completedTasks: Int = 0

    private lateinit var tvTitle: TextView
    private lateinit var rvTasks: RecyclerView


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
        tvTasksSummary = view.findViewById(R.id.tvTasksSummary)
        progressBar = view.findViewById(R.id.progressCourseDetail)
        rvTasks = view.findViewById(R.id.rvTasks)
        btnAddTask = view.findViewById(R.id.btnAddTask)

        tvTitle.text = courseName ?: "Course Details"

        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = TasksAdapter(tasksList, ::onTaskToggled)

        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        updateProgressUI()
    }


    private fun generateTasks() {
        tasksList.clear()
        for (i in 1..totalTasks) {
            val isDone = i <= completedTasks
            tasksList.add(Task("Task $i", isDone))
        }
        completedCount = tasksList.count { it.isDone }
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

    private fun onTaskToggled(position: Int, isChecked: Boolean) {
        if (position < 0 || position >= tasksList.size) return

        tasksList[position].isDone = isChecked
        completedCount = tasksList.count { it.isDone }
        updateProgressUI()

        rvTasks.adapter?.notifyItemChanged(position)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressUI() {
        val total = tasksList.size
        val percent = if (total == 0) 0 else (completedCount * 100 / total)
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

}
