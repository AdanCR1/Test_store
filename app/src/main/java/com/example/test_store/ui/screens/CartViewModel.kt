package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.CartItem
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CartViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private var updateJobs = mutableMapOf<Int, Job>()

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val cartItems = repository.getCart()
                _uiState.value = _uiState.value.copy(cartItems = cartItems, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Error al cargar el carrito", isLoading = false)
            }
        }
    }

    fun updateQuantity(cartItemId: Int, newQuantity: Int) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.cartItemId == cartItemId }
        if (itemIndex == -1) return

        val originalQuantity = currentItems[itemIndex].cantidad
        if (newQuantity == originalQuantity) return

        // Optimistic UI update
        currentItems[itemIndex] = currentItems[itemIndex].copy(cantidad = newQuantity)
        _uiState.value = _uiState.value.copy(cartItems = currentItems)

        // Debounce network call
        updateJobs[cartItemId]?.cancel()
        updateJobs[cartItemId] = viewModelScope.launch {
            delay(500) // Wait for 500ms of inactivity
            try {
                val success = repository.updateCartItemQuantity(cartItemId, newQuantity)
                if (!success) {
                    // Revert on failure
                    val revertedItems = _uiState.value.cartItems.toMutableList()
                    val revertedIndex = revertedItems.indexOfFirst { it.cartItemId == cartItemId }
                    if (revertedIndex != -1) {
                        revertedItems[revertedIndex] = revertedItems[revertedIndex].copy(cantidad = originalQuantity)
                        _uiState.value = _uiState.value.copy(cartItems = revertedItems, error = "No se pudo actualizar el carrito.")
                    }
                }
            } catch (e: Exception) {
                // Revert on error
                val revertedItems = _uiState.value.cartItems.toMutableList()
                val revertedIndex = revertedItems.indexOfFirst { it.cartItemId == cartItemId }
                if (revertedIndex != -1) {
                    revertedItems[revertedIndex] = revertedItems[revertedIndex].copy(cantidad = originalQuantity)
                    _uiState.value = _uiState.value.copy(cartItems = revertedItems, error = e.message)
                }
            }
        }
    }

    fun removeFromCart(cartItemId: Int) {
        val currentItems = _uiState.value.cartItems
        val itemToRemove = currentItems.find { it.cartItemId == cartItemId } ?: return

        // Optimistic UI update
        _uiState.value = _uiState.value.copy(cartItems = currentItems.filter { it.cartItemId != cartItemId })

        viewModelScope.launch {
            try {
                val success = repository.removeFromCart(cartItemId)
                if (!success) {
                    // Revert on failure
                    _uiState.value = _uiState.value.copy(cartItems = currentItems, error = "No se pudo eliminar el ítem.")
                }
            } catch (e: Exception) {
                // Revert on error
                _uiState.value = _uiState.value.copy(cartItems = currentItems, error = e.message)
            }
        }
    }
}

class CartViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}