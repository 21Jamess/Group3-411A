package com.example.finaltodo.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finaltodo.R
import com.example.finaltodo.Task
import com.example.finaltodo.TaskAdapter
import com.example.finaltodo.TaskViewModel
import com.example.finaltodo.TaskViewModelFactory
import com.example.finaltodo.TaskRepostitory
import com.example.finaltodo.api.QuoteExecutor
import com.example.finaltodo.databinding.FragmentTaskListBinding
import java.util.Locale

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(TaskRepostitory(requireContext()))
    }
    private lateinit var adapter: TaskAdapter
    private lateinit var quoteExecutor: QuoteExecutor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TaskListFragment", "Fragment view created")

        // Initialize QuoteExecutor for motivational quotes
        quoteExecutor = QuoteExecutor()
        loadMotivationalQuote()

        // Setup adapter
        val language = Locale.getDefault().language
        adapter = TaskAdapter(
            taskViewModel.tasks.value ?: emptyList(),
            onDeleteClick = { task ->
                Log.d("TaskListFragment", "Delete task: ${task.getLocalizedTitle(language)}")
                taskViewModel.deleteTask(task)
            },
            onEditClick = { task ->
                Log.d("TaskListFragment", "Edit task: ${task.getLocalizedTitle(language)}")
                // Navigate to AddEditTaskFragment with task
                val bundle = Bundle().apply {
                    putSerializable("task", task)
                }
                findNavController().navigate(R.id.addEditTaskFragment, bundle)
            },
            onCompleteStatusChanged = { task ->
                Log.d("TaskListFragment", "Task completion status changed: ${task.getLocalizedTitle(language)} - Completed: ${task.completed}")
                taskViewModel.updateTask(task)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Set up search functionality
        binding.editTextSearch.addTextChangedListener { text ->
            Log.d("TaskListFragment", "Search query changed: ${text?.toString() ?: ""}")
            taskViewModel.searchTasks(text?.toString() ?: "")
        }

        // Observe tasks from TaskViewModel
        taskViewModel.tasks.observe(viewLifecycleOwner, Observer { tasks ->
            Log.d("TaskListFragment", "Tasks updated: ${tasks.size} items")
            adapter.updateTasks(tasks)
        })

        binding.fabAddTask.setOnClickListener {
            Log.d("TaskListFragment", "Add new task button clicked")
            // Navigate to AddEditTaskFragment for new task
            findNavController().navigate(R.id.addEditTaskFragment)
        }

        binding.buttonDeleteCompleted.setOnClickListener {
            Log.d("TaskListFragment", "Delete completed tasks clicked")
            // Get the current completed tasks from the ViewModel
            val completedTasks = taskViewModel.tasks.value?.filter { it.completed } ?: emptyList()

            // Skip if no completed tasks
            if (completedTasks.isEmpty()) {
                Log.d("TaskListFragment", "No completed tasks to delete")
                return@setOnClickListener
            }

            // Log how many tasks we're deleting
            Log.d("TaskListFragment", "Deleting ${completedTasks.size} completed tasks")

            // Delete each completed task using the ViewModel
            completedTasks.forEach { task ->
                taskViewModel.deleteTask(task)
            }
        }

        // Add select all button click listener
        binding.buttonSelectAll.setOnClickListener {
            Log.d("TaskListFragment", "Select all tasks clicked")

            // Get uncompleted tasks count for logging
            val uncompletedTasksCount = taskViewModel.tasks.value?.count { !it.completed } ?: 0

            if (uncompletedTasksCount == 0) {
                Log.d("TaskListFragment", "No uncompleted tasks to select")
                return@setOnClickListener
            }

            Log.d("TaskListFragment", "Selecting all $uncompletedTasksCount uncompleted tasks")

            // Use adapter's select all function
            adapter.selectAllTasks()
        }

        // Load tasks
        taskViewModel.loadTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadMotivationalQuote() {
        // Set initial loading state
        binding.textViewMotivationalQuote.text = "Loading motivational quote..."
        binding.textViewQuoteAuthor.text = ""

        // Fetch a quote using the QuoteExecutor
        val quoteLiveData = quoteExecutor.fetchQuote()
        quoteLiveData.observe(viewLifecycleOwner, Observer { quote ->
            if (quote != null) {
                Log.d("TaskListFragment", "Quote received: ${quote.q} - ${quote.a}")
                binding.textViewMotivationalQuote.text = quote.q
                binding.textViewQuoteAuthor.text = "- ${quote.a}"
            } else {
                Log.e("TaskListFragment", "Failed to load quote")
                binding.textViewMotivationalQuote.text = "Focus on what matters most."
                binding.textViewQuoteAuthor.text = "- FinalTodo"
            }
        })
    }
}

