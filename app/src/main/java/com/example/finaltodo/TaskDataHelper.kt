package com.example.finaltodo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDataHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 3
        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_COMPLETED = "completed"
        const val COLUMN_DUE_DATE = "due_date"
        const val COLUMN_COMPLETED_DATE = "completed_date"
        const val COLUMN_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                title_en TEXT,
                title_es TEXT,
                title_vi TEXT,
                description_en TEXT,
                description_es TEXT,
                description_vi TEXT,
                $COLUMN_COMPLETED INTEGER,
                $COLUMN_DUE_DATE INTEGER,
                $COLUMN_COMPLETED_DATE INTEGER,
                priority INTEGER DEFAULT 0
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN title_en TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN title_es TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN title_vi TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN description_en TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN description_es TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN description_vi TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN priority INTEGER DEFAULT 0")
        }
    }
}