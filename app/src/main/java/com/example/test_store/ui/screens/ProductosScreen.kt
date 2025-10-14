package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test_store.data.model.Category
import com.example.test_store.data.model.User
import com.example.test_store.ui.components.ProductoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    currentUser: User,
    onLogout: () -> Unit,
    onProductoClick: (Int) -> Unit,
    onError: (String) -> Unit,
    productsViewModel: ProductsViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToCart: () -> Unit // New callback
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by productsViewModel.uiState.collectAsState()

    if (uiState.error != null) {
        onError(uiState.error!!)
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirmar Cierre de Sesión") },
            text = { Text("¿Estás seguro de que quieres salir?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Salir")
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ROG Store") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                },
                actions = {
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Text("Salir", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser.isAdmin) {
                FloatingActionButton(onClick = onNavigateToAdd, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bienvenido, ${currentUser.nombre}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(currentUser.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if(currentUser.isAdmin) {
                        Text("Rol: Administrador", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            CategoryFilters(uiState.categories, uiState.selectedCategory) { category ->
                productsViewModel.filterByCategory(category)
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando...")
                    }
                }
                uiState.products.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos en esta categoría.")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        items(uiState.products) { producto ->
                            ProductoCard(producto, onClick = { onProductoClick(producto.id) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isSelected = selectedCategory == null
            if (isSelected) {
                Button(onClick = { onCategorySelected(null) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Todos")
                }
            } else {
                OutlinedButton(onClick = { onCategorySelected(null) }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Text("Todos", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        items(categories) { category ->
            val isSelected = selectedCategory?.id == category.id
            if (isSelected) {
                Button(onClick = { onCategorySelected(category) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(category.nombre)
                }
            } else {
                OutlinedButton(onClick = { onCategorySelected(category) }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Text(category.nombre, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
