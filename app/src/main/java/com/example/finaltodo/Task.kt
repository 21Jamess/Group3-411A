package com.example.realtodo

import java.util.Date

/**
 * Data class representing a Task in the to-do list
 * @param id Unique identifier for the task
 * @param title Title of the task
 * @param description Optional description of the task
 * @param isCompleted Status of the task (completed or not)
 * @param dueDate Optional due date for the task
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    var isCompleted: Boolean = false,
    val dueDate: Date? = null
)