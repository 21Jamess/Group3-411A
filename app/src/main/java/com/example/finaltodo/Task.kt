package com.example.finaltodo

import java.io.Serializable
import java.util.Date

data class Task(
    val id: Long = 0,
    var title: String,
    var description: String = "",
    var completed: Boolean = false,
    var dueDate: Date? = null,
    var priority: Int = 0
) : Serializable
