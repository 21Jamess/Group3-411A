package com.example.finaltodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TaskViewModelFactory(private val repostitory: TaskRepostitory) : ViewModelProvider.Factory {
     fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repostitory) as T
        }
        throw IllegalArgumentException("Unkown ViewModel class")
    }
}