package com.example.test_store.data.model

import com.google.gson.annotations.SerializedName

// Data class for sending product updates or creations to the server.
data class ProductUpdateRequest(
    val id: Int? = null, // Null for creation, non-null for update
    val nombre: String,
    val descripción: String,
    val precio: Double,
    val stock: Int,
    @SerializedName("imagen_url")
    val imagenUrl: String,
    // The backend expects the category NAME in a field confusingly named "categoria_id"
    @SerializedName("categoria_id")
    val categoriaNombre: String
)
