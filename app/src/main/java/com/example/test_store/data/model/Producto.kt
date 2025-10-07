package com.example.test_store.data.model

import com.google.gson.annotations.SerializedName

data class Producto(
    val id: Int,
    val nombre: String?,
    @SerializedName("descripción")
    val descripcion: String?,
    val precio: Double?,
    val stock: Int?,
    @SerializedName("categoria_id")
    val categoriaId: Int?,
    @SerializedName("imagen_url")
    val imageUrl: String?,
    // This field is likely joined in the PHP script, so we keep it.
    @SerializedName("categoria_nombre")
    val categoriaNombre: String?
)
