package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test_store.data.model.User
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetailScreen(
    productId: Int,
    currentUser: User?,
    onBack: () -> Unit,
    productDetailViewModel: ProductDetailViewModel,
    onNavigateToEdit: (Int) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by productDetailViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onDeleteSuccess()
        }
    }

    LaunchedEffect(uiState.addToCartSuccess) {
        if (uiState.addToCartSuccess) {
            delay(2000) // Show success message for 2 seconds
            productDetailViewModel.resetAddToCartStatus()
        }
    }

    LaunchedEffect(productId) {
        productDetailViewModel.loadProduct(productId)
    }

    val product = uiState.product

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { productDetailViewModel.deleteProduct(productId); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.nombre ?: "Cargando...") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás") } },
                actions = {
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = { onNavigateToEdit(productId) }) { Icon(Icons.Default.Edit, contentDescription = "Editar Producto") }
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar Producto", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.isDeleting -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Eliminando producto...")
                        }
                    }
                }
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                uiState.deleteError != null -> Text("Error: ${uiState.deleteError}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                product != null -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.nombre ?: "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!product.imageUrl.isNullOrBlank()) {
                            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                                AsyncImage(model = product.imageUrl, contentDescription = "Imagen de ${product.nombre}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(250.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (product.stock != null && product.stock <= 0) {
                            Text(text = "Fuera de Stock", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(text = "$${product.precio ?: 0.0}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                        } else {
                            Text(text = "$${product.precio ?: 0.0}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Categoría: ${product.categoriaNombre ?: "N/A"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(product.descripcion ?: "", style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.weight(1f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (uiState.addToCartError != null) {
                                Text(uiState.addToCartError!!, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Button(
                                onClick = { productDetailViewModel.addToCart(productId) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = (product.stock ?: 0) > 0 && !uiState.isAddingToCart && !uiState.addToCartSuccess
                            ) {
                                when {
                                    uiState.isAddingToCart -> {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                    uiState.addToCartSuccess -> {
                                        Text("¡Añadido!")
                                    }
                                    else -> {
                                        Text("Añadir al Carrito")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}