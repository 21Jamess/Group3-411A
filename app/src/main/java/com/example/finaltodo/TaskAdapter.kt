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

        holder.titleView.text = task.title
        holder.checkBox.apply {
            isChecked = task.completed
            setOnCheckedChangeListener { _, checked ->
                // Only update if the status actually changed
                if (task.completed != checked) {
                    task.completed = checked
                    // Log task completion status change
                    val status = if (checked) "Completed" else "Uncompleted"
                    Log.i("FinalTodoApp", "Task $status: ${task.title}")

                    // Notify listener to save the updated status
                    onCompleteStatusChanged(task)
                }
            }
        }

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

        holder.deleteButton.setOnClickListener {
            // Log task deletion
            Log.w("FinalTodoApp", "Task Deleted: ${task.title}")
            onDeleteClick(task)
        }

        holder.itemView.setOnClickListener {
            onEditClick(task)
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
