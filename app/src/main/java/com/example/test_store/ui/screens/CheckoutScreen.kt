package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.test_store.data.model.CartItem
import com.example.test_store.ui.utils.formatPrice
import java.util.Calendar

// --- Visual Transformations for formatting ---

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += " / "
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 3
                return 7
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 7) return offset - 3
                return 4
            }
        }

        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

// --- Main Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    checkoutViewModel: CheckoutViewModel,
    cartItems: List<CartItem>,
    onBack: () -> Unit,
    onCheckoutSuccess: () -> Unit
) {
    val uiState by checkoutViewModel.uiState.collectAsState()

    // Form States
    var address by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Validation States
    val isCardNumberValid = cardNumber.length == 16
    val isCvvValid = cvv.length in 3..4
    val isExpiryDateValid = remember(expiryDate) {
        if (expiryDate.length != 4) return@remember false
        val month = expiryDate.substring(0, 2).toIntOrNull() ?: 0
        val year = expiryDate.substring(2, 4).toIntOrNull() ?: 0
        if (month !in 1..12) return@remember false

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        
        (year > currentYear) || (year == currentYear && month >= currentMonth)
    }

    val isFormValid = isCardNumberValid && isCvvValid && isExpiryDateValid && address.isNotBlank()

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            onCheckoutSuccess()
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        val totalItems = cartItems.sumOf { it.cantidad }
        val totalPrice = cartItems.sumOf { it.precio * it.cantidad }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Pedido") },
            text = { Text("Vas a comprar $totalItems producto(s) por un total de ${formatPrice(totalPrice)}. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        checkoutViewModel.performCheckout(cartItems, address)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Dirección de Envío", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección, Ciudad, etc.") }, modifier = Modifier.fillMaxWidth(), isError = address.isBlank())
            
            Spacer(modifier = Modifier.height(24.dp))

            Text("Información de Pago", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it.filter { char -> char.isDigit() } },
                label = { Text("Número de Tarjeta (16 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !isCardNumberValid && cardNumber.isNotEmpty()
            )

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { if (it.length <= 4) expiryDate = it.filter { char -> char.isDigit() } },
                    label = { Text("MM/AA") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation(),
                    isError = !isExpiryDateValid && expiryDate.length == 4
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 4) cvv = it.filter { char -> char.isDigit() } },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isCvvValid && cvv.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { showConfirmDialog = true }, // Show dialog instead of direct checkout
                enabled = isFormValid && !uiState.isProcessing,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar Compra")
                }
            }
        }
    }
}
