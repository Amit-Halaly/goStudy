package com.example.gostudy

import com.example.gostudy.models.Course
import com.example.gostudy.models.Task

object CoursesRepository {

    val courses: MutableList<Course> = mutableListOf(
        Course(
            name = "Operating Systems",
            tasks = mutableListOf(
                Task("HW 1", true),
                Task("HW 2", false),
                Task("Exam practice", false)
            )
        ),
        Course(
            name = "Mobile Development",
            tasks = mutableListOf(
                Task("Watch lecture 1", true),
                Task("Finish project skeleton", false)
            )
        ),
        Course(
            name = "Linear Algebra",
            tasks = mutableListOf(
                Task("Solve tutorial 3", true),
                Task("Study for quiz", false)
            )
        )
    )
}