package com.example.test_store.data.model

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("cart_item_id")
    val cartItemId: Int,
    @SerializedName("product_id")
    val productId: Int,
    val nombre: String,
    val precio: Double,
    @SerializedName("imagen_url")
    val imageUrl: String?,
    var cantidad: Int,
    val stock: Int
)
