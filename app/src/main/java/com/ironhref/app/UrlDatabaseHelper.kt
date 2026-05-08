package com.ironhref.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class UrlEntry(val id: Long = 0, val title: String, val url: String)

class UrlDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ironhref.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "urls"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_URL = "url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_URL TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUrl(entry: UrlEntry): Long {
        val values = ContentValues().apply {
            put(COL_TITLE, entry.title)
            put(COL_URL, entry.url)
        }
        return writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun deleteUrl(id: Long) {
        writableDatabase.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAllUrls() {
        writableDatabase.delete(TABLE_NAME, null, null)
    }

    fun getAllUrls(): List<UrlEntry> {
        val list = mutableListOf<UrlEntry>()
        val cursor = readableDatabase.query(
            TABLE_NAME, null, null, null, null, null, "$COL_TITLE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    UrlEntry(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                        url = it.getString(it.getColumnIndexOrThrow(COL_URL))
                    )
                )
            }
        }
        return list
    }
}