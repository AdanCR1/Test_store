package com.example.test_store.data.model

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val direccion: String?,
    val telefono: String?
)
