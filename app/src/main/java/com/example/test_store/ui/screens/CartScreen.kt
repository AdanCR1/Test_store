package com.example.test_store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test_store.data.model.CartItem
import com.example.test_store.ui.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit // New callback
) {
    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }

    val uiState by cartViewModel.uiState.collectAsState()
    val cartItems = uiState.cartItems
    val total = cartItems.sumOf { it.precio * it.cantidad }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás") } }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomAppBar(
                    modifier = Modifier.height(80.dp), // Adjusted height
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatPrice(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(onClick = onNavigateToCheckout) {
                            Text("Ir a Pagar")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                cartItems.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tu carrito está vacío") }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(cartItems, key = { it.cartItemId }) { item ->
                            CartItemView(item = item, onQuantityChange = { newQuantity -> cartViewModel.updateQuantity(item.cartItemId, newQuantity) }, onRemove = { cartViewModel.removeFromCart(item.cartItemId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemView(item: CartItem, onQuantityChange: (Int) -> Unit, onRemove: () -> Unit) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = item.imageUrl, contentDescription = item.nombre, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(formatPrice(item.precio), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text("Subtotal: ${formatPrice(item.precio * item.cantidad)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QuantitySelector(quantity = item.cantidad, maxStock = item.stock, onQuantityChange = onQuantityChange)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, maxStock: Int, onQuantityChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }, modifier = Modifier.size(36.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Restar")
        }
        Text(quantity.toString(), modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = { if (quantity < maxStock) onQuantityChange(quantity + 1) }, modifier = Modifier.size(36.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Añadir")
        }
    }
}