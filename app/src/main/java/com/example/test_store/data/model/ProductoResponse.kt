package com.example.test_store.data.model

data class ProductoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Producto>? = null
)
