package com.example.desafio03

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val year = Calendar.getInstance().get(Calendar.YEAR)

        // Solo libros terminados cuya fecha de término sea del año actual
        val finished = DbHelper(this).getBooks(uid).filter {
            it.status == "Terminado" && it.finishedDate?.startsWith(year.toString()) == true
        }

        val sb = StringBuilder("Estadísticas $year\n\nLibros terminados por género:\n")
        for (g in Const.GENRES) {
            sb.append("  • $g: ${finished.count { it.genre == g }}\n")
        }

        if (finished.isEmpty()) {
            sb.append("\nAún no has terminado libros este año.")
        } else {
            val top = Const.GENRES.maxByOrNull { g -> finished.count { it.genre == g } }
            sb.append("\nGénero más leído: $top")
            sb.append("\nPáginas leídas en el año: ${finished.sumOf { it.totalPages }}")

            // Mejor calificado; en empate, el que se terminó primero (fecha menor)
            val best = finished.sortedWith(compareByDescending<Book> { it.rating }.thenBy { it.finishedDate }).first()
            sb.append("\nLibro mejor calificado: ${best.title} (${best.rating} ★)")
        }

        findViewById<TextView>(R.id.tvStats).text = sb.toString()
    }
}
