package com.example.finaltodo.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        adapter = TaskAdapter(
            taskViewModel.tasks.value ?: emptyList(),
            onDeleteClick = { task ->
                Log.d("TaskListFragment", "Delete task: ${task.title}")
                taskViewModel.deleteTask(task)
            },
            onEditClick = { task ->
                Log.d("TaskListFragment", "Edit task: ${task.title}")
                // Navigate to AddEditTaskFragment with task
                val bundle = Bundle().apply {
                    putSerializable("task", task)
                }
                findNavController().navigate(R.id.addEditTaskFragment, bundle)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

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

