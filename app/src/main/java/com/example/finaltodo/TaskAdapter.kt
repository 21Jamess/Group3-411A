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
import java.util.Date
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

        // Display countdown or completed date based on task status
        if (task.completed && task.completedDate != null) {
            // Show completion date for completed tasks
            holder.countdownView.text = "Completed: ${SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(task.completedDate!!)}"
        } else {
            // Show countdown for incomplete tasks or if completedDate is null
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
        }

        // Set listeners after UI is configured
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            // Only update if the status actually changed
            if (task.completed != checked) {
                // Create a copy with updated status and completedDate if checked
                val updatedTask = if (checked) {
                    // Set completedDate to current time when checked
                    task.copy(completed = true, completedDate = Date())
                } else {
                    // Clear completedDate when unchecked
                    task.copy(completed = false, completedDate = null)
                }
                
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
        // Count uncompleted tasks
        val uncompletedTasks = tasks.filter { !it.completed }
        
        if (uncompletedTasks.isEmpty() && tasks.isNotEmpty()) {
            // All tasks are already completed, so deselect all
            Log.i("FinalTodoApp", "Deselecting all ${tasks.size} tasks")
            
            // Mark each task as uncompleted
            tasks.forEach { task ->
                val updatedTask = task.copy(completed = false, completedDate = null)
                onCompleteStatusChanged(updatedTask)
            }
        } else {
            // There are uncompleted tasks, so select all
            Log.i("FinalTodoApp", "Selecting all ${uncompletedTasks.size} uncompleted tasks")
            
            // Mark each uncompleted task as completed with current timestamp
            val now = Date()
            uncompletedTasks.forEach { task ->
                val updatedTask = task.copy(completed = true, completedDate = now)
                onCompleteStatusChanged(updatedTask)
            }
        }
    }
}
