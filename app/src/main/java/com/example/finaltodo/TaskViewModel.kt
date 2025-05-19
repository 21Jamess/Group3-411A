package com.example.finaltodo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

class TaskViewModel(private val repostitory: TaskRepostitory): ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks
    
    // Store original unfiltered tasks
    private var originalTasks: List<Task> = emptyList()
    
    fun loadTasks() {
        viewModelScope.launch {
            val loadedTasks = repostitory.getAllTasks()
            Log.d("TaskViewModel", "Loaded ${loadedTasks.size} tasks")
            originalTasks = loadedTasks
            _tasks.value = loadedTasks
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repostitory.addTask(task)
            Log.i("TaskViewModel", "Task added: ${task.getLocalizedTitle(Locale.getDefault().language)}")
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repostitory.updateTask(task)
            Log.i("TaskViewModel", "Task updated: ${task.getLocalizedTitle(Locale.getDefault().language)}")
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repostitory.deleteTask(task)
            Log.i("TaskViewModel", "Task deleted: ${task.getLocalizedTitle(Locale.getDefault().language)}")
            loadTasks()
        }
    }

    // Sorting functions
    fun sortTasksByDate() {
        _tasks.value?.let { currentTasks ->
            val sortedTasks = currentTasks.sortedBy { it.dueDate }
            Log.d("TaskViewModel", "Tasks sorted by date")
            _tasks.value = sortedTasks
        }
    }

    fun sortTasksAlphabetically() {
        val lang = Locale.getDefault().language
        _tasks.value?.let { currentTasks ->
            val sortedTasks = currentTasks.sortedBy { it.getLocalizedTitle((lang)) }
            Log.d("TaskViewModel", "Tasks sorted alphabetically")
            _tasks.value = sortedTasks
        }
    }

    fun sortTasksByPriority() {
        _tasks.value?.let { currentTasks ->
            val sortedTasks = currentTasks.sortedBy { it.priority }
            Log.d("TaskViewModel", "Tasks sorted by priority")
            _tasks.value = sortedTasks
        }
    }
    
    // Search function
    fun searchTasks(query: String) {
        val lang = Locale.getDefault().language
        if (query.isBlank()) {
            // If search is empty, show all tasks
            _tasks.value = originalTasks
            return
        }
        
        val lowercaseQuery = query.lowercase()
        val filteredTasks = originalTasks.filter { task ->
            task.getLocalizedTitle(lang).lowercase().contains(lowercaseQuery) ||
            task.getLocalizedDescription(lang).lowercase().contains(lowercaseQuery)
        }
        
        Log.d("TaskViewModel", "Search query: '$query', found ${filteredTasks.size} matching tasks")
        _tasks.value = filteredTasks
    }
}