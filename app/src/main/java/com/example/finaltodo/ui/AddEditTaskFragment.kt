package com.example.finaltodo.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.finaltodo.R
import com.example.finaltodo.Task
import com.example.finaltodo.TaskRepostitory
import com.example.finaltodo.TaskViewModel
import com.example.finaltodo.TaskViewModelFactory
import com.example.finaltodo.databinding.DialogTimePickerBinding
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
    private var timeSkipped = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskToEdit = arguments?.getSerializable("task") as? Task
        taskToEdit?.let { task ->
            binding.editTextTitle.setText(
                task.getLocalizedTitle(Locale.getDefault().language)
            )
            binding.editTextNote.setText(
                task.getLocalizedDescription(Locale.getDefault().language)
            )
            task.dueDate?.let { date ->
                calendar.time = date
                timeSkipped = date.hours == 0 && date.minutes == 0 && date.seconds == 0
                updateDueDateTimeLabel()
            }
        }

        binding.buttonPickDateTime.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    showCustomTimePicker(now)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun showCustomTimePicker(now: Calendar) {
        val dialogBinding = DialogTimePickerBinding.inflate(layoutInflater)
        val tp: TimePicker = dialogBinding.timePicker
        tp.setIs24HourView(false)
        tp.hour = calendar.get(Calendar.HOUR_OF_DAY)
        tp.minute = calendar.get(Calendar.MINUTE)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_time)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                timeSkipped = false
                calendar.set(Calendar.HOUR_OF_DAY, tp.hour)
                calendar.set(Calendar.MINUTE, tp.minute)
                calendar.set(Calendar.SECOND, 0)
                updateDueDateTimeLabel()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.skip_time) { _, _ ->
                timeSkipped = true
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                updateDueDateTimeLabel()
            }
            .show()

        // Expand dialog to full height so the clock isn't cut off in landscape
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun updateDueDateTimeLabel() {
        val fmt = if (timeSkipped) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        } else {
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        }
        binding.textViewDateTime.text = fmt.format(calendar.time)
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextNote.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.title_cannot_be_empty),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (taskToEdit == null) {
            val newTask = Task(
                titleEn = title,
                titleEs = title,
                titleVi = title,
                descriptionEn = description,
                descriptionEs = description,
                descriptionVi = description,
                completed = false,
                dueDate = calendar.time,
                priority = 0
            )
            taskViewModel.addTask(newTask)
        } else {
            val updated = taskToEdit!!.copy(
                titleEn = title,
                titleEs = title,
                titleVi = title,
                descriptionEn = description,
                descriptionEs = description,
                descriptionVi = description,
                dueDate = calendar.time,
                completed = taskToEdit!!.completed
            )
            taskViewModel.updateTask(updated)
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
