package com.example.realtodo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltodo.R

class TaskAdapter(private val tasks: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val TAG = "TaskAdapter"

    // ViewHolder class for task items
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        val completeCheckBox: CheckBox = itemView.findViewById(R.id.checkboxTaskCompleted)
        val deleteButton: ImageButton = itemView.findViewById(R.id.imageButtonDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = tasks[position]

        // Bind data to views
        holder.titleTextView.text = currentTask.title
        holder.completeCheckBox.isChecked = currentTask.isCompleted
    }

    override fun getItemCount() = tasks.size

    // Method to add a new task
    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
        Log.d(TAG, "Task Added: ${task.title}")
    }
}