@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test_store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.test_store.ui.theme.Test_storeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test_storeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Modelos de datos
data class User(
    val id: Int,
    val nombre: String,
    val email: String
)

data class Producto(
    val id: Int,
    val nombre: String,
    val precio: String,
    val descripción: String,
    val categoria_id: Int,
    val categoria_nombre: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

data class ProductoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Producto>? = null
)

// Navegación principal
@Composable
fun AppNavigation() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Mostrar error global si ocurre
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error de la app", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage ?: "Error desconocido")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { errorMessage = null; currentUser = null }) {
                    Text("Reiniciar")
                }
            }
        }
    } else if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { user ->
                currentUser = user
            },
            onError = { error ->
                errorMessage = error
            }
        )
    } else {
        ProductosScreen(
            currentUser = currentUser!!,
            onLogout = { currentUser = null },
            onError = { error ->
                errorMessage = error
            }
        )
    }
}

// Pantalla de Login
@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit, onError: (String) -> Unit) {
    var email by remember { mutableStateOf("us1@gmail.com") } // Cambié a @gmail.com
    var password by remember { mutableStateOf("4321") }
    var isLoading by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ROG Store",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )

        if (loginErrorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(loginErrorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    loginErrorMessage = "Completa todos los campos"
                    return@Button
                }
                isLoading = true
                loginErrorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "Iniciando sesión..." else "Iniciar Sesión")
        }

        // Credenciales de demo
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Demo: us1@gmail.com / 4321",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Manejar el login
    if (isLoading) {
        LaunchedEffect(Unit) {
            try {
                println("🔐 Iniciando proceso de login...")
                val user = userLogin(email, password)
                println("✅ Login exitoso, usuario: ${user.nombre}, ID: ${user.id}")
                onLoginSuccess(user)
            } catch (e: Exception) {
                println("❌ Error en login: ${e.message}")
                loginErrorMessage = e.message ?: "Error desconocido"
                isLoading = false
            }
        }
    }
}

// Pantalla de Productos
@Composable
fun ProductosScreen(currentUser: User, onLogout: () -> Unit, onError: (String) -> Unit) {
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        try {
            println("🛍️ Cargando productos para usuario ID: ${currentUser.id}")
            productos = loadProductosFromAPI(currentUser.id)
            println("✅ Productos cargados: ${productos.size}")
            isLoading = false
        } catch (e: Exception) {
            println("❌ Error cargando productos: ${e.message}")
            errorMessage = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ROG Store - Productos") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Info del usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bienvenido, ${currentUser.nombre}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(currentUser.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("User ID: ${currentUser.id}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando productos...")
                        }
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage ?: "Error",
                                color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                isLoading = true
                                errorMessage = null
                            }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                productos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Text("No hay productos disponibles")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(productos) { producto ->
                            ProductoCard(producto = producto)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de producto
@Composable
fun ProductoCard(producto: Producto) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(producto.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$${producto.precio}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(producto.descripción,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2)
        }
    }
}

// Función de login - VERSIÓN SIMPLIFICADA
suspend fun userLogin(email: String, password: String): User = withContext(Dispatchers.IO) {
    try {
        val url = "http://192.168.0.0:8080/admin-panel/php/login_user.php" // IP de ejemplo
        val jsonInputString = """{"email":"$email","password":"$password"}"""

        val connection = URL(url).openConnection() as java.net.HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }

        connection.outputStream.bufferedWriter().use { it.write(jsonInputString) }

        val responseCode = connection.responseCode
        val jsonText = connection.inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val response = gson.fromJson(jsonText, LoginResponse::class.java)

        if (response.success && response.user != null) {
            response.user
        } else {
            throw Exception(response.message ?: "Error en el login")
        }
    } catch (e: Exception) {
        throw Exception("Error de conexión: ${e.message}")
    }
}

// Función para obtener productos
suspend fun loadProductosFromAPI(userId: Int): List<Producto> = withContext(Dispatchers.IO) {
    try {
        val url = "http://192.168.0.0:8080/admin-panel/php/products.php?user_id=$userId" // IP de ejemplo
        val jsonText = URL(url).readText()

        val gson = Gson()
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

@Preview
@Composable
fun PreviewLogin() {
    Test_storeTheme { LoginScreen(onLoginSuccess = {}, onError = {}) }
}