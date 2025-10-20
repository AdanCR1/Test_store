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
import com.example.test_store.data.model.CartItem
import com.example.test_store.data.model.CartResponse
import com.example.test_store.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class StoreRepository {

    private val gson = Gson()
    private val BASE_URL = BuildConfig.API_BASE_URL

    private fun makeRequest(urlString: String, method: String = "GET", body: String? = null): String {
        val url = URL(urlString)
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        CookieStorage.cookie?.let {
            connection.setRequestProperty("Cookie", it)
        }

        if (method == "POST" || method == "PUT" || method == "DELETE") {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            body?.let {
                connection.outputStream.bufferedWriter().use { writer -> writer.write(it) }
            }
        }

        val stream = if (connection.responseCode < 400) connection.inputStream else connection.errorStream
        return stream.bufferedReader().use { it.readText() }
    }

    suspend fun registerUser(request: RegisterRequest): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/register_user.php"
        val jsonInputString = gson.toJson(request)

        try {
            val jsonText = makeRequest(url, "POST", jsonInputString)
            val response = gson.fromJson(jsonText, LoginResponse::class.java)

            if (response.success) {
                true
            } else {
                throw Exception(response.message ?: "Error desconocido del servidor")
            }
        } catch (e: Exception) {
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else if (e is com.google.gson.JsonSyntaxException) {
                throw Exception("Error de formato de respuesta del servidor: ${e.message}")
            } else {
                throw e
            }
        }
    }

    suspend fun userLogin(email: String, password: String): User = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/login_user.php"
        val jsonInputString = """{"email":"$email","password":"$password"}"""

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }
            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(jsonText, LoginResponse::class.java)

            if (response.success && response.user != null) {
                val cookieHeader = connection.headerFields["Set-Cookie"]
                if (cookieHeader != null) {
                    CookieStorage.cookie = cookieHeader.joinToString(separator = ";")
                }
                response.user
            } else {
                throw Exception(response.message ?: "Credenciales inválidas")
            }
        } catch (e: Exception) {
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else if (e is com.google.gson.JsonSyntaxException) {
                throw Exception("Error de formato de respuesta del servidor: ${e.message}")
            } else {
                throw e
            }
        }
    }

    suspend fun loadProductosFromAPI(): List<Producto> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/products.php"
            val jsonText = makeRequest(url)
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
            val jsonText = makeRequest(url)
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
            val jsonText = makeRequest(url)
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

        try {
            val jsonText = makeRequest(url, "POST", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al crear el producto")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }

    suspend fun updateProduct(product: ProductUpdateRequest): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/products.php"
        val jsonInputString = gson.toJson(product)

        try {
            val jsonText = makeRequest(url, "PUT", jsonInputString)
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

        try {
            val jsonText = makeRequest(url, "DELETE")
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

    suspend fun getCart(): List<CartItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/cart.php"
            val jsonText = makeRequest(url)
            val response = gson.fromJson(jsonText, CartResponse::class.java)
            if (response.success) {
                response.data ?: emptyList()
            } else {
                throw Exception(response.message ?: "Error al obtener el carrito")
            }
        } catch (e: Exception) {
            if (e is java.io.FileNotFoundException) { 
                throw Exception("No autorizado o recurso no encontrado.")
            }
            throw Exception("No se pudo conectar: ${e.message}")
        }
    }

    suspend fun addToCart(productId: Int, quantity: Int): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/cart.php"
        val jsonInputString = """{"product_id":$productId,"quantity":$quantity}"""

        try {
            val jsonText = makeRequest(url, "POST", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al añadir al carrito")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }

    suspend fun updateCartItemQuantity(cartItemId: Int, quantity: Int): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/cart.php"
        val jsonInputString = """{"cart_item_id":$cartItemId,"quantity":$quantity}"""

        try {
            val jsonText = makeRequest(url, "PUT", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)
            return@withContext response["success"] == true
        } catch (e: Exception) {
            throw Exception("Error al actualizar el carrito: ${e.message}")
        }
    }

    suspend fun removeFromCart(cartItemId: Int): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/cart.php"
        val jsonInputString = """{"cart_item_id":$cartItemId}"""

        try {
            val jsonText = makeRequest(url, "DELETE", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)
            return@withContext response["success"] == true
        } catch (e: Exception) {
            throw Exception("Error al eliminar del carrito: ${e.message}")
        }
    }

    suspend fun checkout(cartItems: List<CartItem>, address: String): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/checkout.php"
        val jsonInputString = gson.toJson(mapOf("cart_items" to cartItems, "address" to address))

        try {
            val jsonText = makeRequest(url, "POST", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error durante el checkout")
            }
        } catch (e: Exception) {
            throw Exception("Error de conexión o de servidor: ${e.message}")
        }
    }

    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/manage_users.php"
        try {
            val jsonText = makeRequest(url)
            val response = gson.fromJson(jsonText, com.example.test_store.data.model.UserResponse::class.java)
            if (response.success) {
                response.data ?: emptyList()
            } else {
                throw Exception(response.message ?: "Error al obtener usuarios")
            }
        } catch (e: Exception) {
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else {
                throw e
            }
        }
    }

    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/update_user.php"
        val jsonInputString = gson.toJson(user)

        try {
            val jsonText = makeRequest(url, "PUT", jsonInputString)
            val response = gson.fromJson(jsonText, Map::class.java)

            if (response["success"] == true) {
                true
            } else {
                throw Exception(response["message"]?.toString() ?: "Error al actualizar el usuario")
            }
        } catch (e: Exception) {
            if (e is java.net.SocketTimeoutException || e is java.io.IOException) {
                throw Exception("Error de conexión: ${e.message}")
            } else {
                throw e
            }
        }
    }
}