package com.example.gostudy.models

import com.example.gostudy.models.Task

data class Course(
    val name: String,
    val tasks: MutableList<Task> = mutableListOf()
) {
    val totalTasks: Int
        get() = tasks.size

    val completedTasks: Int
        get() = tasks.count { it.isDone }

    val tasksLeft: Int
        get() = tasks.count { !it.isDone }

    val progressPercent: Int
        get() = if (tasks.isEmpty()) 0 else (completedTasks * 100 / tasks.size)
}