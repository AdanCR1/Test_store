package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.Category
import com.example.test_store.data.model.Producto
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// The UI state for the products screen
data class ProductsUiState(
    val products: List<Producto> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// The ViewModel
class ProductsViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState(isLoading = true))
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    // Private cache of all products
    private var allProducts: List<Producto> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch both products and categories in parallel
                val productsDeferred = viewModelScope.launch {
                    allProducts = repository.loadProductosFromAPI()
                }
                val categoriesDeferred = viewModelScope.launch {
                    val categories = repository.getCategories()
                    _uiState.value = _uiState.value.copy(categories = categories)
                }

                productsDeferred.join()
                categoriesDeferred.join()

                // Initial state shows all products
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = allProducts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos"
                )
            }
        }
    }
    
    fun loadProducts() {
        // This function can be used to force a refresh
        loadInitialData()
    }

    fun filterByCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        
        val filteredList = if (category == null) {
            allProducts
        } else {
            allProducts.filter { it.categoriaNombre == category.nombre }
        }
        
        _uiState.value = _uiState.value.copy(products = filteredList)
    }
}

// Factory to create the ViewModel with the repository dependency
class ProductsViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}