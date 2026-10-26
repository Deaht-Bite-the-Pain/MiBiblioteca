package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LibraryActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        db = DbHelper(this)
    }

    override fun onResume() {
        super.onResume()
        val books = db.getBooks(uid)
        val list = findViewById<ListView>(R.id.listBooks)
        val empty = findViewById<TextView>(R.id.tvEmpty)

        empty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE

        list.adapter = object : ArrayAdapter<Book>(this, R.layout.item_book, books) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: layoutInflater.inflate(R.layout.item_book, parent, false)
                val b = books[position]
                v.findViewById<TextView>(R.id.tvTitle).text = b.title
                v.findViewById<TextView>(R.id.tvAuthor).text = "${b.author} · ${b.genre}"
                v.findViewById<TextView>(R.id.tvInfo).text = "${b.status} · ${b.progress}% leído"
                val tvRating = v.findViewById<TextView>(R.id.tvRating)
                if (b.status == "Terminado") {
                    tvRating.visibility = View.VISIBLE
                    tvRating.text = "Calificación: " + "★".repeat(b.rating) + "☆".repeat(5 - b.rating)
                } else {
                    tvRating.visibility = View.GONE
                }
                return v
            }
        }
        list.setOnItemClickListener { _, _, position, _ ->
            startActivity(Intent(this, BookFormActivity::class.java).putExtra("bookId", books[position].id))
        }
    }
}
