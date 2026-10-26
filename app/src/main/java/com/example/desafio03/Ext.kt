package com.example.desafio03

import android.widget.EditText

// Muestra el mensaje de error y deja el cursor en el campo para que se vea el aviso
fun EditText.fail(msg: String) {
    error = msg
    requestFocus()
}
