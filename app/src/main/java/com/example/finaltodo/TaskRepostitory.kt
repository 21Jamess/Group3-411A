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
            put(TaskDataHelper.COLUMN_TITLE, task.title)
            put(TaskDataHelper.COLUMN_DESCRIPTION, task.description)
            put(TaskDataHelper.COLUMN_COMPLETED, if (task.completed) 1 else 0)
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
                val title = getString(getColumnIndexOrThrow(TaskDataHelper.COLUMN_TITLE))
                val description = getString(getColumnIndexOrThrow(TaskDataHelper.COLUMN_DESCRIPTION))
                val completed = getInt(getColumnIndexOrThrow(TaskDataHelper.COLUMN_COMPLETED)) == 1
                val dueDate = getLong(getColumnIndexOrThrow(TaskDataHelper.COLUMN_DUE_DATE)).let {
                    if (it != 0L) Date(it) else  null
                }
                tasks.add(Task(id, title, description, completed, dueDate))
            }
        }
        cursor.close()
        return tasks
    }

    fun updateTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TaskDataHelper.COLUMN_TITLE, task.title)
            put(TaskDataHelper.COLUMN_DESCRIPTION, task.description)
            put(TaskDataHelper.COLUMN_COMPLETED, if (task.completed) 1 else 0)
            put(TaskDataHelper.COLUMN_DUE_DATE, task.dueDate?.time)
        }
        db.update(TaskDataHelper.TABLE_NAME, values, "${TaskDataHelper.COLUMN_ID} + ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(task: Task) {
        val db = dbHelper.writableDatabase
        db.delete(TaskDataHelper.TABLE_NAME, "${TaskDataHelper.COLUMN_ID} = ?", arrayOf((task.id.toString())))
    }
}