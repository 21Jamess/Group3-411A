package com.example.realtodo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltodo.R

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var editTextTaskTitle: EditText
    private lateinit var buttonAddTask: Button

    // Sample task list (in a real app, this would come from a database)
    private val taskList = mutableListOf<Task>()

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

        // Set up RecyclerView
        setupRecyclerView()

        // Log app startup
        Log.i(TAG, "Todo List App Started")
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(taskList)

        recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Add some sample tasks for testing
        addSampleTasks()
    }

    private fun addSampleTasks() {
        // Add a few sample tasks to see how the list works
        taskList.add(Task(1, "Buy groceries"))
        taskList.add(Task(2, "Finish homework"))
        taskList.add(Task(3, "Go to gym"))

        taskAdapter.notifyDataSetChanged()

        Log.d(TAG, "Added sample tasks")
    }

    private fun setupAddTaskButton() {
        buttonAddTask.setOnClickListener {
            addNewTask()
        }
    }

    private fun addNewTask() {
        val taskTitle = editTextTaskTitle.text.toString().trim()

        if (taskTitle.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new task and add it to the adapter
        val newTask = Task(
            id = System.currentTimeMillis(), // Use timestamp as a simple ID
            title = taskTitle
        )

        taskAdapter.addTask(newTask)

        // Clear the input field
        editTextTaskTitle.text.clear()

        // Log the event
        Log.i(TAG, "Task Added: $taskTitle")
        Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show()
    }
}