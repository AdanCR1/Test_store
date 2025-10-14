package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.CartItem
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isProcessing: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val error: String? = null
)

class CheckoutViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun performCheckout(cartItems: List<CartItem>, address: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState(isProcessing = true)
            try {
                val success = repository.checkout(cartItems, address)
                if (success) {
                    _uiState.value = CheckoutUiState(checkoutSuccess = true)
                } else {
                    _uiState.value = CheckoutUiState(error = "No se pudo completar la compra.")
                }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState(error = e.message ?: "Error desconocido")
            }
        }
    }
}

class CheckoutViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
