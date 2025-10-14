package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.Producto
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Producto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null,
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false,
    val addToCartError: String? = null
)

class ProductDetailViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, deleteError = null, deleteSuccess = false)
            try {
                val product = repository.loadSingleProductFromAPI(productId)
                _uiState.value = _uiState.value.copy(product = product, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Error al cargar el producto", isLoading = false)
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteError = null)
            try {
                val success = repository.deleteProduct(productId)
                if (success) {
                    _uiState.value = _uiState.value.copy(isDeleting = false, deleteSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isDeleting = false, deleteError = "No se pudo eliminar el producto.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeleting = false, deleteError = e.message ?: "Error desconocido")
            }
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingToCart = true, addToCartError = null, addToCartSuccess = false)
            try {
                val success = repository.addToCart(productId, 1) // Always add 1 quantity for now
                if (success) {
                    _uiState.value = _uiState.value.copy(isAddingToCart = false, addToCartSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isAddingToCart = false, addToCartError = "No se pudo añadir al carrito.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAddingToCart = false, addToCartError = e.message ?: "Error desconocido")
            }
        }
    }

    fun resetAddToCartStatus() {
        _uiState.value = _uiState.value.copy(addToCartSuccess = false, addToCartError = null)
    }
}

class ProductDetailViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
