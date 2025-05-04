package com.example.finaltodo

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

    // Interface for click listeners
    interface TaskItemListener {
        fun onTaskCompleteClicked(position: Int)
        fun onTaskDeleteClicked(position: Int)
    }

    private var listener: TaskItemListener? = null

    fun setTaskItemListener(listener: TaskItemListener) {
        this.listener = listener
    }

    // ViewHolder class for task items
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        val dueDateTextView: TextView = itemView.findViewById(R.id.textViewDueDate)
        val completeCheckBox: CheckBox = itemView.findViewById(R.id.checkboxTaskCompleted)
        val deleteButton: ImageButton = itemView.findViewById(R.id.imageButtonDeleteTask)

        init {
            // Set up click listeners
            completeCheckBox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onTaskCompleteClicked(position)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onTaskDeleteClicked(position)
                }
            }
        }
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

        // Format and display the due date if available
        if (currentTask.dueDate != null) {
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", java.util.Locale.getDefault())
            holder.dueDateTextView.text = "Due: ${dateFormat.format(currentTask.dueDate)}"
            holder.dueDateTextView.visibility = View.VISIBLE
        } else {
            holder.dueDateTextView.visibility = View.GONE
        }
    }

    override fun getItemCount() = tasks.size

    // Methods to modify the task list
    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
        Log.d(TAG, "Task Added: ${task.title}")
    }

    fun completeTask(position: Int) {
        if (position in 0 until tasks.size) {
            tasks[position].isCompleted = !tasks[position].isCompleted
            notifyItemChanged(position)
            Log.d(TAG, "Task Completed: ${tasks[position].title}")
        }
    }

    fun deleteTask(position: Int) {
        if (position in 0 until tasks.size) {
            val deletedTask = tasks[position]
            tasks.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, tasks.size)
            Log.d(TAG, "Task Deleted: ${deletedTask.title}")
        }
    }
}