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
    val error: String? = null
)

class ProductDetailViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState(isLoading = true)
            try {
                val product = repository.loadSingleProductFromAPI(productId)
                _uiState.value = ProductDetailUiState(product = product, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = ProductDetailUiState(error = e.message ?: "Error al cargar el producto", isLoading = false)
            }
        }
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
