package com.example.finaltodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltodo.R
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), TaskAdapter.TaskItemListener {

    private val TAG = "MainActivity"
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var editTextTaskTitle: EditText
    private lateinit var buttonAddTask: Button
    private lateinit var buttonSelectDateTime: Button

    // Sample task list (in a real app, this would come from a database)
    private val taskList = mutableListOf<Task>()

    // Calendar for date/time selection
    private val calendar = Calendar.getInstance()
    private var selectedDueDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        buttonSelectDateTime = findViewById(R.id.buttonSelectDateTime)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up add task button
        setupAddTaskButton()

        // Set up date/time selector button
        setupDateTimeButton()

        // Log app startup
        Log.i(TAG, "Todo List App Started")
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(taskList)
        taskAdapter.setTaskItemListener(this)

        recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Add some sample tasks for testing
        addSampleTasks()
    }

    private fun addSampleTasks() {
        // Add a few sample tasks to see how the list works
        val calendar = java.util.Calendar.getInstance()

        // Task 1: Due today
        taskList.add(Task(1, "Buy groceries", dueDate = calendar.time))

        // Task 2: Due tomorrow
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        taskList.add(Task(2, "Finish homework", dueDate = calendar.time))

        // Task 3: Due in three days
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 2)
        taskList.add(Task(3, "Go to gym", dueDate = calendar.time))

        taskAdapter.notifyDataSetChanged()

        Log.d(TAG, "Added sample tasks with due dates")
    }

    private fun setupAddTaskButton() {
        buttonAddTask.setOnClickListener {
            addNewTask()
        }
    }

    private fun setupDateTimeButton() {
        buttonSelectDateTime.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        // Get current date values
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Show date picker dialog
        DatePickerDialog(this, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            // Save the selected date
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            // Get current time values
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Show time picker dialog
            TimePickerDialog(this, { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                // Save the selected time
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0) // Reset seconds

                // Save the selected date/time
                selectedDueDate = calendar.time

                // Show confirmation toast
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", java.util.Locale.getDefault())
                Toast.makeText(
                    this,
                    "Due date set: ${dateFormat.format(selectedDueDate)}",
                    Toast.LENGTH_SHORT
                ).show()

                // Update button text
                buttonSelectDateTime.text = "Due: ${dateFormat.format(selectedDueDate)}"

                // Log the event
                Log.d(TAG, "Due date selected: ${dateFormat.format(selectedDueDate)}")
            }, hour, minute, true).show()
        }, year, month, day).show()
    }

    private fun addNewTask() {
        val taskTitle = editTextTaskTitle.text.toString().trim()

        if (taskTitle.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            return
        }

        // Use selected date or default to 24 hours from now
        val dueDate = selectedDueDate ?: run {
            val defaultCalendar = Calendar.getInstance()
            defaultCalendar.add(Calendar.DAY_OF_MONTH, 1)
            defaultCalendar.time
        }

        // Create a new task and add it to the adapter
        val newTask = Task(
            id = System.currentTimeMillis(), // Use timestamp as a simple ID
            title = taskTitle,
            dueDate = dueDate
        )

        taskAdapter.addTask(newTask)

        // Clear the input field and reset date selection
        editTextTaskTitle.text.clear()
        selectedDueDate = null
        buttonSelectDateTime.text = "Set Due Date/Time"

        // Log the event
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", java.util.Locale.getDefault())
        Log.i(TAG, "Task Added: $taskTitle with due date: ${dateFormat.format(dueDate)}")
        Toast.makeText(this, "Task Added with due date", Toast.LENGTH_SHORT).show()
    }

    // TaskAdapter.TaskItemListener implementations
    override fun onTaskCompleteClicked(position: Int) {
        taskAdapter.completeTask(position)

        // Get the task that was completed
        val task = taskList[position]
        val status = if (task.isCompleted) "completed" else "uncompleted"

        // Log the event
        Log.i(TAG, "Task $status: ${task.title}")
        Toast.makeText(
            this,
            "Task ${if (task.isCompleted) "completed" else "uncompleted"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onTaskDeleteClicked(position: Int) {
        // Log before deleting to keep the reference
        val taskToDelete = taskList[position]
        Log.i(TAG, "Task Deleted: ${taskToDelete.title}")

        taskAdapter.deleteTask(position)

        Toast.makeText(this, "Task Deleted", Toast.LENGTH_SHORT).show()
    }
}