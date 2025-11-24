package com.example.gostudy

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskToggle: (position: Int, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbTask: CheckBox = itemView.findViewById(R.id.cbTask)
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = tasks[position]

        holder.cbTask.setOnCheckedChangeListener(null)

        holder.tvTaskName.text = item.name
        holder.cbTask.isChecked = item.isDone

        holder.cbTask.setOnCheckedChangeListener { _, isChecked ->
            onTaskToggle(holder.adapterPosition, isChecked)
        }

        if (item.isDone) {
            holder.tvTaskName.paintFlags =
                holder.tvTaskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTaskName.paintFlags =
                holder.tvTaskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }


    override fun getItemCount() = tasks.size
}
