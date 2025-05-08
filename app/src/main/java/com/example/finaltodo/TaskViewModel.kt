package com.example.finaltodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repostitory: TaskRepostitory): ViewModel() {
    private val _tasks = MutableLiveData<List<String>>()
    val tasks: LiveData<List<String>> = _tasks
    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repostitory.getAllTasks()
        }
    }

    fun addTask(task: String) {
        viewModelScope.launch {
            repostitory.addTask(task)
            loadTasks()
        }
    }
}