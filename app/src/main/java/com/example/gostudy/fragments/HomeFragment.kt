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
import androidx.cardview.widget.CardView
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

    // כרטיסי הקורסים למטה
    private lateinit var cardCourse1: CardView
    private lateinit var cardCourse2: CardView
    private lateinit var cardCourse3: CardView

    private lateinit var tvCourse1Name: TextView
    private lateinit var tvCourse1Progress: TextView
    private lateinit var tvCourse2Name: TextView
    private lateinit var tvCourse2Progress: TextView
    private lateinit var tvCourse3Name: TextView
    private lateinit var tvCourse3Progress: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var currentGpa: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        tvGpaValue = view.findViewById(R.id.tvGpaValue)
        tvNextTask = view.findViewById(R.id.tvNextTask)
        tvStudyTime = view.findViewById(R.id.tvStudyTime)

        cardCourse1 = view.findViewById(R.id.cardCourse1)
        cardCourse2 = view.findViewById(R.id.cardCourse2)
        cardCourse3 = view.findViewById(R.id.cardCourse3)

        tvCourse1Name = view.findViewById(R.id.tvCourse1Name)
        tvCourse1Progress = view.findViewById(R.id.tvCourse1Progress)
        tvCourse2Name = view.findViewById(R.id.tvCourse2Name)
        tvCourse2Progress = view.findViewById(R.id.tvCourse2Progress)
        tvCourse3Name = view.findViewById(R.id.tvCourse3Name)
        tvCourse3Progress = view.findViewById(R.id.tvCourse3Progress)

        // לחיצה על ה-GPA – שינוי ושמירה ב-Firestore
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
            tvNextTask.text = "No tasks yet"
            tvStudyTime.text = "No study stats yet. Add your first course!"

            cardCourse1.visibility = View.GONE
            cardCourse2.visibility = View.GONE
            cardCourse3.visibility = View.GONE
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

        bindCourseCard(0, courses, cardCourse1, tvCourse1Name, tvCourse1Progress)
        bindCourseCard(1, courses, cardCourse2, tvCourse2Name, tvCourse2Progress)
        bindCourseCard(2, courses, cardCourse3, tvCourse3Name, tvCourse3Progress)
    }

    @SuppressLint("SetTextI18n")
    private fun bindCourseCard(
        index: Int,
        courses: List<com.example.gostudy.models.Course>,
        card: CardView,
        tvName: TextView,
        tvProgress: TextView
    ) {
        if (index >= courses.size) {
            card.visibility = View.GONE
            return
        }

        val course = courses[index]
        card.visibility = View.VISIBLE

        val left = course.tasksLeft
        val percent = course.progressPercent

        tvName.text = course.name
        tvProgress.text = "Progress: $percent% • tasks Left: $left"
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
