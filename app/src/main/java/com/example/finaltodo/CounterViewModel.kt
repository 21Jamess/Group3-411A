package com.example.finaltodo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CounterViewModel : ViewModel() {

    private val LOG_TAG = "com.example.finaltodo.CounterViewModel"

    private val _tasks = MutableLiveData<MutableList<Task>>(mutableListOf())

    val tasks: LiveData<MutableList<Task>> = _tasks


    fun addTask(task: Task) {
        val currentList = _tasks.value ?: mutableListOf()
        currentList.add(task)
        _tasks.value = currentList
        Log.d(LOG_TAG, "Task added: ${task.title}")
    }

    fun removeTask(position: Int) {
        val currentList = _tasks.value
        currentList?.let {
            if (position in it.indices) {
                Log.d(LOG_TAG, "Task removed: ${it[position].title}")
                it.removeAt(position)
                _tasks.value = it
            }
        }
    }

    fun toggleTaskComplete(position: Int) {
        val currentList = _tasks.value
        currentList?.let {
            if (position in it.indices) {
                it[position].isCompleted = !it[position].isCompleted
                _tasks.value = it
                Log.d(LOG_TAG, "Task toggled: ${it[position].title} -> ${it[position].isCompleted}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(LOG_TAG, "ViewModel cleared")
    }
}

