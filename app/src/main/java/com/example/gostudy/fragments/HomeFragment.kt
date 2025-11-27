package com.example.gostudy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.gostudy.repositories.CoursesRepository
import com.example.gostudy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvGpaValue: TextView
    private lateinit var tvNextTask: TextView
    private lateinit var tvStudyTime: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var currentGpa: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // זה ה-XML ששלחת
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        tvGpaValue = view.findViewById(R.id.tvGpaValue)
        tvNextTask = view.findViewById(R.id.tvNextTask)
        tvStudyTime = view.findViewById(R.id.tvStudyTime)

        tvGpaValue.setOnClickListener {
            showEditGpaDialog()
        }

        loadGpa()

        updateHomeFromCourses()

        val uid = auth.currentUser?.uid
        if (uid != null && CoursesRepository.courses.isEmpty()) {
            CoursesRepository.loadCoursesForUser(uid) {
                updateHomeFromCourses()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateHomeFromCourses()
    }

    @SuppressLint("SetTextI18n")
    private fun updateHomeFromCourses() {
        val courses = CoursesRepository.courses

        if (courses.isEmpty()) {
            tvNextTask.text =  "No tasks yet"
            tvStudyTime.text = "No study stats yet. Add your first course!"
            return
        }

        var totalCourses = courses.size
        var totalTasks = 0
        var completedTasks = 0

        var nextTaskText: String? = null

        for (course in courses) {
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

        tvStudyTime.text = "Courses: $totalCourses • Tasks: $totalTasks • Done: $completedTasks"
    }

    @SuppressLint("DefaultLocale")
    private fun loadGpa() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val gpa = doc.getDouble("gpa")
                currentGpa = gpa
                tvGpaValue.text = gpa?.let { String.format("%.2f", it) } ?: getString(R.string.gpa_value)
            }
            .addOnFailureListener {
                tvGpaValue.text = getString(R.string.gpa_value)
            }
    }


    @SuppressLint("DefaultLocale")
    private fun showEditGpaDialog() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter GPA (0 - 100)"
        currentGpa?.let { input.setText(it.toString()) }

        AlertDialog.Builder(requireContext())
            .setTitle("Update GPA")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    Toast.makeText(requireContext(), "GPA is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newGpa = text.toDoubleOrNull()
                if (newGpa == null || newGpa < 0 || newGpa > 100) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid GPA between 0 and 100",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                db.collection("users")
                    .document(uid)
                    .update("gpa", newGpa)
                    .addOnSuccessListener {
                        currentGpa = newGpa
                        tvGpaValue.text = String.format("%.2f", newGpa)
                        Toast.makeText(requireContext(), "GPA updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update GPA", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
