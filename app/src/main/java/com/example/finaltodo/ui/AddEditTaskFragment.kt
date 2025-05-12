package com.example.finaltodo.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.finaltodo.R
import com.example.finaltodo.Task
import com.example.finaltodo.TaskRepostitory
import com.example.finaltodo.TaskViewModel
import com.example.finaltodo.TaskViewModelFactory
import com.example.finaltodo.databinding.FragmentAddEditTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private var taskToEdit: Task? = null
    private val taskViewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(TaskRepostitory(requireContext()))
    }

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get task from arguments if it exists
        taskToEdit = arguments?.getSerializable("task") as? Task

        Log.d("AddEditTaskFragment", "Fragment view created, editing task: ${taskToEdit != null}")

        // Fill form with existing task data if editing
        taskToEdit?.let { task ->
            binding.editTextTitle.setText(task.title)
            binding.editTextNote.setText(task.description)
            task.dueDate?.let { date ->
                calendar.time = date
                updateDateTimeText()
            }
        }

        // Set up date picker
        binding.buttonPickDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    updateDateTimeText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set up time picker
        binding.buttonPickTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    updateDateTimeText()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        // Save task
        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun updateDateTimeText() {
        binding.textViewDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            .format(calendar.time)
        binding.textViewTime.text = SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(calendar.time)
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextNote.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.title_cannot_be_empty), Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AddEditTaskFragment", "Saving task: $title")

        if (taskToEdit == null) {
            // Create new task
            val newTask = Task(
                title = title,
                description = description,
                completed = false,
                dueDate = calendar.time,
                priority = 0
            )
            taskViewModel.addTask(newTask)
        } else {
            // Update existing task
            val updatedTask = taskToEdit!!.copy(
                title = title,
                description = description,
                dueDate = calendar.time
            )
            taskViewModel.updateTask(updatedTask)
        }

        // Navigate back to task list
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Task?): AddEditTaskFragment {
            val fragment = AddEditTaskFragment()
            val args = Bundle()
            if (task != null) {
                args.putSerializable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}