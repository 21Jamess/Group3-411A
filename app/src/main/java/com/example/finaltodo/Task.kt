package com.example.finaltodo

import java.io.Serializable
import java.util.Date
// This is a test
data class Task(
    val id: Int = 0,
    var titleEn: String,
    var titleEs: String,
    var titleVi: String,
    var descriptionEn: String,
    var descriptionEs: String,
    var descriptionVi: String,
    var completed: Boolean,
    var dueDate: Date?,
    var completedDate: Date? = null,
    var priority: Int = 0
) : Serializable {
    fun getLocalizedTitle(language: String): String = when (language) {
        "es" -> titleEs
        "vi" -> titleVi
        else -> titleEn
    }

    fun getLocalizedDescription(language: String): String = when (language) {
        "es" -> descriptionEs
        "vi" -> descriptionVi
        else -> descriptionEn
    }
}
