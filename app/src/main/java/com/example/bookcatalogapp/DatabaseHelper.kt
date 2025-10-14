package com.example.bookcatalogapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bookcatalogapp.models.VolumeItem

class DatabaseHelper(private val context: Context):
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val TABLE_BOOKS = "books"
        private const val COLUMN_BOOK_ID = "book_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AUTHORS = "authors"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_EMAIL TEXT, " +
                "$COLUMN_PASSWORD TEXT)")
        db?.execSQL(createTableQuery)

        val createBooksTable = ("CREATE TABLE $TABLE_BOOKS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_BOOK_ID TEXT, " +
                "$COLUMN_TITLE TEXT, " +
                "$COLUMN_AUTHORS TEXT, " +
                "$COLUMN_USERNAME TEXT)")
        db?.execSQL(createBooksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertUser(username: String, email: String, password: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val db = writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun getUserEmail(username: String): String? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_EMAIL), selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun insertBook(book: VolumeItem, username: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BOOK_ID, book.id)
            put(COLUMN_TITLE, book.volumeInfo.title ?: "")
            put(COLUMN_AUTHORS, book.volumeInfo.authors?.joinToString(", ") ?: "")
            put(COLUMN_USERNAME, username)
        }
        return db.insert(TABLE_BOOKS, null, values)
    }
}