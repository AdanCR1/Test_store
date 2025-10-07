@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test_store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_store.data.model.User
import com.example.test_store.data.repository.StoreRepository
import com.example.test_store.ui.screens.*
import com.example.test_store.ui.theme.Test_storeTheme
import java.net.CookieHandler
import java.net.CookieManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up a global cookie manager to handle session cookies
        val cookieManager = CookieManager()
        CookieHandler.setDefault(cookieManager)

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
    val registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(repository))
    val productFormViewModel: ProductFormViewModel = viewModel(factory = ProductFormViewModelFactory(repository))

    // Navigation state
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var showRegisterScreen by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf<Int?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }

    // Other state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var prefillEmail by remember { mutableStateOf("") }
    var prefillPassword by remember { mutableStateOf("") }

    val isFormOpen = editingProductId != null || isAddingProduct

    // Back handler for all navigation states
    BackHandler(enabled = selectedProductId != null || showRegisterScreen || isFormOpen) {
        when {
            isFormOpen -> {
                isAddingProduct = false
                editingProductId = null
            }
            selectedProductId != null -> selectedProductId = null
            showRegisterScreen -> showRegisterScreen = false
        }
    }

    fun closeFormAndRefresh() {
        isAddingProduct = false
        editingProductId = null
        productsViewModel.loadProducts() // Refresh product list
        // If coming from detail view, that will also recompose and get fresh data.
    }

    // Main navigation logic
    if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // ... (Error handling UI, unchanged)
        }
    } else if (currentUser == null) {
        // --- Login/Register Flow ---
        if (showRegisterScreen) {
            RegisterScreen(
                registerViewModel = registerViewModel,
                onRegistrationSuccess = { email, password ->
                    showRegisterScreen = false
                    registerViewModel.resetState()
                    prefillEmail = email
                    prefillPassword = password
                },
                onBack = { showRegisterScreen = false }
            )
        } else {
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = { user -> currentUser = user },
                onNavigateToRegister = { showRegisterScreen = true },
                prefillEmail = prefillEmail,
                prefillPassword = prefillPassword
            )
        }
    } else if (isFormOpen) {
        // --- Add/Edit Product Flow ---
        LaunchedEffect(editingProductId) {
            editingProductId?.let {
                productFormViewModel.loadProduct(it)
            }
        }
        ProductFormScreen(
            viewModel = productFormViewModel,
            onSaveSuccess = { closeFormAndRefresh() },
            onBack = {
                isAddingProduct = false
                editingProductId = null
            }
        )
    } else if (selectedProductId != null) {
        // --- Product Detail Flow ---
        ProductoDetailScreen(
            productId = selectedProductId!!,
            currentUser = currentUser,
            onBack = { selectedProductId = null },
            productDetailViewModel = productDetailViewModel,
            onNavigateToEdit = { productId -> editingProductId = productId }
        )
    } else {
        // --- Product List Flow (Default) ---
        ProductosScreen(
            currentUser = currentUser!!,
            onLogout = {
                currentUser = null
                loginViewModel.logout()
            },
            onProductoClick = { productId -> selectedProductId = productId },
            onError = { error -> errorMessage = error },
            productsViewModel = productsViewModel,
            onNavigateToAdd = {
                productFormViewModel.resetForm()
                isAddingProduct = true
            }
        )
    }
}