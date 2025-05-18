package com.example.finaltodo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.util.Date

class TaskRepostitory(context: Context) {
    private val dbHelper = TaskDataHelper(context)

    fun addTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title_en", task.titleEn)
            put("title_es", task.titleEs)
            put("title_vi", task.titleVi)
            put("description_en", task.descriptionEn)
            put("description_es", task.descriptionEs)
            put("description_vi", task.descriptionVi)
            put(TaskDataHelper.COLUMN_COMPLETED, if (task.completed) 1 else 0)
            put(TaskDataHelper.COLUMN_DUE_DATE, task.dueDate?.time)
            put(TaskDataHelper.COLUMN_COMPLETED_DATE, task.completedDate?.time)
        }
        db.insert(TaskDataHelper.TABLE_NAME, null, values)
    }

    fun getAllTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            TaskDataHelper.TABLE_NAME,
            null, null, null, null, null, null
        )
        val tasks = mutableListOf<Task>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(TaskDataHelper.COLUMN_ID))
                val titleEn = getString(getColumnIndexOrThrow("title_en")) ?: ""
                val titleEs = getString(getColumnIndexOrThrow("title_es")) ?: ""
                val titleVi = getString(getColumnIndexOrThrow("title_vi")) ?: ""
                val descriptionEn = getString(getColumnIndexOrThrow("description_en")) ?: ""
                val descriptionEs = getString(getColumnIndexOrThrow("description_es")) ?: ""
                val descriptionVi = getString(getColumnIndexOrThrow("description_vi")) ?: ""
                val completed = getInt(getColumnIndexOrThrow(TaskDataHelper.COLUMN_COMPLETED)) == 1
                val dueDate = getLong(getColumnIndexOrThrow(TaskDataHelper.COLUMN_DUE_DATE)).let {
                    if (it != 0L) Date(it) else null
                }
                
                // Get completedDate - safely handle case where column might not exist in older DB versions
                val completedDate = try {
                    val completedDateIndex = getColumnIndexOrThrow(TaskDataHelper.COLUMN_COMPLETED_DATE)
                    getLong(completedDateIndex).let {
                        if (it != 0L) Date(it) else null
                    }
                } catch (e: Exception) {
                    null
                }
                
                tasks.add(Task(id, titleEn, titleEs, titleVi, descriptionEn, descriptionEs, descriptionVi, completed, dueDate, completedDate))
            }
        }
        cursor.close()
        return tasks
    }

    fun updateTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title_en", task.titleEn)
            put("title_es", task.titleEs)
            put("title_vi", task.titleVi)
            put("description_en", task.descriptionEn)
            put("description_es", task.descriptionEs)
            put("description_vi", task.descriptionVi)
            put(TaskDataHelper.COLUMN_COMPLETED, if (task.completed) 1 else 0)
            put(TaskDataHelper.COLUMN_DUE_DATE, task.dueDate?.time)
            put(TaskDataHelper.COLUMN_COMPLETED_DATE, task.completedDate?.time)
        }
        db.update(TaskDataHelper.TABLE_NAME, values, "${TaskDataHelper.COLUMN_ID} = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(task: Task) {
        val db = dbHelper.writableDatabase
        db.delete(TaskDataHelper.TABLE_NAME, "${TaskDataHelper.COLUMN_ID} = ?", arrayOf((task.id.toString())))
    }
}