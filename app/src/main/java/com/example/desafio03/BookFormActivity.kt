package com.example.desafio03

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookFormActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var uid: String
    private var editing: Book? = null

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etTotal: EditText
    private lateinit var etRead: EditText
    private lateinit var spGenre: Spinner
    private lateinit var spStatus: Spinner
    private lateinit var ratingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_form)

        uid = FirebaseAuth.getInstance().currentUser!!.uid
        db = DbHelper(this)

        etTitle = findViewById(R.id.etTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etTotal = findViewById(R.id.etTotal)
        etRead = findViewById(R.id.etRead)
        spGenre = findViewById(R.id.spGenre)
        spStatus = findViewById(R.id.spStatus)
        ratingBar = findViewById(R.id.ratingBar)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Const.GENRES)
        spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Const.STATUSES)

        // La calificación solo se habilita cuando el estado es Terminado
        spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val finished = Const.STATUSES[pos] == "Terminado"
                ratingBar.isEnabled = finished
                if (!finished) ratingBar.rating = 0f
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        val bookId = intent.getLongExtra("bookId", -1)
        if (bookId != -1L) {
            editing = db.getBook(bookId, uid)
        }
        editing?.let { b ->
            title = "Editar libro"
            etTitle.setText(b.title)
            etAuthor.setText(b.author)
            etTotal.setText(b.totalPages.toString())
            etRead.setText(b.readPages.toString())
            spGenre.setSelection(Const.GENRES.indexOf(b.genre))
            spStatus.setSelection(Const.STATUSES.indexOf(b.status))
            ratingBar.rating = b.rating.toFloat()
            btnDelete.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        btnDelete.setOnClickListener {
            editing?.let { db.deleteBook(it.id, uid) }
            finish()
        }
    }

    private fun save() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val total = etTotal.text.toString().trim().toIntOrNull()
        val read = etRead.text.toString().trim().toIntOrNull()
        val status = spStatus.selectedItem.toString()
        val genre = spGenre.selectedItem.toString()
        val stars = ratingBar.rating.toInt()

        if (title.length !in 2..60) {
            etTitle.fail("El título es obligatorio y debe tener entre 2 y 60 caracteres")
            return
        }
        if (author.length !in 2..60) {
            etAuthor.fail("El autor es obligatorio y debe tener entre 2 y 60 caracteres")
            return
        }
        if (total == null || total <= 0) {
            etTotal.fail("El total de páginas debe ser un entero mayor que cero")
            return
        }
        if (read == null || read < 0 || read > total) {
            etRead.fail("Las páginas leídas deben ser un entero entre 0 y $total")
            return
        }
        if (status == "Terminado" && stars < 1) {
            Toast.makeText(this, "Un libro terminado debe tener calificación de 1 a 5 estrellas", Toast.LENGTH_LONG).show()
            return
        }
        if (status != "Terminado" && stars > 0) {
            Toast.makeText(this, "Solo los libros terminados pueden tener calificación", Toast.LENGTH_LONG).show()
            return
        }

        // Fecha de término: se guarda al marcar Terminado y se conserva si ya existía
        val finishedDate = if (status == "Terminado") {
            editing?.finishedDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        } else null

        val book = Book(
            id = editing?.id ?: 0,
            uid = uid,
            title = title,
            author = author,
            genre = genre,
            totalPages = total,
            readPages = read,
            status = status,
            rating = stars,
            finishedDate = finishedDate
        )
        if (editing == null) db.insertBook(book) else db.updateBook(book)
        finish()
    }
}
