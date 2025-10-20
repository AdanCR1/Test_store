package com.example.test_store.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_store.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    userManagementViewModel: UserManagementViewModel,
    onBack: () -> Unit
) {
    val uiState by userManagementViewModel.uiState.collectAsState()
    var expandedUserId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        userManagementViewModel.onScreenShown()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) {
        innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando Usuarios...")
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                        items(uiState.users) { user ->
                            UserManagementCard(
                                user = user,
                                isExpanded = user.id == expandedUserId,
                                onExpandClick = { clickedUserId ->
                                    expandedUserId = if (expandedUserId == clickedUserId) null else clickedUserId
                                },
                                onUpdate = { updatedUser -> userManagementViewModel.updateUser(updatedUser) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementCard(
    user: User,
    isExpanded: Boolean,
    onExpandClick: (Int) -> Unit,
    onUpdate: (User) -> Unit
) {
    var isAdmin by remember { mutableStateOf(user.isAdmin) }
    var isSuperAdmin by remember { mutableStateOf(user.isSuperAdmin) }
    var isActive by remember { mutableStateOf(user.isActive) }

    // Update internal state when user prop changes (e.g., after a successful update from the ViewModel)
    LaunchedEffect(user) {
        isAdmin = user.isAdmin
        isSuperAdmin = user.isSuperAdmin
        isActive = user.isActive
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick(user.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = user.email, style = MaterialTheme.typography.bodySmall)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isAdmin, onCheckedChange = { isAdmin = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isSuperAdmin, onCheckedChange = { isSuperAdmin = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Super Admin")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { 
                    val updatedUser = user.copy(isAdmin = isAdmin, isSuperAdmin = isSuperAdmin, isActive = isActive)
                    onUpdate(updatedUser)
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}
