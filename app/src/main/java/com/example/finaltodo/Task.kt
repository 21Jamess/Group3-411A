package com.example.finaltodo

import java.io.Serializable
import java.util.Date

data class Task(
    val id: Int = 0,
    var title: String,
    var description: String,
    var completed: Boolean,
    var dueDate: Date?,
    var priority: Int = 0
) : Serializable