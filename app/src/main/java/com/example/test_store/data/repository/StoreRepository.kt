package com.example.test_store.data.repository

import com.example.test_store.data.model.LoginResponse
import com.example.test_store.data.model.Producto
import com.example.test_store.data.model.ProductoResponse
import com.example.test_store.data.model.SingleProductoResponse
import com.example.test_store.data.model.RegisterRequest
import com.example.test_store.data.model.User
import com.example.test_store.data.model.Category
import com.example.test_store.data.model.CategoryResponse
import com.example.test_store.data.model.ProductUpdateRequest
import com.example.test_store.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class StoreRepository {

    private val gson = Gson()
    private val BASE_URL = BuildConfig.API_BASE_URL

    suspend fun registerUser(request: RegisterRequest): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/register_user.php"
        val jsonInputString = gson.toJson(request)

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }

        try {
            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(jsonText, LoginResponse::class.java)

            if (response.success) {
                true
            } else {
                // Propagate the specific message from the backend
                throw Exception(response.message ?: "Error desconocido del servidor")
            }
        } catch (e: Exception) {
            // Only catch and wrap actual network/parsing errors
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else if (e is com.google.gson.JsonSyntaxException) {
                throw Exception("Error de formato de respuesta del servidor: ${e.message}")
            } else {
                // Re-throw other exceptions, including the one from response.message
                throw e
            }
        }
    }

    suspend fun userLogin(email: String, password: String): User = withContext(Dispatchers.IO) {
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

        try {
            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(jsonText, LoginResponse::class.java)

            if (response.success && response.user != null) {
                response.user
            } else {
                // If backend explicitly says login failed, use "Credenciales inválidas"
                // Otherwise, use the backend's message or a generic one
                throw Exception(response.message ?: "Credenciales inválidas")
            }
        } catch (e: Exception) {
            // Differentiate between network/parsing errors and login failures
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else if (e is com.google.gson.JsonSyntaxException) {
                throw Exception("Error de formato de respuesta del servidor: ${e.message}")
            } else {
                // This catches the Exception thrown above for invalid credentials
                // or any other unexpected exception.
                throw e
            }
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

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/products.php?action=getCategories"
            val jsonText = URL(url).readText()
            val response = gson.fromJson(jsonText, CategoryResponse::class.java)
            if (response.success) {
                response.data ?: emptyList()
            } else {
                throw Exception(response.message ?: "Error al obtener categorías")
            }
        } catch (e: Exception) {
            throw Exception("No se pudo conectar: ${e.message}")
        }
    }

    suspend fun createProduct(product: ProductUpdateRequest): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/products.php"
        val jsonInputString = gson.toJson(product)

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }

        try {
            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            // Assuming a simple success/message response, not returning a full object
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al crear el producto")
            }
        } catch (e: Exception) {
            // Handle network/parsing errors
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }

    suspend fun updateProduct(product: ProductUpdateRequest): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/products.php"
        val jsonInputString = gson.toJson(product)

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.apply {
            requestMethod = "PUT"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }

        try {
            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al actualizar el producto")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }

    suspend fun deleteProduct(productId: Int): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/products.php?id=$productId"

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.apply {
            requestMethod = "DELETE"
            connectTimeout = 10000
            readTimeout = 10000
        }

        try {
            // For DELETE, the response body might be read from errorStream if status code is not 2xx
            val stream = if (connection.responseCode < 400) connection.inputStream else connection.errorStream
            val jsonText = stream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al eliminar el producto")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }
}
