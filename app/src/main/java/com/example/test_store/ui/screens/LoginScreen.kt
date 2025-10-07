package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test_store.data.model.User
import com.example.test_store.ui.theme.Test_storeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    prefillEmail: String = "",
    prefillPassword: String = ""
) {
    var email by remember { mutableStateOf(prefillEmail.ifEmpty { "marshel@tecba.com" }) }
    var password by remember { mutableStateOf(prefillPassword.ifEmpty { "marshel123" }) }

    val uiState by loginViewModel.uiState.collectAsState()
    val loggedInUser by loginViewModel.loggedInUser.collectAsState()

    // Observe login success
    LaunchedEffect(loggedInUser) {
        loggedInUser?.let {
            onLoginSuccess(it)
        }
    }

    // Observe errors
    var localErrorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            localErrorMessage = it
            loginViewModel.onLoginErrorShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ROG Store",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = localErrorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = localErrorMessage != null
        )

        if (localErrorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(localErrorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    localErrorMessage = "Completa todos los campos"
                    return@Button
                }
                localErrorMessage = null
                loginViewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (uiState.isLoading) "Iniciando sesión..." else "Iniciar Sesión")
        }

        // Credenciales de demo
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Demo: marshel@tecba.com / marshel123",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate aquí.")
        }
    }
}

// The preview is now broken because it can't easily instantiate the ViewModel.
// This is expected and can be fixed with a more advanced preview setup,
// but for now, we focus on the app's functionality.
@Preview
@Composable
fun PreviewLogin() {
    // Test_storeTheme { LoginScreen(onLoginSuccess = {}, onError = {}) }
}