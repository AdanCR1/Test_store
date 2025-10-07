package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.Category
import com.example.test_store.data.model.Producto
import com.example.test_store.data.model.ProductUpdateRequest
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State for the form UI
data class ProductFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val loadError: String? = null,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

// State for the form fields
data class ProductFormState(
    val id: Int? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stock: String = "",
    val imagenUrl: String = "",
    val categoriaNombre: String = ""
)

class ProductFormViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductFormUiState(isLoading = true)
            try {
                val product = repository.loadSingleProductFromAPI(productId)
                _formState.value = ProductFormState(
                    id = product.id,
                    nombre = product.nombre ?: "",
                    descripcion = product.descripcion ?: "",
                    precio = product.precio?.toString() ?: "",
                    stock = product.stock?.toString() ?: "",
                    imagenUrl = product.imageUrl ?: "",
                    categoriaNombre = product.categoriaNombre ?: ""
                )
                _uiState.value = ProductFormUiState(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = ProductFormUiState(isLoading = false, loadError = e.message)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) {
                // Error loading categories, can be handled in UI
                _uiState.value = _uiState.value.copy(loadError = "Error al cargar categorías: ${e.message}")
            }
        }
    }
    
    fun onFormFieldChange(
        nombre: String = _formState.value.nombre,
        descripcion: String = _formState.value.descripcion,
        precio: String = _formState.value.precio,
        stock: String = _formState.value.stock,
        imagenUrl: String = _formState.value.imagenUrl,
        categoriaNombre: String = _formState.value.categoriaNombre
    ) {
        _formState.value = _formState.value.copy(
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUrl = imagenUrl,
            categoriaNombre = categoriaNombre
        )
    }

    fun saveProduct() {
        viewModelScope.launch {
            _uiState.value = ProductFormUiState(isSaving = true)
            
            val currentFormState = _formState.value
            val productRequest = ProductUpdateRequest(
                id = currentFormState.id,
                nombre = currentFormState.nombre,
                descripción = currentFormState.descripcion,
                precio = currentFormState.precio.toDoubleOrNull() ?: 0.0,
                stock = currentFormState.stock.toIntOrNull() ?: 0,
                imagenUrl = currentFormState.imagenUrl,
                categoriaNombre = currentFormState.categoriaNombre
            )

            try {
                val success = if (productRequest.id == null) {
                    repository.createProduct(productRequest)
                } else {
                    repository.updateProduct(productRequest)
                }

                if (success) {
                    _uiState.value = ProductFormUiState(saveSuccess = true)
                } else {
                     _uiState.value = ProductFormUiState(saveError = "No se pudo guardar el producto.")
                }
            } catch (e: Exception) {
                _uiState.value = ProductFormUiState(saveError = e.message)
            }
        }
    }

    fun resetForm() {
        _formState.value = ProductFormState()
        _uiState.value = ProductFormUiState()
    }
}

// Factory for the ViewModel
class ProductFormViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
