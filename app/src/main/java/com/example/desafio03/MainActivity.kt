package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var uid: String
    private val year = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        uid = user.uid
        db = DbHelper(this)

        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tvGreeting).text = "Hola, " + user.email

        findViewById<Button>(R.id.btnGoal).setOnClickListener { showGoalDialog() }
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, BookFormActivity::class.java))
        }
        findViewById<Button>(R.id.btnLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
        findViewById<Button>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) refresh()
    }

    private fun refresh() {
        val books = db.getBooks(uid)
        val goal = db.getGoal(uid, year)
        val finished = books.count {
            it.status == "Terminado" && it.finishedDate?.startsWith(year.toString()) == true
        }

        findViewById<TextView>(R.id.tvGoal).text = if (goal == 0) {
            "Aún no has definido tu meta para $year"
        } else {
            "Meta $year: $goal libros\nTerminados: $finished\nFaltan: ${maxOf(goal - finished, 0)}"
        }

        val container = findViewById<LinearLayout>(R.id.containerReading)
        container.removeAllViews()
        val reading = books.filter { it.status == "Leyendo" }
        findViewById<TextView>(R.id.tvNoReading).visibility =
            if (reading.isEmpty()) View.VISIBLE else View.GONE

        for (b in reading) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_reading, container, false)
            row.findViewById<TextView>(R.id.tvTitle).text = b.title
            row.findViewById<TextView>(R.id.tvPercent).text = "${b.progress}%"
            row.findViewById<ProgressBar>(R.id.progress).progress = b.progress
            row.setOnClickListener {
                startActivity(Intent(this, BookFormActivity::class.java).putExtra("bookId", b.id))
            }
            container.addView(row)
        }
    }

    private fun showGoalDialog() {
        val input = EditText(this)
        input.hint = "Número de libros"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        val current = db.getGoal(uid, year)
        if (current > 0) input.setText(current.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Meta anual $year")
            .setView(input)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
        // Se asigna después de show() para que el diálogo no se cierre si el dato es inválido
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val n = input.text.toString().toIntOrNull()
            if (n == null || n <= 0) {
                input.error = "Debe ser un número entero mayor que cero"
            } else {
                db.setGoal(uid, year, n)
                dialog.dismiss()
                refresh()
            }
        }
    }
}
