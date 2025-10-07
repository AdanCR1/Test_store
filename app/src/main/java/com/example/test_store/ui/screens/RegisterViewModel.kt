package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.RegisterRequest
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(request: RegisterRequest) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val success = repository.registerUser(request)
                _uiState.value = RegisterUiState(isSuccess = success)
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = e.message ?: "Error desconocido")
            }
        }
    }

    fun onRegistrationErrorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = RegisterUiState() // Reset all state variables
    }
}

class RegisterViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
