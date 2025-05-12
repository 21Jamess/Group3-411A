package com.example.finaltodo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.finaltodo.Task
import com.example.finaltodo.TaskAdapter
import com.example.finaltodo.databinding.FragmentTaskBinding

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    // Use CounterViewModel instead of TaskViewModel
    private val counterViewModel: CounterViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure CounterViewModel provides a list of tasks
        adapter = TaskAdapter(
            counterViewModel.tasks.value ?: mutableListOf(),  // Using CounterViewModel
            onDeleteClick = { task ->
                val index = counterViewModel.tasks.value?.indexOf(task) ?: -1
                if (index != -1) counterViewModel.removeTask(index)
            },
            onEditClick = {
                // Navigate to AddEditTaskFragment
                findNavController().navigate(
                    TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment(it)
                )
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe tasks from CounterViewModel
        counterViewModel.tasks.observe(viewLifecycleOwner) {
            // Ensure the list is non-null and submit it to the adapter
            adapter.submitList(it)  // Submit updated list to adapter
        }

        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment(null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

