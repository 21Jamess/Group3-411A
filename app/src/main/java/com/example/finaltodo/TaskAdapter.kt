package com.example.finaltodo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        val language = Locale.getDefault().language
        holder.titleView.text = task.getLocalizedTitle(language)
        holder.checkBox.isChecked = task.completed

        // Prefix "Due " to the formatted date/time
        holder.dueView.text = task.dueDate?.let {
            "Due ${SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(it)}"
        } ?: "Due: No due date"

        // Display countdown or completed date based on task status
        if (task.completed && task.completedDate != null) {
            // Show completion date for completed tasks (in green)
            val formatted = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                .format(task.completedDate!!)
            holder.countdownView.text = "Completed on $formatted"
            holder.countdownView.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
            )
        } else {
            // Show countdown or Overdue! (always in red)
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
            holder.countdownView.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
            )
        }

        // Restore listener after UI is set
        holder.checkBox.setOnCheckedChangeListener { _, checked ->
            if (task.completed != checked) {
                val updatedTask = if (checked) {
                    task.copy(completed = true, completedDate = Date())
                } else {
                    task.copy(completed = false, completedDate = null)
                }

                Log.i("FinalTodoApp", "${if (checked) "Completed" else "Uncompleted"}: ${task.getLocalizedTitle(language)}")
                holder.itemView.post { onCompleteStatusChanged(updatedTask) }
            }
        }

        holder.deleteButton.setOnClickListener {
            Log.w("FinalTodoApp", "Task Deleted: ${task.getLocalizedTitle(language)}")
            holder.itemView.post { onDeleteClick(task) }
        }

        holder.itemView.setOnClickListener {
            holder.itemView.post { onEditClick(task) }
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    fun deleteCompletedTasks() {
        val completedTasks = tasks.filter { it.completed }
        if (completedTasks.isEmpty()) return
        Log.w("FinalTodoApp", "Deleting ${completedTasks.size} completed tasks")
        completedTasks.forEach { task -> onDeleteClick(task) }
    }

    fun selectAllTasks() {
        val uncompletedTasks = tasks.filter { !it.completed }
        if (uncompletedTasks.isEmpty() && tasks.isNotEmpty()) {
            Log.i("FinalTodoApp", "Deselecting all ${tasks.size} tasks")
            tasks.forEach { task ->
                val updatedTask = task.copy(completed = false, completedDate = null)
                onCompleteStatusChanged(updatedTask)
            }
        } else {
            Log.i("FinalTodoApp", "Selecting all ${uncompletedTasks.size} uncompleted tasks")
            val now = Date()
            uncompletedTasks.forEach { task ->
                val updatedTask = task.copy(completed = true, completedDate = now)
                onCompleteStatusChanged(updatedTask)
            }
        }
    }
}
