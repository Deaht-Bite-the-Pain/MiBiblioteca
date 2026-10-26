package com.example.desafio03

data class Book(
    val id: Long = 0,
    val uid: String,
    val title: String,
    val author: String,
    val genre: String,
    val totalPages: Int,
    val readPages: Int,
    val status: String,
    val rating: Int,
    val finishedDate: String?   // formato yyyy-MM-dd
) {
    val progress: Int
        get() = if (totalPages > 0) readPages * 100 / totalPages else 0
}

object Const {
    val GENRES = listOf("Novela", "Ciencia ficción", "Historia", "Desarrollo personal")
    val STATUSES = listOf("Por leer", "Leyendo", "Terminado")
}
