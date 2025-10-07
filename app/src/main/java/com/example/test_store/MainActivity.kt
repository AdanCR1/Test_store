@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test_store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
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
import com.example.test_store.ui.screens.LoginScreen
import com.example.test_store.ui.screens.ProductosScreen
import com.example.test_store.ui.screens.ProductoDetailScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_store.ui.screens.ProductsViewModel
import com.example.test_store.ui.screens.ProductsViewModelFactory
import com.example.test_store.ui.screens.LoginViewModel
import com.example.test_store.ui.screens.LoginViewModelFactory
import com.example.test_store.ui.screens.ProductDetailViewModel
import com.example.test_store.ui.screens.ProductDetailViewModelFactory
import com.example.test_store.data.model.User
import com.example.test_store.data.model.Producto
import com.example.test_store.data.model.LoginResponse
import com.example.test_store.data.model.ProductoResponse
import com.example.test_store.data.model.SingleProductoResponse
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.gson.Gson
import coil.compose.AsyncImage

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


// Navegación principal
@Composable
fun AppNavigation() {
    val repository = remember { StoreRepository() }

    // Hoist ViewModels to the top level
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(repository))
    val productsViewModel: ProductsViewModel = viewModel(factory = ProductsViewModelFactory(repository))
    val productDetailViewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModelFactory(repository))

    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Back handler para la pantalla de detalle
    BackHandler(enabled = selectedProductId != null) {
        selectedProductId = null
    }

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
                Button(onClick = {
                    errorMessage = null
                    currentUser = null
                    loginViewModel.logout() // Also clear the viewmodel state
                }) {
                    Text("Reiniciar")
                }
            }
        }
    } else if (currentUser == null) {
        LoginScreen(
            loginViewModel = loginViewModel,
            onLoginSuccess = { user ->
                currentUser = user
            }
        )
    } else if (selectedProductId != null) {
        ProductoDetailScreen(
            productId = selectedProductId!!,
            onBack = { selectedProductId = null },
            productDetailViewModel = productDetailViewModel
        )
    } else {
        ProductosScreen(
            currentUser = currentUser!!,
            onLogout = {
                currentUser = null
                loginViewModel.logout() // Call the new logout function
            },
            onProductoClick = { productId ->
                selectedProductId = productId
            },
            onError = { error ->
                errorMessage = error
            },
            productsViewModel = productsViewModel
        )
    }
}






