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
import com.example.gostudy.repositories.CoursesRepository
import com.example.gostudy.models.Course
import com.google.firebase.auth.FirebaseAuth


class CourseDetailsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        course = CoursesRepository.courses[courseIndex]
        completedCount = course.completedTasks
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_details, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnDelete = view.findViewById<Button>(R.id.btnDeleteCourse)

        btnDelete.setOnClickListener {
            confirmDeleteCourse()
        }

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

        val uid = auth.currentUser?.uid
        if (uid != null) {
            CoursesRepository.loadTasksForCourse(uid, course) {
                rvTasks.adapter?.notifyDataSetChanged()
                updateProgressUI()
            }
        } else {
            updateProgressUI()
        }
    }


    private fun onTaskToggled(position: Int, isChecked: Boolean) {
        if (position < 0 || position >= tasksList.size) return

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoursesRepository.updateTaskStatus(uid, course, position, isChecked)

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

                val uid = auth.currentUser?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                CoursesRepository.addTaskToCourse(uid, course, name) {
                    rvTasks.adapter?.notifyItemInserted(tasksList.size - 1)
                    updateProgressUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun confirmDeleteCourse() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteCourse()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCourse() {
        val uid = auth.currentUser?.uid ?: return

        CoursesRepository.deleteCourse(uid, course) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Course deleted", Toast.LENGTH_SHORT).show()

                CoursesRepository.courses.removeAt(courseIndex)

                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Failed to delete course", Toast.LENGTH_SHORT).show()
            }
        }
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

