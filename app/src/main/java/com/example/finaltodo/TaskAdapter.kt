package com.example.finaltodo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private var tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onCompleteStatusChanged: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
    ) {
        val checkBox: CheckBox        = itemView.findViewById(R.id.checkboxTaskCompleted)
        val titleView: TextView       = itemView.findViewById(R.id.textViewTaskTitle)
        val dueView: TextView         = itemView.findViewById(R.id.textViewDue)
        val countdownView: TextView   = itemView.findViewById(R.id.textViewCountdown)
        val deleteButton: MaterialButton =
            itemView.findViewById(R.id.imageButtonDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(parent)

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Clear previous listeners to prevent duplicate events
        holder.checkBox.setOnCheckedChangeListener(null)
        
        // Set UI state
        holder.titleView.text = task.title
        holder.checkBox.isChecked = task.completed

        holder.dueView.text = task.dueDate?.let {
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(it)
        } ?: "No due date"

        holder.countdownView.text = task.dueDate?.let { due ->
            val diff = due.time - System.currentTimeMillis()
            if (diff > 0) {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                "${days}d ${hours}h left"
            } else {
                "Overdue!"
            }
        } ?: ""

        // Set listeners after UI is configured
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            // Only update if the status actually changed
            if (task.completed != checked) {
                // Create a copy with updated status instead of modifying original
                val updatedTask = task.copy(completed = checked)
                val status = if (checked) "Completed" else "Uncompleted"
                Log.i("FinalTodoApp", "Task $status: ${task.title}")

                // Post the update to happen after the current layout pass
                holder.itemView.post {
                    onCompleteStatusChanged(updatedTask)
                }
            }
        }

        holder.deleteButton.setOnClickListener {
            // Log task deletion
            Log.w("FinalTodoApp", "Task Deleted: ${task.title}")
            // Also post delete operations
            holder.itemView.post {
                onDeleteClick(task)
            }
        }

        holder.itemView.setOnClickListener {
            // Post edit operations too for consistency
            holder.itemView.post {
                onEditClick(task)
            }
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    fun deleteCompletedTasks() {
        // Get all completed tasks
        val completedTasks = tasks.filter { it.completed }
        
        // Skip if no completed tasks
        if (completedTasks.isEmpty()) return
        
        // Log batch deletion
        Log.w("FinalTodoApp", "Deleting ${completedTasks.size} completed tasks")
        
        // Delete each completed task
        completedTasks.forEach { task ->
            onDeleteClick(task)
        }
    }
    
    fun selectAllTasks() {
        // Get all tasks that are not completed
        val uncompletedTasks = tasks.filter { !it.completed }
        
        // Skip if no uncompleted tasks
        if (uncompletedTasks.isEmpty()) return
        
        // Log how many tasks we're selecting
        Log.i("FinalTodoApp", "Selecting all ${uncompletedTasks.size} uncompleted tasks")
        
        // Mark each task as completed
        uncompletedTasks.forEach { task ->
            val updatedTask = task.copy(completed = true)
            onCompleteStatusChanged(updatedTask)
        }
    }
}
