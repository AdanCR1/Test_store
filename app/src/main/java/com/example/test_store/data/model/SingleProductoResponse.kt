package com.example.test_store.data.model

data class SingleProductoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Producto? = null
)
