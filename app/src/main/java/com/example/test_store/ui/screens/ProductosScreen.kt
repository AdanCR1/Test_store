package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onNavigateToAdd: () -> Unit // New callback
) {
    val uiState by productsViewModel.uiState.collectAsState()

    // Handle error state
    if (uiState.error != null) {
        onError(uiState.error!!)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ROG Store - Productos") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            // Show FAB only for admins
            if (currentUser.isAdmin) {
                FloatingActionButton(onClick = onNavigateToAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
                }
            }
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
                    // Show admin status
                    if(currentUser.isAdmin) {
                        Text("Rol: Administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando productos...")
                        }
                    }
                }
                uiState.products.isEmpty() -> {
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
                        items(uiState.products) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { onProductoClick(producto.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}