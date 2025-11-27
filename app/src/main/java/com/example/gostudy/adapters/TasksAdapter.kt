package com.example.gostudy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gostudy.R
import com.example.gostudy.models.Task

class TasksAdapter(
    private val tasks: List<Task>,
    private val onTaskToggled: (Int, Boolean) -> Unit,
    private val isReadOnly: Boolean = false
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvTaskName)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvName.text = task.name

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.isDone

        if (isReadOnly) {
            holder.cbDone.isEnabled = false
            holder.cbDone.isClickable = false
        } else {
            holder.cbDone.isEnabled = true
            holder.cbDone.isClickable = true

            holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onTaskToggled(adapterPosition, isChecked)
                }
            }
        }
    }

    override fun getItemCount(): Int = tasks.size
}
