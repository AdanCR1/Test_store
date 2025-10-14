package com.example.test_store.data.model

data class CartResponse(
    val success: Boolean,
    val data: List<CartItem>? = null,
    val message: String? = null
)
