package com.example.estudiamentes.ui

import android.widget.Button
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen(
    welcomeMessage: String? = null,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(welcomeMessage) {
        if (welcomeMessage != null) {
            snackbarHostState.showSnackbar(welcomeMessage)
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Button(
            onClick = onLogout
        ) {
            Text("Cerrar sesión")
        }
    }
}
