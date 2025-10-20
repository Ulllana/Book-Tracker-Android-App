package com.example.bookcatalogapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bookcatalogapp.models.ImageLinks
import com.example.bookcatalogapp.models.VolumeInfo
import com.example.bookcatalogapp.models.VolumeItem

class DatabaseHelper(private val context: Context):
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val TABLE_BOOKS = "books"
        private const val COLUMN_BOOK_ID = "book_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AUTHORS = "authors"
        private const val COLUMN_THUMBNAIL = "thumbnail"
        private const val TABLE_LISTS = "lists"
        private const val COLUMN_LIST_ID = "list_id"
        private const val COLUMN_LIST_NAME = "list_name"
        private const val TABLE_BOOK_LISTS = "book_lists"
        private const val COLUMN_BL_ID = "bl_id"
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
                "$COLUMN_USERNAME TEXT, " +
                "UNIQUE($COLUMN_BOOK_ID, $COLUMN_USERNAME))")
        db?.execSQL(createBooksTable)

        val createListsTable = ("CREATE TABLE $TABLE_LISTS (" +
                "$COLUMN_LIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_LIST_NAME TEXT, " +
                "$COLUMN_USERNAME TEXT)")
        db?.execSQL(createListsTable)

        val createBookListsTable = ("CREATE TABLE $TABLE_BOOK_LISTS (" +
                "$COLUMN_BL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_LIST_ID INTEGER, " +
                "$COLUMN_BOOK_ID TEXT, " +
                "UNIQUE($COLUMN_LIST_ID, $COLUMN_BOOK_ID))")
        db?.execSQL(createBookListsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LISTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOK_LISTS")
        onCreate(db)
    }

    fun insertUser(username: String, email: String, password: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val db = writableDatabase
        val userId = db.insert(TABLE_NAME, null, values)
        if (userId > 0) {
            insertList("Favorites", username)
        }
        return userId
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

    fun insertList(listName: String, username: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_LIST_NAME, listName)
            put(COLUMN_USERNAME, username)
        }
        val db = writableDatabase
        return db.insert(TABLE_LISTS, null, values)
    }

    fun getListId(username: String, listName: String): Long? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_LIST_NAME = ?"
        val selectionArgs = arrayOf(username, listName)
        val cursor = db.query(TABLE_LISTS, arrayOf(COLUMN_LIST_ID), selection, selectionArgs, null, null, null)
        return if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LIST_ID))
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun insertBookToList(book: VolumeItem, listName: String, username: String): Long {
        val listId = getListId(username, listName) ?: return -1
        val db = writableDatabase

        var selection = "$COLUMN_BOOK_ID = ? AND $COLUMN_USERNAME = ?"
        var cursor = db.query(TABLE_BOOKS, null, selection, arrayOf(book.id, username), null, null, null)
        if (cursor.count == 0) {
            val bookValues = ContentValues().apply {
                put(COLUMN_BOOK_ID, book.id)
                put(COLUMN_TITLE, book.volumeInfo.title ?: "")
                put(COLUMN_AUTHORS, book.volumeInfo.authors?.joinToString(", ") ?: "")
                put(COLUMN_THUMBNAIL, book.volumeInfo.imageLinks?.thumbnail ?: "")
                put(COLUMN_USERNAME, username)
            }
            db.insert(TABLE_BOOKS, null, bookValues)
        }
        cursor.close()

        val blValues = ContentValues().apply {
            put(COLUMN_LIST_ID, listId)
            put(COLUMN_BOOK_ID, book.id)
        }
        return db.insertWithOnConflict(TABLE_BOOK_LISTS, null, blValues, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun isBookInList(listId: Long, bookId: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_LIST_ID = ? AND $COLUMN_BOOK_ID = ?"
        val cursor = db.query(TABLE_BOOK_LISTS, null, selection, arrayOf(listId.toString(), bookId), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun removeBookFromList(listId: Long, bookId: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_BOOK_LISTS, "$COLUMN_LIST_ID = ? AND $COLUMN_BOOK_ID = ?", arrayOf(listId.toString(), bookId))
    }

    fun getUserLists(username: String): List<ListItem> {
        val lists = mutableListOf<ListItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_LISTS, arrayOf(COLUMN_LIST_ID, COLUMN_LIST_NAME), "$COLUMN_USERNAME = ?", arrayOf(username), null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LIST_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME))
            lists.add(ListItem(id, name))
        }
        cursor.close()
        return lists
    }

    fun getBooksInList(listId: Long, username: String): List<VolumeItem> {
        val books = mutableListOf<VolumeItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_BOOK_LISTS, arrayOf(COLUMN_BOOK_ID), "$COLUMN_LIST_ID = ?", arrayOf(listId.toString()), null, null, null)
        while (cursor.moveToNext()) {
            val bookId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID))
            val bookCursor = db.query(
                TABLE_BOOKS,
                arrayOf(COLUMN_TITLE, COLUMN_AUTHORS, COLUMN_THUMBNAIL),
                "$COLUMN_BOOK_ID = ? AND $COLUMN_USERNAME = ?",
                arrayOf(bookId, username),
                null,
                null,
                null
            )
            if (bookCursor.moveToFirst()) {
                val title = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val authorsStr = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_AUTHORS))
                val thumbnail = bookCursor.getString(bookCursor.getColumnIndexOrThrow(COLUMN_THUMBNAIL))
                val authors = authorsStr?.split(", ") ?: emptyList()
                val imageLinks = if (thumbnail.isNotEmpty()) ImageLinks(thumbnail) else null
                val volumeInfo =
                    VolumeInfo(title, authors, null, null, null, null, imageLinks, null)
                val item = VolumeItem(bookId, volumeInfo)
                books.add(item)
            }
            bookCursor.close()
        }
        cursor.close()
        return books
    }
}

data class ListItem(val id: Long, val name: String)
