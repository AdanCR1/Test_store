package com.example.test_store.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_store.data.model.User
import com.example.test_store.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser.asStateFlow()

    fun login(email: String, password: String) {
        // Don't start a new login if one is already in progress
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                val user = repository.userLogin(email, password)
                _loggedInUser.value = user
                _uiState.value = LoginUiState(loginSuccess = true)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = e.message ?: "Error desconocido")
            }
        }
    }

    fun onLoginErrorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        _loggedInUser.value = null
        _uiState.value = LoginUiState()
    }
}

class LoginViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
