package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test_store.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetailScreen(
    productId: Int,
    currentUser: User?, // New parameter
    onBack: () -> Unit,
    productDetailViewModel: ProductDetailViewModel,
    onNavigateToEdit: (Int) -> Unit // New callback
) {
    // Load the product when the screen is first composed or productId changes
    LaunchedEffect(productId) {
        productDetailViewModel.loadProduct(productId)
    }

    val uiState by productDetailViewModel.uiState.collectAsState()
    val product = uiState.product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.nombre ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // Show Edit button only for admins
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = { onNavigateToEdit(productId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Producto")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                product != null -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.nombre ?: "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!product.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = "Imagen de ${product.nombre}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            "$${product.precio ?: 0.0}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Categoría: ${product.categoriaNombre ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(product.descripcion ?: "", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}