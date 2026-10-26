package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase conserva la sesión: si ya hay usuario, pasa directo a la pantalla principal
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Correo no válido"
                return@setOnClickListener
            }
            if (pass.length < 6) {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) goToMain()
                else Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnGoRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
