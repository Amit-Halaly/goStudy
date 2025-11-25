package com.example.gostudy.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.R
import com.example.gostudy.models.Course

class CoursesAdapter(
    private val courseList: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCourseName)
        val tvTasks: TextView = itemView.findViewById(R.id.tvTasksInfo)
        val progress: ProgressBar = itemView.findViewById(R.id.progressCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val item = courseList[position]

        holder.tvName.text = item.name
        holder.tvTasks.text = "${item.tasksLeft} tasks left"
        holder.progress.max = 100
        holder.progress.progress = item.progressPercent

        holder.itemView.setOnClickListener {
            onCourseClick(item)
        }
    }

    override fun getItemCount() = courseList.size
}
