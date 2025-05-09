package com.example.finaltodo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class TaskRepostitory(context: Context) {
    private val dbHelper = TaskDataHelper(context)

    fun addTask(task: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TaskDataHelper.COLUMN_TASK, task)
        }
        db.insert(TaskDataHelper.TABLE_NAME, null, values)
    }

    fun getAllTasks(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            TaskDataHelper.TABLE_NAME,
            arrayOf(TaskDataHelper.COLUMN_TASK),
            null, null, null, null, null
        )
        val tasks = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val task = getString(getColumnIndexOrThrow(TaskDataHelper.COLUMN_TASK))
                tasks.add(task)
            }
        }
        cursor.close()
        return tasks
    }
}