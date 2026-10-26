package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirm)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Correo no válido"
                return@setOnClickListener
            }
            if (pass.length < 6) {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }
            if (pass != confirm) {
                etConfirm.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val i = Intent(this, MainActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(i)
                    } else {
                        Toast.makeText(
                            this, "No se pudo registrar: " + task.exception?.message, Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
