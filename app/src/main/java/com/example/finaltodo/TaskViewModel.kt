package com.example.finaltodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository): ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getAllTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            loadTasks()
        }
    }

    fun sortTasksByDate() {
        _tasks.value = tasks.value?.sortedBy { it.dueDate }
    }


    fun sortTasksAlphabetically() {
        _tasks.value = tasks.value?.sortedBy { it.title }
    }

    fun sortTasksByPriority() {
        _tasks.value = tasks.value?.sortedBy { it.priority }
    }

}