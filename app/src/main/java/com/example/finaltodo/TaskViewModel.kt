package com.example.finaltodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repostitory: TaskRepostitory): ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks
    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repostitory.getAllTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repostitory.addTask(task)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repostitory.updateTask(task)
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repostitory.deleteTask(task)
            loadTasks()
        }
    }
}