package com.example.trailwatch

data class Usuario(
    val username: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    var contraseña: String,
    var peso: Double? = null,
    var edad: Int? = null,
    var estatura: Double? = null
)
