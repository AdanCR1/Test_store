package com.example.test_store.data.model

data class UserResponse(
    val success: Boolean,
    val data: List<User>?,
    val message: String?
)