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

@Composable
fun AppNavigation() {
    val repository = remember { StoreRepository() }

    // ViewModels
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(repository))
    val productsViewModel: ProductsViewModel = viewModel(factory = ProductsViewModelFactory(repository))
    val productDetailViewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModelFactory(repository))
    val registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(repository))
    val productFormViewModel: ProductFormViewModel = viewModel(factory = ProductFormViewModelFactory(repository))
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(repository))
    val checkoutViewModel: CheckoutViewModel = viewModel(factory = CheckoutViewModelFactory(repository))

    // Navigation state
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var showRegisterScreen by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf<Int?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }
    var showCartScreen by remember { mutableStateOf(false) }
    var showCheckoutScreen by remember { mutableStateOf(false) }

    // Other state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var prefillEmail by remember { mutableStateOf("") }
    var prefillPassword by remember { mutableStateOf("") }

    val isFormOpen = editingProductId != null || isAddingProduct

    // Back handler
    BackHandler(enabled = selectedProductId != null || showRegisterScreen || isFormOpen || showCartScreen || showCheckoutScreen) {
        when {
            showCheckoutScreen -> showCheckoutScreen = false
            showCartScreen -> showCartScreen = false
            isFormOpen -> { isAddingProduct = false; editingProductId = null }
            selectedProductId != null -> selectedProductId = null
            showRegisterScreen -> showRegisterScreen = false
        }
    }

    fun closeFormAndRefresh() {
        isAddingProduct = false
        editingProductId = null
        productsViewModel.loadProducts()
    }

    // Main navigation logic
    if (errorMessage != null) {
        // ... Error UI
    } else if (currentUser == null) {
        // --- Login/Register Flow ---
        if (showRegisterScreen) {
            RegisterScreen(registerViewModel, { email, password ->
                showRegisterScreen = false
                registerViewModel.resetState()
                prefillEmail = email
                prefillPassword = password
            }, { showRegisterScreen = false })
        } else {
            LoginScreen(loginViewModel, { user -> currentUser = user }, { showRegisterScreen = true }, prefillEmail, prefillPassword)
        }
    } else if (showCheckoutScreen) {
        // --- Checkout Flow ---
        val cartItems = cartViewModel.uiState.collectAsState().value.cartItems
        CheckoutScreen(
            checkoutViewModel = checkoutViewModel,
            cartItems = cartItems,
            onBack = { showCheckoutScreen = false },
            onCheckoutSuccess = { 
                showCheckoutScreen = false
                showCartScreen = false // Go back past the cart
                productsViewModel.loadProducts() // Refresh stock on product list
            }
        )
    } else if (showCartScreen) {
        // --- Cart Flow ---
        CartScreen(cartViewModel = cartViewModel, onBack = { showCartScreen = false }, onNavigateToCheckout = { showCheckoutScreen = true })
    } else if (isFormOpen) {
        // --- Add/Edit Product Flow ---
        LaunchedEffect(editingProductId) {
            editingProductId?.let { productFormViewModel.loadProduct(it) }
        }
        ProductFormScreen(productFormViewModel, { closeFormAndRefresh() }, {
            isAddingProduct = false
            editingProductId = null
        })
    } else if (selectedProductId != null) {
        // --- Product Detail Flow ---
        ProductoDetailScreen(selectedProductId!!, currentUser, { selectedProductId = null }, productDetailViewModel, { productId -> editingProductId = productId }, {
            selectedProductId = null
            productsViewModel.loadProducts()
        })
    } else {
        // --- Product List Flow (Default) ---
        ProductosScreen(currentUser!!, { currentUser = null; loginViewModel.logout() }, { productId -> selectedProductId = productId }, { error -> errorMessage = error }, productsViewModel, { productFormViewModel.resetForm(); isAddingProduct = true }, { showCartScreen = true })
    }
}