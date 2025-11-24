package com.example.gostudy

data class Course(
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int
) {
    val tasksLeft: Int
        get() = (totalTasks - completedTasks).coerceAtLeast(0)

    val progressPercent: Int
        get() = if (totalTasks == 0) 0 else (completedTasks * 100 / totalTasks)
}

