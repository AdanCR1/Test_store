package com.example.test_store.data.model

data class CategoryResponse(
    val success: Boolean,
    val data: List<Category>? = null,
    val message: String? = null
)
