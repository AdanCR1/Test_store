package com.example.test_store.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val nombre: String,
    val email: String,
    val direccion: String?,
    val telefono: String?,
    @SerializedName("fecha_registro")
    val fechaRegistro: String?
)
