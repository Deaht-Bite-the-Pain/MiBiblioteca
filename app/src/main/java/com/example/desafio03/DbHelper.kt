package com.example.desafio03

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "libros.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE books (id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT, title TEXT, " +
                    "author TEXT, genre TEXT, totalPages INTEGER, readPages INTEGER, " +
                    "status TEXT, rating INTEGER, finishedDate TEXT)"
        )
        db.execSQL("CREATE TABLE goals (uid TEXT, year INTEGER, goal INTEGER, PRIMARY KEY (uid, year))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun values(b: Book) = ContentValues().apply {
        put("uid", b.uid)
        put("title", b.title)
        put("author", b.author)
        put("genre", b.genre)
        put("totalPages", b.totalPages)
        put("readPages", b.readPages)
        put("status", b.status)
        put("rating", b.rating)
        put("finishedDate", b.finishedDate)
    }

    fun insertBook(b: Book) {
        writableDatabase.insert("books", null, values(b))
    }

    fun updateBook(b: Book) {
        writableDatabase.update("books", values(b), "id = ? AND uid = ?", arrayOf(b.id.toString(), b.uid))
    }

    fun deleteBook(id: Long, uid: String) {
        writableDatabase.delete("books", "id = ? AND uid = ?", arrayOf(id.toString(), uid))
    }

    // Siempre se filtra por uid: cada usuario solo ve sus propios libros
    fun getBooks(uid: String): List<Book> {
        val list = ArrayList<Book>()
        val c = readableDatabase.rawQuery("SELECT * FROM books WHERE uid = ? ORDER BY id DESC", arrayOf(uid))
        while (c.moveToNext()) {
            list.add(
                Book(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    uid = c.getString(c.getColumnIndexOrThrow("uid")),
                    title = c.getString(c.getColumnIndexOrThrow("title")),
                    author = c.getString(c.getColumnIndexOrThrow("author")),
                    genre = c.getString(c.getColumnIndexOrThrow("genre")),
                    totalPages = c.getInt(c.getColumnIndexOrThrow("totalPages")),
                    readPages = c.getInt(c.getColumnIndexOrThrow("readPages")),
                    status = c.getString(c.getColumnIndexOrThrow("status")),
                    rating = c.getInt(c.getColumnIndexOrThrow("rating")),
                    finishedDate = c.getString(c.getColumnIndexOrThrow("finishedDate"))
                )
            )
        }
        c.close()
        return list
    }

    fun getBook(id: Long, uid: String): Book? = getBooks(uid).firstOrNull { it.id == id }

    fun getGoal(uid: String, year: Int): Int {
        val c = readableDatabase.rawQuery(
            "SELECT goal FROM goals WHERE uid = ? AND year = ?", arrayOf(uid, year.toString())
        )
        val goal = if (c.moveToFirst()) c.getInt(0) else 0
        c.close()
        return goal
    }

    fun setGoal(uid: String, year: Int, goal: Int) {
        val cv = ContentValues()
        cv.put("uid", uid)
        cv.put("year", year)
        cv.put("goal", goal)
        writableDatabase.insertWithOnConflict("goals", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
