package com.example.test_store.data.repository

import com.example.test_store.data.model.LoginResponse
import com.example.test_store.data.model.Producto
import com.example.test_store.data.model.ProductoResponse
import com.example.test_store.data.model.SingleProductoResponse
import com.example.test_store.data.model.User
import com.example.test_store.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class StoreRepository {

    private val gson = Gson()
    private val BASE_URL = BuildConfig.API_BASE_URL

    suspend fun userLogin(email: String, password: String): User = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/login_user.php"
            val jsonInputString = """{"email":"$email","password":"$password"}"""

            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }

            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }

            val response = try {
                gson.fromJson(jsonText, LoginResponse::class.java)
            } catch (e: Exception) {
                throw Exception("Respuesta inválida del servidor: $jsonText")
            }

            if (response.success && response.user != null) {
                response.user
            } else {
                throw Exception(response.message ?: "Error en el login")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o login: ${e.message}")
        }
    }

    suspend fun loadProductosFromAPI(): List<Producto> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/products.php"
            val jsonText = URL(url).readText()
            val response = gson.fromJson(jsonText, ProductoResponse::class.java)
            if (response.success) {
                response.data ?: emptyList()
            } else {
                throw Exception(response.message ?: "Error al obtener productos")
            }
        } catch (e: Exception) {
            throw Exception("No se pudo conectar: ${e.message}")
        }
    }

    suspend fun loadSingleProductFromAPI(productId: Int): Producto = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/products.php?id=$productId"
            val jsonText = URL(url).readText()
            val response = gson.fromJson(jsonText, SingleProductoResponse::class.java)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.message ?: "Producto no encontrado")
            }
        } catch (e: Exception) {
            throw Exception("No se pudo conectar: ${e.message}")
        }
    }
}
